import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D


# 1. Генерация равномерных направлений на сфере
# ==============================================
N = 100000

# cos(theta) равномерно в [-1, 1]
u = np.random.uniform(-1, 1, N)

# phi равномерно в [0, 2π]
phi = np.random.uniform(0, 2*np.pi, N)

theta = np.arccos(u)

# Декартовы координаты (точки на сфере)
x = np.sin(theta) * np.cos(phi)
y = np.sin(theta) * np.sin(phi)
z = np.cos(theta)

points = np.vstack((x, y, z)).T  # shape = (100000, 3)

# 2. Проверка — все точки на сфере
# =========================================================
r = np.sqrt(x*x + y*y + z*z)
print("Среднее отклонение радиуса от 1:", np.mean(np.abs(r - 1)))

# 3. Проверка равномерности распределения
#    В идеальном случае гистограмма Z должна быть ровной
# =========================================================

plt.figure(figsize=(8, 4))
plt.hist(z, bins=40, density=True, color='royalblue')
plt.title("Гистограмма распределения z = cos(theta)")
plt.xlabel("z")
plt.ylabel("Плотность")
plt.grid(True)

# 4. 3D визуализация облака точек
# =========================================================
fig = plt.figure(figsize=(6, 6))
ax = fig.add_subplot(111, projection='3d')

# отображаем только часть (иначе слишком густо)
sample = points[:5000]

ax.scatter(sample[:, 0], sample[:, 1], sample[:, 2], s=2)
ax.set_title("Равномерные точки на сфере (5000 из 100000)")
ax.set_box_aspect([1, 1, 1])
ax.set_xlabel("X")
ax.set_ylabel("Y")
ax.set_zlabel("Z")

plt.show()
