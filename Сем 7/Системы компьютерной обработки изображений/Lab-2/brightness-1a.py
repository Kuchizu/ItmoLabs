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

V = np.array([1.1, 1.2, 1.5])   # точка наблюдения

# координаты углов треугольника
P0 = np.array([0.0, 0.0, 0.0])
P1 = np.array([1.0, 0.0, 0.4])
P2 = np.array([0.0, 1.0, 0.0])

# точка на плоскости треугольника
x_local = 0.25
y_local = 0.35

# -----------------------
# Вычисления
# -----------------------
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
cos_theta2 = np.clip(cos_theta1, -1.0, 1.0)  # защитный зажим

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

if Lrgb < 0:
    Lrgb = 0

print("E1 =", E1)
print("E2 =", E2)
print("diff1 =", diff1, "spec1 =", spec1)
print("diff2 =", diff2, "spec2 =", spec2)
print("Lrgb =", Lrgb)

# -----------------------
# ВИЗУАЛИЗАЦИЯ
# -----------------------
fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

# треугольник
triangle = np.array([P0, P1, P2, P0])
ax.plot(triangle[:,0], triangle[:,1], triangle[:,2], 'b-', label='Triangle')

# точки вершин
ax.scatter(P0[0], P0[1], P0[2], color='blue', s=50)
ax.text(P0[0], P0[1], P0[2], "P0", color='blue')

ax.scatter(P1[0], P1[1], P1[2], color='blue', s=50)
ax.text(P1[0], P1[1], P1[2], "P1", color='blue')

ax.scatter(P2[0], P2[1], P2[2], color='blue', s=50)
ax.text(P2[0], P2[1], P2[2], "P2", color='blue')

# точка Pt
ax.scatter(Pt[0], Pt[1], Pt[2], color='red', s=60)
ax.text(Pt[0], Pt[1], Pt[2], "Pt", color='red')

# источник света 1
ax.scatter(Pl1[0], Pl1[1], Pl1[2], color='yellow', s=80)
ax.text(Pl1[0], Pl1[1], Pl1[2], "Pl1", color='orange')

# направление света 1
ax.quiver(Pl1[0], Pl1[1], Pl1[2],
          O1_vec[0], O1_vec[1], O1_vec[2],
          length=0.5, color='orange', label='Light axis')

# луч Pl → Pt
ax.plot([Pl1[0], Pt[0]], [Pl1[1], Pt[1]], [Pl1[2], Pt[2]], 'r--')

# источник света 2
ax.scatter(Pl2[0], Pl2[1], Pl2[2], color='yellow', s=80)
ax.text(Pl2[0], Pl2[1], Pl2[2], "Pl2", color='orange')

# направление света 2
ax.quiver(Pl2[0], Pl2[1], Pl2[2],
          O2_vec[0], O2_vec[1], O2_vec[2],
          length=0.5, color='cyan', label='Light2 axis')

# луч Pl2 → Pt
ax.plot([Pl2[0], Pt[0]], [Pl2[1], Pt[1]], [Pl2[2], Pt[2]], 'c--')

# наблюдатель
ax.scatter(V[0], V[1], V[2], color='purple', s=80)
ax.text(V[0], V[1], V[2], "V", color='purple')

# нормаль к треугольнику (из P0)
ax.quiver(P0[0], P0[1], P0[2],
          N[0], N[1], N[2],
          length=0.5, color='green', label='Normal')

# оформление
ax.set_xlabel('X')
ax.set_ylabel('Y')
ax.set_zlabel('Z')
ax.legend()

ax.set_box_aspect([1,1,1])
plt.show()
