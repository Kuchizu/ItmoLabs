import numpy as np
import matplotlib.pyplot as plt

# Исходные параметры
C = np.array([1.0, 2.0, 0.5])   # центр окружности в 3D
N_vec = np.array([0.3, -0.6, 0.74])  # вектор нормали (ориентация)
Rc = 1.25                        # радиус
N_points = 100000   # количество точек

def normalize(v, eps=1e-12):
    n = np.linalg.norm(v)
    if n < eps:
        raise ValueError("Вектор нулевой длины")
    return v / n

# Рассчитываем ортогональный базис (u_axis, v_axis) для плоскости круга
Nn = normalize(N_vec)
if abs(Nn[0]) < 0.9:
    tmp = np.array([1.0, 0.0, 0.0])
else:
    tmp = np.array([0.0, 1.0, 0.0])
u_axis = normalize(np.cross(Nn, tmp))
v_axis = normalize(np.cross(Nn, u_axis))

# Генерируем равномерно распределенные точки внутри диска
u = np.random.rand(N_points)
theta = 2 * np.pi * np.random.rand(N_points)
r = Rc * np.sqrt(u)
x_local = r * np.cos(theta)
y_local = r * np.sin(theta)

points = C[None, :] + np.outer(x_local, u_axis) + np.outer(y_local, v_axis)

# Проверка расстояний
rel = points - C[None, :]
proj_x = np.dot(rel, u_axis)
proj_y = np.dot(rel, v_axis)
radial = np.sqrt(proj_x**2 + proj_y**2)

print("Points generated:", N_points)
print("Radial distances: min =", radial.min(), "max =", radial.max())
print("Within Rc tolerance? ", np.all(radial <= Rc + 1e-12))

# Проверка равномерности по кольцам
n_bins = 50
bins = np.linspace(0, Rc, n_bins+1)
counts, _ = np.histogram(radial, bins=bins)
areas = np.pi * (bins[1:]**2 - bins[:-1]**2)
density = counts / areas

print("\nРезультат проверки статистической равномерности:")
print("min:", counts.min(), "max:", counts.max(), "mean:", counts.mean())
print("статистика плотностей:")
print("среднее =", density.mean())
print("стандартное отклонение =", density.std())

# отобразим 300 точек
subset_idx = np.random.choice(N_points, size=300, replace=False)
pts3 = points[subset_idx]

# ---------------- FIGURE 1 ----------------
fig = plt.figure(figsize=(12, 6))

ax1 = fig.add_subplot(1, 2, 1, projection='3d')
ax1.scatter(pts3[:,0], pts3[:,1], pts3[:,2], s=8)

angles = np.linspace(0, 2*np.pi, 200)
rim = C[None,:] + np.outer(Rc*np.cos(angles), u_axis) + np.outer(Rc*np.sin(angles), v_axis)
ax1.plot(rim[:,0], rim[:,1], rim[:,2], 'r-', linewidth=1.2)
ax1.set_title("Подмножество точек и окружность (3D)")
ax1.set_xlabel('X'); ax1.set_ylabel('Y'); ax1.set_zlabel('Z')
ax1.view_init(elev=25, azim=30)

ax2 = fig.add_subplot(1, 2, 2)
ax2.scatter(proj_x[subset_idx], proj_y[subset_idx], s=8)
circle = plt.Circle((0,0), Rc, edgecolor='r', facecolor='none')
ax2.add_patch(circle)
ax2.set_aspect('equal', 'box')
ax2.set_xlim(-Rc*1.1, Rc*1.1); ax2.set_ylim(-Rc*1.1, Rc*1.1)
ax2.set_title("Проекция на плоскость окружности (локальные координаты)")

plt.tight_layout()

# ---------------- FIGURE 2 ----------------
fig2 = plt.figure(figsize=(12, 6))

# плотность по радиусу
ax3 = fig2.add_subplot(1, 2, 1)
bin_centers = 0.5*(bins[:-1] + bins[1:])
ax3.plot(bin_centers, density, marker='o', linestyle='-')
ax3.set_xlabel('радиус')
ax3.set_ylabel('точек на единицу площади')
ax3.set_title('Эмпирическая плотность вдоль радиуса (должна быть постоянной)')
ax3.grid(True)
ax3.set_ylim(10000, max(density)*1.05)

# pdf(r) - функция плотности вероятности
ax4 = fig2.add_subplot(1, 2, 2)
hist_counts, hist_bins = np.histogram(radial, bins=100, density=True)
bin_centers2 = 0.5*(hist_bins[:-1] + hist_bins[1:])
theo = 2*bin_centers2/(Rc**2)
ax4.plot(bin_centers2, hist_counts, label='эмпирическая pdf(r)')
ax4.plot(bin_centers2, theo, '--', label='теоретическая pdf 2r/Rc^2')
ax4.set_xlabel('r')
ax4.set_ylabel('pdf')
ax4.set_title('Радиальное распределение (эмпирическое-теоретическое)')
ax4.legend()
ax4.grid(True)


plt.tight_layout()
plt.show()

# Вывод результатов
print("\n Контрольные проверки:")
print("- максимальный радиус <= Rc:", radial.max() <= Rc + 1e-12)
print("- коэффициент вариации:", density.std() / density.mean())
