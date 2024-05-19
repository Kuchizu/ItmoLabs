import math
import os
import random
import re
import sys

class Result:
    error_message = "Integrated function has discontinuity or does not defined in current interval"
    has_discontinuity = False
    eps = 1e-6

    def first_function(x: float):
        if x == 0:
            Result.has_discontinuity = True
        return 1 / x if x != 0 else 0

    def second_function(x: float):
        if x == 0:
            # Use eps to handle the function's behavior at x = 0
            return (math.sin(Result.eps) / Result.eps + math.sin(-Result.eps) / -Result.eps) / 2
        return math.sin(x) / x

    def third_function(x: float):
        return x*x + 2

    def fourth_function(x: float):
        return 2*x + 2

    def five_function(x: float):
        if x <= 0:
            Result.has_discontinuity = True
        return math.log(x) if x > 0 else 0

    def get_function(n: int):
        if n == 1:
            return Result.first_function
        elif n == 2:
            return Result.second_function
        elif n == 3:
            return Result.third_function
        elif n == 4:
            return Result.fourth_function
        elif n == 5:
            return Result.five_function
        else:
            raise NotImplementedError(f"Function {n} not defined.")

    def calculate_integral(a, b, f, epsilon):
        if a > b:
            a, b = b, a  # Swap to make a <= b
            should_negate = True
        else:
            should_negate = False

        func = Result.get_function(f)
        n = 10  # Initial number of intervals
        old_result = 0

        while True:
            h = (b - a) / n
            integral = func(a) + func(b)

            # Apply Simpson's Rule
            for i in range(1, n, 2):
                integral += 4 * func(a + i * h)
            for i in range(2, n - 1, 2):
                integral += 2 * func(a + i * h)

            integral = (h / 3) * integral
            if Result.has_discontinuity:
                return None

            # Convergence check
            if abs(integral - old_result) < epsilon:
                return -integral if should_negate else integral
            old_result = integral
            n *= 2

if __name__ == '__main__':
    a = float(input().strip())
    b = float(input().strip())
    f = int(input().strip())
    epsilon = float(input().strip())

    result = Result.calculate_integral(a, b, f, epsilon)
    if not Result.has_discontinuity:
        print(f"{result}\n")
    else:
        print(Result.error_message + '\n')