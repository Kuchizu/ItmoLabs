/* 02_vertex_and_fragment_program.c - OpenGL-based very simple vertex program example
   using Cg program from Chapter 2 of "The Cg Tutorial" (Addison-Wesley,
   ISBN 0321194969). */

#include <windows.h>
#include <stdio.h>
#include <d3d9.h>     /* Can't include this?  Is DirectX SDK installed? */
#include <Cg/cg.h>    /* Can't include this?  Is Cg Toolkit installed! */
#include <Cg/cgD3D9.h>

#include "DXUT.h"  /* DirectX Utility Toolkit (part of the DirectX SDK) */

#pragma comment(lib, "dxguid.lib")
#pragma comment(lib, "d3d9.lib")

static CGcontext myCgContext;
static CGprofile myCgVertexProfile,
myCgFragmentProfile;
static CGprogram myCgVertexProgram,
myCgFragmentProgram;

static LPDIRECT3DVERTEXBUFFER9 myVertexBuffer = NULL;

static const WCHAR* myProgramNameW = L"02_vertex_and_fragment_program";
static const char* myProgramName = "02_vertex_and_fragment_program",
* myVertexProgramFileName = "C2E1v_green.cg",
/* Page 38 */* myVertexProgramName = "C2E1v_green",
* myFragmentProgramFileName = "C2E2f_passthru.cg",
/* Page 53 */* myFragmentProgramName = "C2E2f_passthru";

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

/* Forward declared DXUT callbacks registered by WinMain. */
static HRESULT CALLBACK OnResetDevice(IDirect3DDevice9*, const D3DSURFACE_DESC*, void*);
static void CALLBACK OnFrameRender(IDirect3DDevice9*, double, float, void*);
static void CALLBACK OnLostDevice(void*);

INT WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int)
{
    myCgContext = cgCreateContext();
    checkForCgError("creating context");
    cgSetParameterSettingMode(myCgContext, CG_DEFERRED_PARAMETER_SETTING);

    DXUTSetCallbackDeviceReset(OnResetDevice);
    DXUTSetCallbackDeviceLost(OnLostDevice);
    DXUTSetCallbackFrameRender(OnFrameRender);

    /* Parse  command line, handle  default hotkeys, and show messages. */
    DXUTInit();

    DXUTCreateWindow(myProgramNameW);

    /* Display 400x400 window. */
    DXUTCreateDevice(D3DADAPTER_DEFAULT, true, 400, 400);

    DXUTMainLoop();

    cgDestroyProgram(myCgVertexProgram);
    checkForCgError("destroying vertex program");
    cgDestroyProgram(myCgFragmentProgram);
    checkForCgError("destroying fragment program");
    cgDestroyContext(myCgContext);

    return DXUTGetExitCode();
}

static void createCgPrograms()
{
    const char** profileOpts;

    /* Determine the best profiles once a device to be set. */
    myCgVertexProfile = cgD3D9GetLatestVertexProfile();
    checkForCgError("getting latest vertex profile");

    profileOpts = cgD3D9GetOptimalOptions(myCgVertexProfile);
    checkForCgError("getting latest vertex profile options");

    myCgVertexProgram =
        cgCreateProgramFromFile(
            myCgContext,              /* Cg runtime context */
            CG_SOURCE,                /* Program in human-readable form */
            myVertexProgramFileName,  /* Name of file containing program */
            myCgVertexProfile,        /* Profile: OpenGL ARB vertex program */
            myVertexProgramName,      /* Entry function name */
            profileOpts);             /* Pass optimal compiler options */
    checkForCgError("creating vertex program from file");

    /* Determine the best profile once a device to be set. */
    myCgFragmentProfile = cgD3D9GetLatestPixelProfile();
    checkForCgError("getting latest fragment profile");

    profileOpts = cgD3D9GetOptimalOptions(myCgFragmentProfile);
    checkForCgError("getting latest fragment profile options");

    myCgFragmentProgram =
        cgCreateProgramFromFile(
            myCgContext,                /* Cg runtime context */
            CG_SOURCE,                  /* Program in human-readable form */
            myFragmentProgramFileName,  /* Name of file containing program */
            myCgFragmentProfile,        /* Profile: OpenGL ARB vertex program */
            myFragmentProgramName,      /* Entry function name */
            profileOpts);               /* No extra compiler options */
    checkForCgError("creating fragment program from file");
}

static const struct ShapeList {
    float x, y;
    float width, height;
} myShapeList[] = {
    /*   x       y     width  height */
    {  -0.5f,   0.0f,  0.4f,  0.3f },
    {   0.5f,   0.5f,  0.3f,  0.5f },
    {   0.0f,  -0.5f,  0.6f,  0.4f }
};
static int myShapeCount = sizeof(myShapeList) / sizeof(myShapeList[0]);

struct MY_V3F {
    FLOAT x, y, z;
};

/* Helper function to write vertex positions into vertex buffer. */
static inline void writeVertex(MY_V3F& v, float x, float y)
{
    v.x = x;
    v.y = y;
    v.z = 0.0f;
}

static HRESULT initVertexBuffer(IDirect3DDevice9* pDev,
    int count, const ShapeList* list)
{
    int vertexCount = count * 6;

    if (FAILED(pDev->CreateVertexBuffer(vertexCount * sizeof(MY_V3F),
        0, D3DFVF_XYZ,
        D3DPOOL_DEFAULT,
        &myVertexBuffer, NULL))) {
        return E_FAIL;
    }

    VOID* pVertices;
    if (FAILED(myVertexBuffer->Lock(0, 0, /* map entire buffer */
        &pVertices, 0))) {
        return E_FAIL;
    }

    MY_V3F* rectVertices = (MY_V3F*)pVertices;
    for (int i = 0, n = 0; i < count; i++) {
        const float x = list[i].x;
        const float y = list[i].y;
        const float width = list[i].width;
        const float height = list[i].height;

        // Compute the four corners of the rectangle
        float left = x - width / 2;
        float right = x + width / 2;
        float top = y + height / 2;
        float bottom = y - height / 2;

        // First triangle
        writeVertex(rectVertices[n++], left, top);       // Top left
        writeVertex(rectVertices[n++], right, top);      // Top right
        writeVertex(rectVertices[n++], right, bottom);   // Bottom right

        // Second triangle
        writeVertex(rectVertices[n++], left, top);       // Top left
        writeVertex(rectVertices[n++], right, bottom);   // Bottom right
        writeVertex(rectVertices[n++], left, bottom);    // Bottom left
    }

    myVertexBuffer->Unlock();

    return S_OK;
}

static HRESULT CALLBACK OnResetDevice(IDirect3DDevice9* pDev,
    const D3DSURFACE_DESC* backBuf,
    void* userContext)
{
    cgD3D9SetDevice(pDev);
    checkForCgError("setting Direct3D device");

    static int firstTime = 1;
    if (firstTime) {
        /* Cg runtime resources such as CGprogram and CGparameter handles
           survive a device reset so we just need to compile a Cg program
           just once.  We do however need to unload Cg programs with
           cgD3DUnloadProgram upon when a Direct3D device is lost and load
           Cg programs every Direct3D device reset with cgD3D9UnloadProgram. */
        createCgPrograms();
        firstTime = 0;
    }

    /* false below means "with parameter shadowing" */
    cgD3D9LoadProgram(myCgVertexProgram, false, 0);
    checkForCgError("loading vertex program");

    /* false below means "with parameter shadowing" */
    cgD3D9LoadProgram(myCgFragmentProgram, false, 0);
    checkForCgError("loading fragment program");

    return initVertexBuffer(pDev, myShapeCount, myShapeList);
}

static void CALLBACK OnFrameRender(IDirect3DDevice9* pDev,
    double time,
    float elapsedTime,
    void* userContext)
{
    pDev->Clear(0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER,
        D3DXCOLOR(0.1f, 0.3f, 0.6f, 0.0f), 1.0f, 0);

    if (SUCCEEDED(pDev->BeginScene())) {
        cgD3D9BindProgram(myCgVertexProgram);
        checkForCgError("binding vertex program");

        cgD3D9BindProgram(myCgFragmentProgram);
        checkForCgError("binding fragment program");

        /* Render the rectangles. */
        pDev->SetStreamSource(0, myVertexBuffer, 0, sizeof(MY_V3F));
        pDev->SetFVF(D3DFVF_XYZ);
        for (int i = 0; i < myShapeCount; i++) {
            pDev->DrawPrimitive(D3DPT_TRIANGLELIST, i * 6, 2);
        }

        pDev->EndScene();
    }
}

static void CALLBACK OnLostDevice(void* userContext)
{
    if (myVertexBuffer) {
        myVertexBuffer->Release();
        myVertexBuffer = NULL;
    }
    cgD3D9SetDevice(NULL);
}
