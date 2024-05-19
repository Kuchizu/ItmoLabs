#!/bin/python3

import math
import os
import random
import re
import sys


class FunctionSet:

    def weierstrass_function(x: float):
        f_x = 0
        n = 5
        b = 0.5
        a = 13
        for i in range(n):
            f_x += pow(b, i) * math.cos(pow(a, n) * math.pi * x)

        return f_x

    def gamma_function(x: float):
        # made for consistency with other languages. Normally math.gamma can be used.
        tmp = (x - 0.5) * math.log(x + 4.5) - (x + 4.5)
        ser = 1.0 + 76.18009173 / (x + 0.0) - 86.50532033 / (x + 1.0) + 24.01409822 / (x + 2.0) - 1.231739516 / (
                x + 3.0) + 0.00120858003 / (x + 4.0) - 0.00000536382 / (x + 5.0)

        return math.exp(tmp + math.log(ser * math.sqrt(2 * math.pi)))

    # How to use this function:
    # func = FunctionSet.get_function(4)
    # func(0.01)
    def get_function(n: int):
        if n == 1:
            return FunctionSet.weierstrass_function
        elif n == 2:
            return FunctionSet.gamma_function
        else:
            raise NotImplementedError(f"Function {n} not defined.")


#
# Complete the 'interpolate_by_lagrange' function below.
#
# The function is expected to return a DOUBLE.
# The function accepts following parameters:
#  1. INTEGER f
#  2. DOUBLE a
#  3. DOUBLE b
#  4. DOUBLE x
#
def lagrange_interpolation(x, x_points, y_points):
    n = len(x_points)
    polynomial_value = 0
    for i in range(n):
        term = y_points[i]
        for j in range(n):
            if i != j:
                term = term * (x - x_points[j]) / (x_points[i] - x_points[j])
        polynomial_value += term
    return polynomial_value


def interpolate_by_lagrange(f, a, b, x):
    func = FunctionSet.get_function(f)
    n = 1
    prev_value = 0
    current_value = 0
    while True:
        n += 1
        nodes = [(a + b) / 2 + (b - a) / 2 * math.cos((2 * i + 1) / (2 * n) * math.pi) for i in range(n)]
        values = [func(node) for node in nodes]
        current_value = lagrange_interpolation(x, nodes, values)
        if n > 2 and abs(current_value - prev_value) < 0.01:
            break
        prev_value = current_value

    return current_value


import matplotlib.pyplot as plt
import numpy as np

# Задайте интервал и функцию
a, b = 0, 2  # Пример интервала для функции Вейерштрасса
x_points = np.linspace(a, b, 100)  # 100 точек на интервале для гладкости кривой
y_real = [FunctionSet.weierstrass_function(x) for x in x_points]  # Реальные значения функции

# Интерполированные значения (примерно, требуется адаптация вашего кода)
y_interpolated = [interpolate_by_lagrange(1, a, b, x) for x in x_points]

# Визуализация
plt.figure(figsize=(10, 6))
plt.plot(x_points, y_real, label='Real Function')
plt.plot(x_points, y_interpolated, label='Interpolated', linestyle='--')
plt.legend()
plt.xlabel('x')
plt.ylabel('f(x)')
plt.title('Interpolation Example')
plt.show()

