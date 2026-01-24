import numpy as np
import matplotlib.pyplot as plt

# ----------------- исходные данные -----------------
N_points = 100000
C = np.array([0.5, -0.2, 0.3], dtype=float)     # точка C
N_vec = np.array([0.2, 0.6, 0.76], dtype=float) # ось N (не обязана быть единичной)
R = 1.0                                         # радиус сферы

# (опционально) фиксируем seed для повторяемости
# np.random.seed(42)

def normalize(v, eps=1e-12):
    v = np.asarray(v, dtype=float)
    n = np.linalg.norm(v)
    if n < eps:
        raise ValueError("Zero length vector")
    return v / n

Nn = normalize(N_vec)

# ----------------- ортонормальный базис вокруг N -----------------
# строим два вектора T,B такие, что {T, B, Nn} — ортонормальный базис
if abs(Nn[2]) < 0.9:
    a = np.array([0.0, 0.0, 1.0])
else:
    a = np.array([1.0, 0.0, 0.0])

T = normalize(np.cross(a, Nn))
B = np.cross(Nn, T)  # уже нормированный из-за ортонормальности

# ----------------- cosine-weighted sampling (полусфера) -----------------
# Это стандартное распределение: p(ω) = (cos α)/π на полусфере u·N >= 0
u1 = np.random.rand(N_points)
u2 = np.random.rand(N_points)

r = np.sqrt(u1)
phi = 2*np.pi*u2

x = r * np.cos(phi)
y = r * np.sin(phi)
z = np.sqrt(1.0 - u1)          # z = cos(alpha) в локальных координатах

# локальный вектор (x,y,z) -> мировой u = x*T + y*B + z*Nn
U = (x[:, None] * T[None, :]
   + y[:, None] * B[None, :]
   + z[:, None] * Nn[None, :])

# на всякий случай нормируем (должны уже быть единичные)
U = U / np.linalg.norm(U, axis=1)[:, None]

# ----------------- строим точки P на сфере, чтобы |P-C| ∝ cos(alpha) -----------------
# Геометрия: центр сферы O = C + R*Nn (тогда C — "южный полюс" этой сферы)
O = C + R * Nn

cos_alpha = U @ Nn                 # должно быть в [0,1]
t = 2.0 * R * cos_alpha            # это |P - C| (длина хорды), t ∈ [0, 2R]
P = C + t[:, None] * U             # пересечение луча с сферой (кроме t=0)

# ----------------- диагностика -----------------
# 1) Проверка, что P действительно на сфере радиуса R вокруг O
dist_to_center = np.linalg.norm(P - O, axis=1)
print("Distance to sphere center O: mean, min, max:",
      dist_to_center.mean(), dist_to_center.min(), dist_to_center.max())

# 2) Проверка диапазона cos_alpha
print("cos(alpha)=U·N: min, max, mean:", cos_alpha.min(), cos_alpha.max(), cos_alpha.mean())

# Теория:
# Для cosine-weighted hemisphere маргинальная плотность по c=cos(alpha) равна f(c)=2c на [0,1]
# Значит на гистограмме будет: около 0 мало, к 1 много.
# А для t=2Rc: плотность f(t)= t/(2R^2) на [0,2R] (линейно растущая).

# ----------------- графики -----------------
subset = np.random.choice(N_points, size=1500, replace=False)

fig = plt.figure(figsize=(13, 5))

# 3D: точки P на сфере
ax1 = fig.add_subplot(1, 2, 1, projection="3d")
ax1.scatter(P[subset, 0], P[subset, 1], P[subset, 2], s=5)
ax1.scatter(C[0], C[1], C[2], s=80, color="red", label="C")
ax1.scatter(O[0], O[1], O[2], s=40, color="black", label="O (center)")
ax1.quiver(C[0], C[1], C[2], Nn[0], Nn[1], Nn[2], length=1.3*R, color="green", label="N axis")
ax1.set_title("Подмножество точек P на сфере (cosine-weighted направления)")
ax1.set_box_aspect([1, 1, 1])
ax1.legend()

# Гистограмма cos(alpha) и теоретическая плотность 2c
ax2 = fig.add_subplot(1, 2, 2)
bins = 60
ax2.hist(cos_alpha, bins=bins, range=(0, 1), density=True, alpha=0.7, label="empirical")

c_grid = np.linspace(0, 1, 400)
ax2.plot(c_grid, 2*c_grid, linewidth=2, label="theory: f(c)=2c")

ax2.set_title("cos(alpha)=U·N")
ax2.set_xlabel("c = cos(alpha)")
ax2.set_ylabel("density")
ax2.grid(True)
ax2.legend()

plt.tight_layout()
plt.show()

# Доп. проверка (не обязательно): распределение t = |P-C|
fig2 = plt.figure(figsize=(13, 5))

ax3 = fig2.add_subplot(1, 2, 1)
ax3.hist(t, bins=60, range=(0, 2*R), density=True, alpha=0.7, label="empirical")

t_grid = np.linspace(0, 2*R, 400)
ax3.plot(t_grid, t_grid/(2*R*R), linewidth=2, label="theory: f(t)=t/(2R^2)")

ax3.set_title("t = |P-C|: плотность линейно растёт")
ax3.set_xlabel("t")
ax3.set_ylabel("density")
ax3.grid(True)
ax3.legend()

# Проекция направлений на плоскость, ортогональную N (должна быть равномерной в диске)
# (для cosine-weighted направления это ключевое свойство)
proj_x = U @ T
proj_y = U @ B
ax4 = fig2.add_subplot(1, 2, 2)
ax4.scatter(proj_x[subset], proj_y[subset], s=5)

circle = plt.Circle((0, 0), 1.0, edgecolor="r", facecolor="none")
ax4.add_patch(circle)
ax4.set_aspect("equal", "box")
ax4.set_xlim(-1.05, 1.05)
ax4.set_ylim(-1.05, 1.05)
ax4.set_title("Проекция направлений на плоскость ⟂ N (равномерно в диске)")
ax4.grid(True)

plt.tight_layout()
plt.show()
