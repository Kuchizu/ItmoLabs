///////////////////////////////////////////////
// Глобальные переменные
///////////////////////////////////////////////
float4x4 gWorld           : WORLD;
float4x4 gView            : VIEW;
float4x4 gProjection      : PROJECTION;

// Дополнительные матрицы
float4x4 gMatrixA         : MAT_A < string UIName = "Matrix A" >;
float4x4 gMatrixB         : MAT_B < string UIName = "Matrix B" >;
float4x4 gMatrixC         : MAT_C < string UIName = "Matrix C" >;

///////////////////////////////////////////////
// Структуры вершинного шейдера
///////////////////////////////////////////////
struct VS_INPUT
{
    float4 Position : POSITION;
    float2 TexCoord : TEXCOORD0;
};

struct VS_OUTPUT
{
    float4 Position        : POSITION;
    float4 Intermediate1   : TEXCOORD1;
    float4 Intermediate2   : TEXCOORD2;
    float4 Intermediate3   : TEXCOORD3;
    float2 TexCoord        : TEXCOORD0;
};

///////////////////////////////////////////////
// Вершинный шейдер
///////////////////////////////////////////////
VS_OUTPUT vsMain(VS_INPUT IN)
{
    VS_OUTPUT OUT = (VS_OUTPUT)0;

    // Исходная позиция
    float4 pos = IN.Position;

    // 1) Умножаем на матрицу A
    float4 afterA = mul(pos, gMatrixA);
    // 2) На матрицу B
    float4 afterB = mul(afterA, gMatrixB);
    // 3) На матрицу C
    float4 afterC = mul(afterB, gMatrixC);

    // Сохраняем промежуточные данные в TEXCOORD1..3
    OUT.Intermediate1 = afterA;
    OUT.Intermediate2 = afterB;
    OUT.Intermediate3 = afterC;

    // Итоговая матрица: World * View * Projection
    float4x4 WVP = mul(mul(gWorld, gView), gProjection);
    OUT.Position = mul(afterC, WVP);

    // Пробросим TexCoord (если нужно для пикс. шейдера)
    OUT.TexCoord = IN.TexCoord;
    return OUT;
}

///////////////////////////////////////////////
// Пиксельный (фрагментный) шейдер
///////////////////////////////////////////////
float4 psMain(VS_OUTPUT IN) : COLOR
{
    // Просто рисуем белым цветом
    return float4(1,1,1,1);
}

///////////////////////////////////////////////
// Техника рендеринга
///////////////////////////////////////////////
technique MyTechnique
{
    pass P0
    {
        VertexShader = compile vs_2_0 vsMain();
        PixelShader  = compile ps_2_0 psMain();
    }
}
