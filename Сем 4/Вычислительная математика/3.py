#!/bin/python3

import math
import os
import random
import re
import sys


def first_function(args: []) -> float:
    return math.sin(args[0])


def second_function(args: []) -> float:
    return (args[0] * args[1]) / 2


def third_function(args: []) -> float:
    return pow(args[0], 2) * pow(args[1], 2) - 3 * pow(args[0], 3) - 6 * pow(args[1], 3) + 8


def fourth_function(args: []) -> float:
    return pow(args[0], 4) - 9 * args[1] + 2


def fifth_function(args: []) -> float:
    return args[0] + pow(args[0], 2) - 2 * args[1] * args[2] - 0.1


def six_function(args: []) -> float:
    return args[1] + pow(args[1], 2) + 3 * args[0] * args[2] + 0.2


def seven_function(args: []) -> float:
    return args[2] + pow(args[2], 2) + 2 * args[0] * args[1] - 0.3


def default_function(args: []) -> float:
    return 0.0


# How to use this function:
# funcs = Result.get_functions(4)
# funcs[0](0.01)
def get_functions(n: int):
    if n == 1:
        return [first_function, second_function]
    elif n == 2:
        return [third_function, fourth_function]
    elif n == 3:
        return [fifth_function, six_function, seven_function]
    else:
        return [default_function]


#
# Complete the 'solve_by_fixed_point_iterations' function below.
#
# The function is expected to return a DOUBLE_ARRAY.
# The function accepts following parameters:
#  1. INTEGER system_id
#  2. INTEGER number_of_unknowns
#  3. DOUBLE_ARRAY initial_approximations
#
def solve_by_fixed_point_iterations(system_id, number_of_unknowns, initial_approximations):
    funcs = get_functions(system_id)
    tolerance = 1e-5
    max_iterations = 10000
    current_approximations = initial_approximations[:]
    scaling_factors = [0.01] * number_of_unknowns

    for iteration in range(max_iterations):
        next_approximations = current_approximations.copy()
        for i in range(number_of_unknowns):          
            calculated_value = funcs[i](current_approximations)
            delta = calculated_value - current_approximations[i]
            next_approximation = current_approximations[i] + scaling_factors[i] * delta

            if abs(next_approximation) > 1e11:
                return ["OverflowError: Calculation exceeded limits."]

            next_approximations[i] = next_approximation

            if abs(delta) < tolerance:
                scaling_factors[i] *= 1.1
            else:
                scaling_factors[i] *= 0.9

        if all(abs(next_approximations[i] - current_approximations[i]) < tolerance for i in range(number_of_unknowns)):
            break

        current_approximations = next_approximations

    return [round(x, 5) for x in current_approximations]

if __name__ == '__main__':
    system_id = int(input().strip())

    number_of_unknowns = int(input().strip())

    initial_approximations = []

    for _ in range(number_of_unknowns):
        initial_approximations_item = float(input().strip())
        initial_approximations.append(initial_approximations_item)

    result = solve_by_fixed_point_iterations(system_id, number_of_unknowns, initial_approximations)

    print('\n'.join(map(str, result)))