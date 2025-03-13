/* 02_vertex_and_fragment_program.c
   Пример использования Cg и Direct3D9 для рисования одного треугольника,
   на который наложена программно созданная (процедурная) текстура.

   Требования:
   - Установленный DirectX 9 (или старый June 2010 SDK).
   - Установленный Cg Toolkit (NVIDIA).
   - Правильное подключение библиотек d3dx9.lib, etc.
*/

#include <windows.h>
#include <stdio.h>
#include <d3d9.h>       // Библиотека Direct3D9
#include <Cg/cg.h>      // Библиотека Cg
#include <Cg/cgD3D9.h>
#include "DXUT.h"       // DirectX Utility Toolkit (входит в DX SDK)

#pragma comment(lib, "dxguid.lib")
#pragma comment(lib, "d3d9.lib")
// Не забудьте добавить в проект/систему библиотеку d3dx9.lib, например:
// #pragma comment(lib, "d3dx9.lib")

/* Глобальные переменные для Cg */
static CGcontext   myCgContext;
static CGprofile   myCgVertexProfile,
myCgFragmentProfile;
static CGprogram   myCgVertexProgram,
myCgFragmentProgram;

/* Глобальные объекты Direct3D */
static LPDIRECT3DVERTEXBUFFER9 myVertexBuffer = NULL;
static LPDIRECT3DTEXTURE9      myTexture = NULL;

/* Пути и имена шейдерных программ Cg */
static const WCHAR* myProgramNameW = L"02_vertex_and_fragment_program";
static const char* myProgramName = "02_vertex_and_fragment_program";
static const char* myVertexProgramFileName = "texturedTri.cg";
static const char* myVertexProgramName = "texturedTriVert";
static const char* myFragmentProgramFileName = "texturedTri.cg";
static const char* myFragmentProgramName = "texturedTriFrag";

/* Функция проверки ошибок Cg */
static void checkForCgError(const char* situation)
{
    char buffer[4096];
    CGerror error;
    const char* string = cgGetLastErrorString(&error);

    if (error != CG_NO_ERROR) {
        if (error == CG_COMPILER_ERROR) {
            sprintf(buffer,
                "Program: %s\n"
                "Situation: %s\n"
                "Error: %s\n\n"
                "Cg compiler output...\n",
                myProgramName, situation, string);
            OutputDebugStringA(buffer);
            OutputDebugStringA(cgGetLastListing(myCgContext));
            sprintf(buffer,
                "Program: %s\n"
                "Situation: %s\n"
                "Error: %s\n\n"
                "Check debug output for Cg compiler output...",
                myProgramName, situation, string);
            MessageBoxA(0, buffer,
                "Cg compilation error", MB_OK | MB_ICONSTOP | MB_TASKMODAL);
        }
        else {
            sprintf(buffer,
                "Program: %s\n"
                "Situation: %s\n"
                "Error: %s",
                myProgramName, situation, string);
            MessageBoxA(0, buffer,
                "Cg runtime error", MB_OK | MB_ICONSTOP | MB_TASKMODAL);
        }
        exit(1);
    }
}

/* Объявляем вперёд каллбэки, которые понадобятся DXUT. */
static HRESULT CALLBACK OnResetDevice(IDirect3DDevice9*, const D3DSURFACE_DESC*, void*);
static void    CALLBACK OnFrameRender(IDirect3DDevice9*, double, float, void*);
static void    CALLBACK OnLostDevice(void*);

/*
   Процедурное создание "шахматной" текстуры в памяти.
   Вы можете изменить логику заполнения под любые цвета/паттерны.
*/
static HRESULT createProceduralTexture(IDirect3DDevice9* pDev, LPDIRECT3DTEXTURE9* outTexture,
    int width, int height)
{
    HRESULT hr = pDev->CreateTexture(width,
        height,
        1,            // количество мип-уровней (1 = без мипов)
        0,            // usage
        D3DFMT_A8R8G8B8,
        D3DPOOL_MANAGED,
        outTexture,
        NULL);
    if (FAILED(hr)) {
        return hr;
    }

    // Блокируем текстуру, чтобы заполнить пиксели
    D3DLOCKED_RECT lockedRect;
    if (SUCCEEDED((*outTexture)->LockRect(0, &lockedRect, NULL, 0))) {
        DWORD* data = (DWORD*)lockedRect.pBits;
        // Заполняем текстуру простым узором (черно-белая "шахматка")
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Каждая клетка 8x8
                bool black = (((x / 8) % 2) ^ ((y / 8) % 2)) != 0;
                // Если black = true, цвет = черный; иначе белый.
                DWORD color = black ? 0xFF000000 : 0xFFFFFFFF;
                // Записываем в массив (учитывая Pitch)
                data[x + (y * (lockedRect.Pitch / 4))] = color;
            }
        }
        (*outTexture)->UnlockRect(0);
    }

    return S_OK;
}

/* Точка входа Win32-приложения */
INT WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int)
{
    /* Создаём контекст Cg */
    myCgContext = cgCreateContext();
    checkForCgError("creating context");
    cgSetParameterSettingMode(myCgContext, CG_DEFERRED_PARAMETER_SETTING);

    /* Устанавливаем каллбэки DXUT */
    DXUTSetCallbackDeviceReset(OnResetDevice);
    DXUTSetCallbackDeviceLost(OnLostDevice);
    DXUTSetCallbackFrameRender(OnFrameRender);

    /* Инициализация DXUT, создание окна */
    DXUTInit();
    DXUTCreateWindow(myProgramNameW);

    /* Создаём устройство с заданным размером окна */
    DXUTCreateDevice(D3DADAPTER_DEFAULT, true, 400, 400);

    /* Запускаем основной цикл */
    DXUTMainLoop();

    /* Освобождаем Cg-программы и контекст перед выходом */
    cgDestroyProgram(myCgVertexProgram);
    checkForCgError("destroying vertex program");
    cgDestroyProgram(myCgFragmentProgram);
    checkForCgError("destroying fragment program");
    cgDestroyContext(myCgContext);

    return DXUTGetExitCode();
}

/* Создаём и компилируем вершинный и фрагментный шейдеры */
static void createCgPrograms()
{
    const char** profileOpts;

    // Профиль для вершинного шейдера
    myCgVertexProfile = cgD3D9GetLatestVertexProfile();
    checkForCgError("getting latest vertex profile");
    profileOpts = cgD3D9GetOptimalOptions(myCgVertexProfile);
    checkForCgError("getting vertex profile options");

    myCgVertexProgram =
        cgCreateProgramFromFile(
            myCgContext,
            CG_SOURCE,
            myVertexProgramFileName,
            myCgVertexProfile,
            myVertexProgramName,
            profileOpts);
    checkForCgError("creating vertex program from file");

    // Профиль для фрагментного (пиксельного) шейдера
    myCgFragmentProfile = cgD3D9GetLatestPixelProfile();
    checkForCgError("getting latest fragment profile");
    profileOpts = cgD3D9GetOptimalOptions(myCgFragmentProfile);
    checkForCgError("getting fragment profile options");

    myCgFragmentProgram =
        cgCreateProgramFromFile(
            myCgContext,
            CG_SOURCE,
            myFragmentProgramFileName,
            myCgFragmentProfile,
            myFragmentProgramName,
            profileOpts);
    checkForCgError("creating fragment program from file");
}

/* Структура вершины: позиция + координаты текстуры */
struct MY_V3FT2
{
    FLOAT x, y, z;  // координаты (XYZ)
    FLOAT u, v;     // текстурные координаты
};

/* Инициализация вершинного буфера для одного треугольника */
static HRESULT initVertexBuffer(IDirect3DDevice9* pDev)
{
    // Определяем три вершины треугольника в clip space
    MY_V3FT2 triangleVertices[3] = {
        //          x       y     z      u    v
        {  0.0f,   0.8f,   0.0f,  0.5f, 0.0f },  // Верхняя вершина
        { -0.8f,  -0.8f,   0.0f,  0.0f, 1.0f },  // Левая нижняя
        {  0.8f,  -0.8f,   0.0f,  1.0f, 1.0f },  // Правая нижняя
    };

    // Создаём вершинный буфер
    HRESULT hr = pDev->CreateVertexBuffer(sizeof(triangleVertices),
        0,
        D3DFVF_XYZ | D3DFVF_TEX1, // формат вершин
        D3DPOOL_DEFAULT,
        &myVertexBuffer,
        NULL);
    if (FAILED(hr)) {
        return E_FAIL;
    }

    // Заполняем буфер
    VOID* pVertices;
    if (FAILED(myVertexBuffer->Lock(0, 0, &pVertices, 0))) {
        return E_FAIL;
    }
    memcpy(pVertices, triangleVertices, sizeof(triangleVertices));
    myVertexBuffer->Unlock();

    // Создаём и заполняем «шахматную» текстуру 128x128
    hr = createProceduralTexture(pDev, &myTexture, 128, 128);
    if (FAILED(hr)) {
        MessageBox(NULL,
            L"Не удалось создать процедурную текстуру!",
            L"Ошибка",
            MB_OK | MB_ICONERROR);
        return E_FAIL;
    }

    return S_OK;
}

/* Каллбэк, вызываемый при создании/восстановлении устройства (Reset) */
static HRESULT CALLBACK OnResetDevice(IDirect3DDevice9* pDev,
    const D3DSURFACE_DESC* backBuf,
    void* userContext)
{
    // Привязываем устройство Direct3D9 к Cg
    cgD3D9SetDevice(pDev);
    checkForCgError("setting Direct3D device");

    static int firstTime = 1;
    if (firstTime)
    {
        // Компилируем программы Cg один раз
        createCgPrograms();
        firstTime = 0;
    }

    // Загружаем (или повторно загружаем) шейдеры Cg
    cgD3D9LoadProgram(myCgVertexProgram, false, 0);
    checkForCgError("loading vertex program");
    cgD3D9LoadProgram(myCgFragmentProgram, false, 0);
    checkForCgError("loading fragment program");

    // Инициализируем вершинный буфер + создаём текстуру
    return initVertexBuffer(pDev);
}

/* Отрисовка кадра */
static void CALLBACK OnFrameRender(IDirect3DDevice9* pDev,
    double time,
    float elapsedTime,
    void* userContext)
{
    // Очищаем буфер экрана и z-буфер (цвет фона можно поменять)
    pDev->Clear(0, NULL,
        D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER,
        D3DXCOLOR(0.1f, 0.3f, 0.6f, 1.0f),
        1.0f,
        0);

    if (SUCCEEDED(pDev->BeginScene()))
    {
        // Привязываем шейдеры Cg
        cgD3D9BindProgram(myCgVertexProgram);
        checkForCgError("binding vertex program");

        cgD3D9BindProgram(myCgFragmentProgram);
        checkForCgError("binding fragment program");

        // Устанавливаем текстуру в семплер 0
        pDev->SetTexture(0, myTexture);

        // Опционально отключаем освещение и culling, на случай, если оно мешает
        pDev->SetRenderState(D3DRS_LIGHTING, FALSE);
        pDev->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);

        // Указываем, как читать вершины (stride и формат)
        pDev->SetStreamSource(0, myVertexBuffer, 0, sizeof(MY_V3FT2));
        pDev->SetFVF(D3DFVF_XYZ | D3DFVF_TEX1);

        // Рисуем один треугольник (D3DPT_TRIANGLELIST)
        pDev->DrawPrimitive(D3DPT_TRIANGLELIST, 0, 1);

        pDev->EndScene();
    }
}

/* Каллбэк, вызываемый при потере устройства (Lost) */
static void CALLBACK OnLostDevice(void* userContext)
{
    // Освобождаем объекты, привязанные к устройству
    if (myVertexBuffer) {
        myVertexBuffer->Release();
        myVertexBuffer = NULL;
    }
    if (myTexture) {
        myTexture->Release();
        myTexture = NULL;
    }

    // Отвязываем устройство из Cg
    cgD3D9SetDevice(NULL);
}
