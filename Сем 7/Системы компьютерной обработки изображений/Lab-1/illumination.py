import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def calculate_color_intensity(I0_RGB, direction_vector, cos_theta):
    """
    Вычисляет интенсивность света с учетом угла.
    Чем больше угол между направлением света и осью источника, тем меньше интенсивность.
    """
    return I0_RGB * cos_theta

def calculate_point_illumination(I_RGB_s, R):
    """
    Вычисляет освещенность точки по закону обратных квадратов.
    Чем дальше точка от источника, тем меньше освещенность (делим на R²).
    """
    return I_RGB_s / (R ** 2)

def calculate_triangle_normal(P0, P1, P2):
    """
    Вычисляет нормаль (перпендикуляр) к плоскости треугольника.
    Нормаль показывает, куда смотрит поверхность треугольника.
    Возвращает norm вектор len 1.
    """
    edge1 = P1 - P0  # Первое ребро треугольника
    edge2 = P2 - P0  # Второе ребро треугольника
    normal = np.cross(edge1, edge2)
    return normal / np.linalg.norm(normal)

def convert_local_to_global(P0, P1, P2, x, y):
    """
    Переводит локальные координаты (x, y) в глобальные 3D координаты.
    Локальные координаты - это координаты на плоскости треугольника (0-1).
    Глобальные координаты - это реальные 3D координаты в пространстве.
    """

    edge1 = P1 - P0
    edge2 = P2 - P0
    P_T = P0 + (edge1 / np.linalg.norm(edge1)) * x + (edge2 / np.linalg.norm(edge2)) * y
    return P_T

def calculate_illumination_at_point(I0_RGB, direction_O, P_L, P_T, normal_N):
    """
    Находим вектор от точки до источника света (s)
    Нормализуем его (чтобы знать только направление)
    Вычисляем углы (cos α и cos θ)
    Если углы правильные (> 0), вычисляем освещенность
    Если углы неправильные (<= 0), возвращаем 0 (свет не попадает)
    """

    s_vector = P_L - P_T
    R = np.linalg.norm(s_vector)

    if R < 1e-10:
        return np.array([0.0, 0.0, 0.0])

    s_normalized = s_vector / R # Направление only
    cos_alpha = np.dot(s_normalized, normal_N)
    cos_theta = np.dot(s_normalized, -direction_O)

    if cos_alpha <= 0 or cos_theta <= 0:  # Если свет не попадает на поверхность
        return np.array([0.0, 0.0, 0.0])

    I_RGB_s = calculate_color_intensity(I0_RGB, s_normalized, cos_theta)  # Интенсивность
    E_RGB = calculate_point_illumination(I_RGB_s * cos_alpha, R)  # Освещенность

    return E_RGB

def generate_grid_points(num_x=5, num_y=5, max_x=1.0, max_y=1.0):
    x_values = np.linspace(0, max_x, num_x)
    y_values = np.linspace(0, max_y, num_y)
    return x_values, y_values

def calculate_illumination_grid(I0_RGB, direction_O, P_L, P0, P1, P2, x_values, y_values):
    normal_N = calculate_triangle_normal(P0, P1, P2)

    results = []
    for y in y_values:
        row = []
        for x in x_values:
            P_T = convert_local_to_global(P0, P1, P2, x, y)
            E_RGB = calculate_illumination_at_point(I0_RGB, direction_O, P_L, P_T, normal_N)
            row.append(E_RGB)
        results.append(row)

    return np.array(results), normal_N

def visualize_example_points(I0_RGB, direction_O, P_L, P0, P1, P2, normal_N):
    example_points = [
        (0.0, 0.0, "P₀ (вершина)", 'red'),
        (1.5, 1.25, "центр", 'orange'),
        (3.0, 0.0, "P₀-P₁", 'magenta')
    ]

    fig = plt.figure(figsize=(18, 6))
    fig.canvas.manager.set_window_title('Примеры расчетов освещенности')

    for idx, (x_local, y_local, description, color) in enumerate(example_points):
        ax = fig.add_subplot(1, 3, idx + 1, projection='3d')

        triangle = np.array([P0, P1, P2])
        poly = Poly3DCollection([triangle], alpha=0.3, facecolor='lightblue', edgecolor='black', linewidths=2)
        ax.add_collection3d(poly)

        ax.scatter(*P0, color='blue', s=80, marker='o', alpha=0.5)
        ax.scatter(*P1, color='blue', s=80, marker='o', alpha=0.5)
        ax.scatter(*P2, color='blue', s=80, marker='o', alpha=0.5)

        P_T = convert_local_to_global(P0, P1, P2, x_local, y_local)
        ax.scatter(*P_T, color=color, s=200, marker='o', label=f'Pₜ ({description})', edgecolors='black', linewidths=2)
        ax.scatter(*P_L, color='yellow', s=250, marker='*', label='Pₗ (light)', edgecolors='orange', linewidths=2)

        s_vector = P_L - P_T
        ax.quiver(P_T[0], P_T[1], P_T[2], s_vector[0], s_vector[1], s_vector[2],
                  color='purple', arrow_length_ratio=0.1, linewidth=2, label='s (to light)')
        ax.quiver(P_T[0], P_T[1], P_T[2], normal_N[0], normal_N[1], normal_N[2],
                  color='red', arrow_length_ratio=0.3, linewidth=2, label='N (normal)')
        ax.quiver(P_L[0], P_L[1], P_L[2], direction_O[0] * 0.8, direction_O[1] * 0.8, direction_O[2] * 0.8,
                  color='orange', arrow_length_ratio=0.3, linewidth=2, label='O (dir)', alpha=0.7)

        R = np.linalg.norm(s_vector)
        s_normalized = s_vector / R
        cos_alpha = np.dot(s_normalized, normal_N)
        cos_theta = np.dot(s_normalized, -direction_O)
        I_RGB_s = I0_RGB * cos_theta
        E_RGB = I_RGB_s * cos_alpha / (R ** 2)

        ax.set_xlabel('X', fontsize=10)
        ax.set_ylabel('Y', fontsize=10)
        ax.set_zlabel('Z', fontsize=10)
        ax.set_title(f'{description}\nR={R:.2f}, cos(θ)={cos_theta:.3f}, cos(α)={cos_alpha:.3f}\nE(R,G,B)=({E_RGB[0]:.2f}, {E_RGB[1]:.2f}, {E_RGB[2]:.2f})',
                     fontsize=11, fontweight='bold')
        ax.legend(loc='upper left', fontsize=8)

        all_points = np.vstack([P0, P1, P2, P_L])
        max_range = np.array([all_points[:, 0].max() - all_points[:, 0].min(),
                              all_points[:, 1].max() - all_points[:, 1].min(),
                              all_points[:, 2].max() - all_points[:, 2].min()]).max() / 2.0
        mid_x = (all_points[:, 0].max() + all_points[:, 0].min()) * 0.5
        mid_y = (all_points[:, 1].max() + all_points[:, 1].min()) * 0.5
        mid_z = (all_points[:, 2].max() + all_points[:, 2].min()) * 0.5
        ax.set_xlim(mid_x - max_range, mid_x + max_range)
        ax.set_ylim(mid_y - max_range, mid_y + max_range)
        ax.set_zlim(mid_z - max_range, mid_z + max_range)

    plt.tight_layout()

def visualize_illumination_heatmap(I0_RGB, direction_O, P_L, P0, P1, P2, normal_N, results, x_values, y_values):
    fig = plt.figure(figsize=(15, 5))
    fig.canvas.manager.set_window_title('Тепловая карта освещенности')

    channels = ['Red', 'Green', 'Blue']
    channel_idx = [0, 1, 2]

    for idx, (channel, ch_idx) in enumerate(zip(channels, channel_idx)):
        ax = fig.add_subplot(1, 3, idx + 1)

        illumination_data = results[:, :, ch_idx]

        im = ax.imshow(illumination_data, cmap='hot', interpolation='bilinear', origin='lower',
                       extent=[x_values[0], x_values[-1], y_values[0], y_values[-1]], aspect='auto')

        contours = ax.contour(x_values, y_values, illumination_data, levels=8, colors='white', alpha=0.4, linewidths=0.5)
        ax.clabel(contours, inline=True, fontsize=8, fmt='%.2f')

        ax.set_xlabel('x (локальные координаты)', fontsize=10)
        ax.set_ylabel('y (локальные координаты)', fontsize=10)
        ax.set_title(f'Освещенность - {channel} канал\nE_{channel}(x,y)', fontsize=12, fontweight='bold')

        cbar = plt.colorbar(im, ax=ax)
        cbar.set_label(f'E_{channel}', rotation=270, labelpad=15)

        example_points = [(0.0, 0.0), (1.5, 1.25), (3.0, 0.0)]
        for x, y in example_points:
            if x <= x_values[-1] and y <= y_values[-1]:
                ax.plot(x, y, 'o', color='cyan', markersize=8, markeredgecolor='white', markeredgewidth=2)

    plt.tight_layout()

def visualize_scene(P_L, direction_O, P0, P1, P2, normal_N):
    fig = plt.figure(figsize=(12, 9))
    fig.canvas.manager.set_window_title('Освещение треугольника точечным источником света')
    ax = fig.add_subplot(111, projection='3d')

    triangle = np.array([P0, P1, P2])
    poly = Poly3DCollection([triangle], alpha=0.6, facecolor='blue', edgecolor='black', linewidths=2)
    ax.add_collection3d(poly)

    ax.scatter(*P0, color='blue', s=100, marker='o', label='P₀')
    ax.scatter(*P1, color='blue', s=100, marker='o', label='P₁')
    ax.scatter(*P2, color='blue', s=100, marker='o', label='P₂')

    P_T_center = (P0 + P1 + P2) / 3
    ax.scatter(*P_T_center, color='red', s=100, marker='o', label='Pₜ (center)')
    ax.scatter(*P_L, color='yellow', s=200, marker='*', label='Pₗ (light)', edgecolors='orange', linewidths=2)

    ax.quiver(P_T_center[0], P_T_center[1], P_T_center[2], normal_N[0], normal_N[1], normal_N[2],
              color='red', arrow_length_ratio=0.3, linewidth=2, label='N (normal)')
    ax.quiver(P_L[0], P_L[1], P_L[2], direction_O[0], direction_O[1], direction_O[2],
              color='orange', arrow_length_ratio=0.3, linewidth=2, label='O (direction)')

    s_vector = P_L - P_T_center
    ax.quiver(P_T_center[0], P_T_center[1], P_T_center[2], s_vector[0], s_vector[1], s_vector[2],
              color='purple', arrow_length_ratio=0.1, linewidth=1.5, label='s (to light)', alpha=0.7)

    edge1_norm = (P1 - P0) / np.linalg.norm(P1 - P0) * 0.5
    edge2_norm = (P2 - P0) / np.linalg.norm(P2 - P0) * 0.5
    ax.quiver(P0[0], P0[1], P0[2], edge1_norm[0], edge1_norm[1], edge1_norm[2],
              color='green', arrow_length_ratio=0.3, linewidth=1.5, label='x-axis', linestyle='--')
    ax.quiver(P0[0], P0[1], P0[2], edge2_norm[0], edge2_norm[1], edge2_norm[2],
              color='cyan', arrow_length_ratio=0.3, linewidth=1.5, label='y-axis', linestyle='--')

    ax.set_xlabel('X')
    ax.set_ylabel('Y')
    ax.set_zlabel('Z')
    ax.set_title('Освещение треугольника точечным источником света')
    ax.legend(loc='upper left', fontsize=8)

    all_points = np.vstack([P0, P1, P2, P_L])
    max_range = np.array([all_points[:, 0].max() - all_points[:, 0].min(),
                          all_points[:, 1].max() - all_points[:, 1].min(),
                          all_points[:, 2].max() - all_points[:, 2].min()]).max() / 2.0
    mid_x = (all_points[:, 0].max() + all_points[:, 0].min()) * 0.5
    mid_y = (all_points[:, 1].max() + all_points[:, 1].min()) * 0.5
    mid_z = (all_points[:, 2].max() + all_points[:, 2].min()) * 0.5
    ax.set_xlim(mid_x - max_range, mid_x + max_range)
    ax.set_ylim(mid_y - max_range, mid_y + max_range)
    ax.set_zlim(mid_z - max_range, mid_z + max_range)

    plt.tight_layout()

def print_illumination_table(x_values, y_values, results):
    print("\n" + "="*100)
    print("ТАБЛИЦА ОСВЕЩЕННОСТИ E(RGB, P_T) для точек на треугольнике")
    print("="*100)

    y_x_label = 'y \\ x'
    header = f"{y_x_label:>10}"
    for x in x_values:
        header += f" | {f'x={x:.2f}':>20}"
    print(header)
    print("-" * 100)

    for i, y in enumerate(y_values):
        row = f"{f'y={y:.2f}':>10}"
        for j, x in enumerate(x_values):
            E_RGB = results[i, j]
            rgb_str = f"({E_RGB[0]:.4f}, {E_RGB[1]:.4f}, {E_RGB[2]:.4f})"
            row += f" | {rgb_str:>20}"
        print(row)

    print("="*100)

def main():
    I0_RGB = np.array([100.0, 80.0, 60.0])

    P0 = np.array([0.0, 0.0, 0.0])
    P1 = np.array([4.0, 0.0, 3.0])
    P2 = np.array([2.0, 3.0, 0.0])

    P_L = np.array([2.0, 2.0, 5.0])

    triangle_center = (P0 + P1 + P2) / 3
    direction_O = triangle_center - P_L
    direction_O = direction_O / np.linalg.norm(direction_O)

    print("="*100)
    print("ВХОДНЫЕ ДАННЫЕ")
    print("="*100)
    print(f"I₀(RGB) = {I0_RGB}")
    print(f"O (направление) = {direction_O}")
    print(f"Pₗ (источник света) = {P_L}")
    print(f"P₀ = {P0}")
    print(f"P₁ = {P1}")
    print(f"P₂ = {P2}")
    print("="*100)

    x_values, y_values = generate_grid_points(num_x=5, num_y=5, max_x=3.0, max_y=2.5)

    results, normal_N = calculate_illumination_grid(I0_RGB, direction_O, P_L, P0, P1, P2, x_values, y_values)

    print(f"\nВектор нормали N = {normal_N}")
    print(f"||N|| = {np.linalg.norm(normal_N)}")

    print_illumination_table(x_values, y_values, results)

    print("\n" + "="*100)
    print("ПРИМЕРЫ РАСЧЕТОВ ДЛЯ НЕСКОЛЬКИХ ТОЧЕК")
    print("="*100)

    example_points = [
        (0.0, 0.0, "P₀ (вершина)"),
        (1.5, 1.25, "центр треугольника"),
        (3.0, 0.0, "вдоль стороны P₀-P₁")
    ]

    for x_local, y_local, description in example_points:
        print("\n" + "-"*100)
        print(f"ТОЧКА: {description} | Локальные координаты: x = {x_local}, y = {y_local}")
        print("-"*100)

        P_T = convert_local_to_global(P0, P1, P2, x_local, y_local)
        print(f"Pₜ (глобальные координаты) = {P_T}")

        s_vector = P_L - P_T
        R = np.linalg.norm(s_vector)

        if R < 1e-10:
            print("Точка совпадает с источником света!")
            continue

        s_normalized = s_vector / R
        cos_alpha = np.dot(s_normalized, normal_N)
        cos_theta = np.dot(s_normalized, -direction_O)

        print(f"\nШаг 1: Вектор от точки до источника света")
        print(f"  s = Pₗ - Pₜ = {s_vector}")
        print(f"  R = ||s|| = √(s₀² + s₁² + s₂²) = √({s_vector[0]:.2f}² + {s_vector[1]:.2f}² + {s_vector[2]:.2f}²) = {R:.4f}")

        print(f"\nШаг 2: Вычисление углов")
        print(f"  cos(θ) = (s · (-O)) / ||s|| = {cos_theta:.4f}")
        print(f"    где θ - угол между вектором s и осью источника O")
        print(f"  cos(α) = (s · N) / ||s|| = {cos_alpha:.4f}")
        print(f"    где α - угол между вектором s и нормалью N к поверхности")

        if cos_alpha <= 0:
            print(f"  E(RGB, Pₜ) = (0.0, 0.0, 0.0)")
            continue

        if cos_theta <= 0:
            print(f"  E(RGB, Pₜ) = (0.0, 0.0, 0.0)")
            continue

        I_RGB_s = I0_RGB * cos_theta
        print(f"\nШаг 3: Интенсивность с учетом диаграммы направленности")
        print(f"  I(RGB, s) = I₀(RGB) × cos(θ)")
        print(f"  I(RGB, s) = {I0_RGB} × {cos_theta:.4f}")
        print(f"  I(RGB, s) = {I_RGB_s}")

        E_RGB = I_RGB_s * cos_alpha / (R ** 2)
        print(f"\nШаг 4: Освещенность точки")
        print(f"  E(RGB, Pₜ) = I(RGB, s) × cos(α) / R²")
        print(f"  E(RGB, Pₜ) = {I_RGB_s} × {cos_alpha:.4f} / {R:.4f}²")
        print(f"  E(RGB, Pₜ) = {I_RGB_s} × {cos_alpha:.4f} / {R**2:.4f}")
        print(f"  E(RGB, Pₜ) = ({E_RGB[0]:.6f}, {E_RGB[1]:.6f}, {E_RGB[2]:.6f})")

    print("\n" + "="*100) 

    P_T_center = (P0 + P1 + P2) / 3
    s_vector = P_L - P_T_center
    s_normalized = s_vector / np.linalg.norm(s_vector)
    
    cos_theta_current = np.dot(s_normalized, -direction_O)
    cos_theta_reversed = np.dot(s_normalized, direction_O)
    
    print(f"Текущее направление (-O): cos(θ) = {cos_theta_current:.4f}")
    E_current = calculate_illumination_at_point(I0_RGB, direction_O, P_L, P_T_center, normal_N)
    print(f"E(RGB) = ({E_current[0]:.4f}, {E_current[1]:.4f}, {E_current[2]:.4f})")
    
    print(f"\nОбратное направление (O): cos(θ) = {cos_theta_reversed:.4f}")
    print(f"E(RGB) = (0.0, 0.0, 0.0) - cos(θ) <= 0")

    visualize_scene(P_L, direction_O, P0, P1, P2, normal_N)
    visualize_example_points(I0_RGB, direction_O, P_L, P0, P1, P2, normal_N)

    x_dense = np.linspace(0, 3.0, 30)
    y_dense = np.linspace(0, 2.5, 25)
    results_dense, _ = calculate_illumination_grid(I0_RGB, direction_O, P_L, P0, P1, P2, x_dense, y_dense)
    visualize_illumination_heatmap(I0_RGB, direction_O, P_L, P0, P1, P2, normal_N, results_dense, x_dense, y_dense)

    plt.show()

if __name__ == "__main__":
    main()
