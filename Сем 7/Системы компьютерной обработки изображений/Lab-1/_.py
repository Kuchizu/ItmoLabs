import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

def normalize(v):
    n = np.linalg.norm(v)
    return v / n if n != 0 else v

# Исходные данные
Pl = np.array([0.0, 0.0, 1.0])      # источник
O_point = np.array([-1.0, 0.0, 1.0]) # направление излучения

P0 = np.array([0.0, 0.0, 0.0])
P1 = np.array([1.0, 0.0, 0.0])
P2 = np.array([0.0, 1.0, 0.0])

# точка на плоскости треугольника
x_local = 1.0
y_local = 0.0

# -----------------------
# Вычисления
# -----------------------
# глобальные координаты заданной точки
u01 = normalize(P1 - P0)
u02 = normalize(P2 - P0)
Pt = P0 + u01 * x_local + u02 * y_local  # глобальная точка

# нормаль треугольника
N = normalize(np.cross(P1 - P0, P2 - P0))

# направление источника
O_vec = normalize(O_point - Pl)

# луч к точке
s = Pt - Pl
s_norm = normalize(s)
R = np.linalg.norm(s) # длина луча

# косинус угла между ними
cos_theta = np.dot(s_norm, O_vec)
cos_theta = np.clip(cos_theta, -1.0, 1.0)  # защитный зажим

cos_alpha = np.dot(-s_norm, N)
cos_alpha = np.clip(cos_alpha, -1.0, 1.0)  # защитный зажим

# рассчитываем освещенность
E = cos_theta * cos_alpha / (R * R)

print("cos_theta =", cos_theta)
print("cos_alpha =", cos_alpha)
print("R =", R)
print("E =",  E)

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
ax.scatter(P1[0], P1[1], P1[2], color='blue', s=50)
ax.scatter(P2[0], P2[1], P2[2], color='blue', s=50)

# точка Pt
ax.scatter(Pt[0], Pt[1], Pt[2], color='red', s=60, label='Pt')

# источник света
ax.scatter(Pl[0], Pl[1], Pl[2], color='yellow', s=80, label='Light')

# нормаль к треугольнику (из P0)
ax.quiver(P0[0], P0[1], P0[2],
          N[0], N[1], N[2],
          length=0.5, color='green', label='Normal')

# ось источника
ax.quiver(Pl[0], Pl[1], Pl[2],
          O_vec[0], O_vec[1], O_vec[2],
          length=0.5, color='orange', label='Light axis')

# луч от источника к точке Pt
ax.plot([Pl[0], Pt[0]], [Pl[1], Pt[1]], [Pl[2], Pt[2]], 'r--', label='Light ray')

# оформление
ax.set_xlabel('X')
ax.set_ylabel('Y')
ax.set_zlabel('Z')
ax.legend()

ax.set_box_aspect([1,1,1])
plt.show()
