import numpy as np
import matplotlib.pyplot as plt

# --------------------------------------
# Треугольник в пространстве (любые координаты)
# --------------------------------------
V1 = np.array([0, 0, 0])
V2 = np.array([2, 1, 0.5])
V3 = np.array([-1, 2, 1])

N = 1000000   # число точек

# --------------------------------------
# Функция генерации равномерных точек внутри треугольника
# Метод: u,v — независимые равномерные, но если u+v>1 – отражаем
# --------------------------------------
def sample_triangle(V1, V2, V3, n):
    u = np.random.rand(n)
    v = np.random.rand(n)

    # Отражение для равномерности
    mask = u + v > 1
    u[mask] = 1 - u[mask]
    v[mask] = 1 - v[mask]

    # Генерация точек
    return V1 + u[:,None]*(V2 - V1) + v[:,None]*(V3 - V1)

# Сгенерируем точки
points = sample_triangle(V1, V2, V3, N)

# --------------------------------------
# Чтобы визуализировать распределение, надо перевести точки в 2D
# (система координат треугольника)
# --------------------------------------
e1 = V2 - V1
e2 = V3 - V1

# базис (ортонормируем)
e1n = e1 / np.linalg.norm(e1)
e2p = e2 - np.dot(e2, e1n) * e1n
e2n = e2p / np.linalg.norm(e2p)

# Проекция на 2D
proj_x = np.dot(points - V1, e1n)
proj_y = np.dot(points - V1, e2n)

# --------------------------------------
# Визуализация тепловой карты распределения
# --------------------------------------
plt.figure(figsize=(6, 6))
plt.hist2d(proj_x, proj_y, bins=100, density=True)
plt.colorbar(label="Плотность")
plt.title("Тепловая карта плотности точек")
plt.xlabel("X (local)")
plt.ylabel("Y (local)")
plt.show()

# --------------------------------------
# Покажем треугольник и первые 500 точек
# --------------------------------------
tri_proj = np.array([
    [np.dot(V1 - V1, e1n), np.dot(V1 - V1, e2n)],
    [np.dot(V2 - V1, e1n), np.dot(V2 - V1, e2n)],
    [np.dot(V3 - V1, e1n), np.dot(V3 - V1, e2n)],
])

plt.figure(figsize=(6, 6))
plt.fill(tri_proj[:,0], tri_proj[:,1], edgecolor='black', fill=False, linewidth=2)

plt.scatter(proj_x[:500], proj_y[:500], s=10, color='red')
plt.title("Треугольник и первые 50 точек")
plt.xlabel("X (local)")
plt.ylabel("Y (local)")
plt.gca().set_aspect('equal')
plt.show()
