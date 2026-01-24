import numpy as np
# -------------------------------
# Настройки задачи
# -------------------------------
a, b = 2.0, 5.0  # границы интегрирования
N_values = [100]
np.random.seed(42)  # фиксируем зерно для воспроизводимости
# функция и истинное значение интеграла
def f(x):
    return x ** 2
I_true = (b ** 3 - a ** 3) / 3.0  # аналитически ∫_2^5 x^2 dx
# I_true должно быть 39.0
# -------------------------------
# Базовые методы
# -------------------------------
def mc_simple_uniform(N):
    """
    Простое интегрирование методом Монте-Карло:
    X ~ U(a, b), I ≈ (b-a)/N * sum f(X_i)
    """
    x = np.random.uniform(a, b, size=N)
    return (b - a) * np.mean(f(x))
def mc_stratified(N, step):
    """
    Стратификация по отрезкам длины step:
    [a, a+step], [a+step, a+2*step], ..., [.., b]
    В каждом страта-отрезке берём примерно одинаковое число выборок.
    """
    edges = np.arange(a, b + 1e-12, step)
    if abs(edges[-1] - b) > 1e-9:
        edges = np.append(edges, b)
    total_estimate = 0.0
    n_strata = len(edges) - 1
    # равномерно распределяем N по стратам
    base = N // n_strata
    remainder = N - base * n_strata
    for i in range(n_strata):
        n_i = base + (1 if i < remainder else 0)
        left, right = edges[i], edges[i + 1]
        x = np.random.uniform(left, right, size=n_i)
        # локальный интеграл на страте
        total_estimate += (right - left) * np.mean(f(x))
    return total_estimate
# -------------------------------
# Вспомогательные функции для выборки по значимости
# p(x) ~ x^k, k = 1,2,3 на [a,b]
# -------------------------------
def sample_power(N, k):
    """
    Сэмплирование из распределения с плотностью p(x) ∝ x^k на [a, b]
    через метод обратной функции.
    CDF: F(x) = (x^{k+1} - a^{k+1}) / (b^{k+1} - a^{k+1})
    => x = (u * (b^{k+1} - a^{k+1}) + a^{k+1})^{1/(k+1)}
    """
    u = np.random.rand(N)
    return (u * (b ** (k + 1) - a ** (k + 1)) + a ** (k + 1)) ** (1.0 / (k + 1))


def pdf_power(x, k):
    """
    Нормированная плотность:
    p_k(x) = (k+1) * x^k / (b^{k+1} - a^{k+1}), x∈[a,b]
    """
    return (k + 1) * x ** k / (b ** (k + 1) - a ** (k + 1))
def mc_importance(N, k):
    """
    Интегрирование методом Монте-Карло с выборкой по значимости
    для плотности p_k(x) ~ x^k (нормированной).
    I ≈ 1/N * Σ f(x_i) / p_k(x_i), где x_i ~ p_k.
    """
    x = sample_power(N, k)
    p = pdf_power(x, k)
    return np.mean(f(x) / p)
# -------------------------------
# Многократная выборка по значимости (Multiple Importance Sampling)
# p1(x) ~ x, p2(x) ~ x^3
# Два варианта весов:
# 1) w1 = p1/(p1+p2), w2 = p2/(p1+p2)
# 2) w1 = p1^2/(p1^2+p2^2), w2 = p2^2/(p1^2+p2^2)
# -------------------------------
def mc_mis(N, use_power_heuristic=False):
    """
    Multiple Importance Sampling:
    - N/2 выборок из p1(x) ~ x
    - N/2 выборок из p2(x) ~ x^3
    Веса считаем по заданным формулам.
    Возвращаем оценку интеграла.
    """
    N1 = N // 2
    N2 = N - N1  # на случай нечётного N, но у нас N чётные
    # выборка из p1 (k=1)
    x1 = sample_power(N1, 1)
    p1_x1 = pdf_power(x1, 1)
    p2_x1 = pdf_power(x1, 3)
    # выборка из p2 (k=3)
    x2 = sample_power(N2, 3)
    p1_x2 = pdf_power(x2, 1)
    p2_x2 = pdf_power(x2, 3)
    if use_power_heuristic:
        # второй вариант: средний квадрат плотностей
        w1_x1 = p1_x1 ** 2 / (p1_x1 ** 2 + p2_x1 ** 2)
        w2_x2 = p2_x2 ** 2 / (p1_x2 ** 2 + p2_x2 ** 2)
    else:
        # первый вариант: средняя плотность
        w1_x1 = p1_x1 / (p1_x1 + p2_x1)
        w2_x2 = p2_x2 / (p1_x2 + p2_x2)
    # Оценка из выборок p1 и p2
    est1 = np.mean(f(x1) * w1_x1 / p1_x1)
    est2 = np.mean(f(x2) * w2_x2 / p2_x2)

    # Суммируем обе части
    return est1 + est2


# -------------------------------
# Русская рулетка
# -------------------------------

def mc_russian_roulette(N, R):
    """
    Метод Монте-Карло с русской рулеткой.
    Пусть с вероятностью R "выживаем" и считаем вклад,
    с вероятностью (1-R) — обнуляем вклад.
    Если выжили: X~U(a,b), вклад = (b-a)*f(X)/R
    Тогда E[вклад] = ∫ f(x) dx — оценка интеграла.
    I ≈ 1/N * Σ sample_i
    """
    u = np.random.rand(N)
    survive_mask = (u < R)
    M = survive_mask.sum()
    samples = np.zeros(N)
    if M > 0:
        x = np.random.uniform(a, b, size=M)
        samples[survive_mask] = (b - a) * f(x) / R

    return samples.mean()
# -------------------------------
# Запуск экспериментов и вывод таблицы
# -------------------------------
methods = []
for N in N_values:
    delta_I = I_true / N  # оценка погрешности по заданию
    # 1. Простое Монте-Карло
    I_est = mc_simple_uniform(N)
    methods.append(("Простое MC (равномерное)", N, I_est, abs(I_est - I_true), delta_I))
    # 2. Стратификация: шаг 1
    I_est = mc_stratified(N, step=1.0)
    methods.append(("Стратификация, шаг 1.0", N, I_est, abs(I_est - I_true), delta_I))
    # 3. Стратификация: шаг 0.5
    I_est = mc_stratified(N, step=0.5)
    methods.append(("Стратификация, шаг 0.5", N, I_est, abs(I_est - I_true), delta_I))
    # 4. Выборка по значимости: p1(x) ~ x
    I_est = mc_importance(N, k=1)
    methods.append(("Важн. выборка p(x)∝x", N, I_est, abs(I_est - I_true), delta_I))
    # 5. Выборка по значимости: p2(x) ~ x^2
    I_est = mc_importance(N, k=2)
    methods.append(("Важн. выборка p(x)∝x^2", N, I_est, abs(I_est - I_true), delta_I))
    # 6. Выборка по значимости: p3(x) ~ x^3
    I_est = mc_importance(N, k=3)
    methods.append(("Важн. выборка p(x)∝x^3", N, I_est, abs(I_est - I_true), delta_I))
    # 7. MIS: средняя плотность (balance heuristic)
    I_est = mc_mis(N, use_power_heuristic=False)
    methods.append(("MIS (средняя плотность)", N, I_est, abs(I_est - I_true), delta_I))
    # 8. MIS: средний квадрат плотностей (power heuristic)
    I_est = mc_mis(N, use_power_heuristic=True)
    methods.append(("MIS (средний квадрат)", N, I_est, abs(I_est - I_true), delta_I))
    # 9. Русская рулетка, R=0.5
    I_est = mc_russian_roulette(N, R=0.5)
    methods.append(("Русская рулетка R=0.5", N, I_est, abs(I_est - I_true), delta_I))
    # 10. Русская рулетка, R=0.75
    I_est = mc_russian_roulette(N, R=0.75)
    methods.append(("Русская рулетка R=0.75", N, I_est, abs(I_est - I_true), delta_I))
    # 11. Русская рулетка, R=0.95
    I_est = mc_russian_roulette(N, R=0.95)
    methods.append(("Русская рулетка R=0.95", N, I_est, abs(I_est - I_true), delta_I))
# Печать таблицы
print(f"Истинное значение интеграла I_true = {I_true:.6f}\n")
header = f"{'Метод':35s} {'N':>8s} {'I_MC':>15s} {'|I_MC - I_true|':>18s} {'ΔI=I_true/N':>15s}"
print(header)
print("-" * len(header))
for name, N, I_est, err, delta_I in methods:
    print(f"{name:35s} {N:8d} {I_est:15.8f} {err:18.8f} {delta_I:15.8f}")
