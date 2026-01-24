import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

# вспомогательная фйнкция нормализации вектора
def normalize(v):
    n = np.linalg.norm(v)
    return v / n if n != 0 else v

# Исходные данные
Pl1 = np.array([0.0, 0.0, 1.0])     # источник 1
Pl2 = np.array([0.0, 1.0, 0.7])     # источник 2

O1_point = np.array([0.5, 0.5, 0.7]) # направление излучения Pl1
O2_point = np.array([0.7, 0.7, 0.5]) # направление излучения Pl2

V = np.array([1.1, 1.2, -1.5])   # точка наблюдения

# координаты углов треугольника
P0 = np.array([0.0, 0.0, 0.0])
P1 = np.array([1.0, 0.0, 0.4])
P2 = np.array([0.0, 1.0, 0.0])

def brightness(x_local, y_local):
    # глобальные координаты заданной точки
    u01 = normalize(P1 - P0)
    u02 = normalize(P2 - P0)
    Pt = P0 + u01 * x_local + u02 * y_local  # глобальная точка

    # нормаль треугольника
    N = normalize(np.cross(P1 - P0, P2 - P0))

    # направления источников
    O1_vec = normalize(O1_point - Pl1)
    O2_vec = normalize(O2_point - Pl2)

    # луч от точки Pl1 к точке Pt
    s1 = Pt - Pl1
    s1_norm = normalize(s1)
    R1 = np.linalg.norm(s1) # длина луча от источника Pl1

    # луч от точки Pl2 к точке Pt
    s2 = Pt - Pl2
    s2_norm = normalize(s2)
    R2 = np.linalg.norm(s2) # длина луча от источника Pl1

    # косинусы углов между лучами
    cos_theta1 = np.dot(s1_norm, O1_vec)
    cos_theta1 = np.clip(cos_theta1, -1.0, 1.0)  # защитный зажим

    cos_alpha1 = np.dot(-s1_norm, N) 
    cos_alpha1 = np.clip(cos_alpha1, -1.0, 1.0)  # защитный зажим

    cos_theta2 = np.dot(s2_norm, O2_vec)
    cos_theta2 = np.clip(cos_theta2, -1.0, 1.0)  # защитный зажим

    cos_alpha2 = np.dot(-s2_norm, N) 
    cos_alpha2 = np.clip(cos_alpha2, -1.0, 1.0)  # защитный зажим

    # рассчитываем освещенность от разных источников
    E1 = cos_theta1 * cos_alpha1 / (R1 * R1)
    E2 = cos_theta2 * cos_alpha2 / (R2 * R2)

    # расчет отражения
    # -----------------------
    Kd = 0.1 # коэффициент диффузного отражения поверхности
    Ks = 0.2 # коэффициент «зеркальности» поверхности
    Ke = 10.0 # коэффициент, определяющий ширину блика (чем больше — тем уже блик)
    Krgb = np.array([0.9, 1.0, 0.8]) # «цвет» поверхности

    # направления
    V_vec = normalize(V - Pt)

    # half-векторы
    H1 = normalize(O1_vec + V_vec)
    H2 = normalize(O2_vec + V_vec)

    # diffuse компонента
    diff1 = Kd * cos_alpha1
    diff2 = Kd * cos_alpha2

    # specular компонента
    spec1 = Ks * max(np.dot(N, H1), 0)**Ke
    spec2 = Ks * max(np.dot(N, H2), 0)**Ke

    # итоговая BRDF для каждого источника
    BRDF1 = diff1 + spec1
    BRDF2 = diff2 + spec2

    # итоговая яркость точки
    Lrgb = Krgb * (E1 * BRDF1 + E2 * BRDF2)
    print(f"{x_local:3.1f} {y_local:3.1f}")
    print(f"E1={E1:6.4f} E2={E2:6.4f} "
      f"Lrgb=({Lrgb[0]:.3f}, {Lrgb[1]:.3f}, {Lrgb[2]:.3f})\n")
    #print("E1 =", E1, "E2 =", E2, "Lrgb =", Lrgb)

# диапазон значений
vals = np.arange(0.1, 0.6, 0.1)

for x_local in vals:
    for y_local in vals:
        E = brightness(x_local, y_local)


