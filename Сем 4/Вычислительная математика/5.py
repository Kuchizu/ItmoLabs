#!/bin/python3

import math
import os
import random
import re
import sys

class Result:
    
    def first_function(x: float, y: float):
        return math.sin(x)


    def second_function(x: float, y: float):
        return (x * y)/2


    def third_function(x: float, y: float):
        return y - (2 * x)/y


    def fourth_function(x: float, y: float):
        return x + y

    
    def default_function(x:float, y: float):
        return 0.0

    # How to use this function:
    # func = Result.get_function(4)
    # func(0.01)
    def get_function(n: int):
        if n == 1:
            return Result.first_function
        elif n == 2:
            return Result.second_function
        elif n == 3:
            return Result.third_function
        elif n == 4:
            return Result.fourth_function
        else:
            return Result.default_function

    #
    # Complete the 'solveByAdams' function below.
    #
    # The function is expected to return a DOUBLE.
    # The function accepts following parameters:
    #  1. INTEGER f
    #  2. DOUBLE epsilon
    #  3. DOUBLE a
    #  4. DOUBLE y_a
    #  5. DOUBLE b
    #
    def solveByAdams(f, epsilon, a, y_a, b):
        func = Result.get_function(f)
        h = 0.1
        x = a
        y = y_a
        while x < b:
            k1 = h * func(x, y)
            k2 = h * func(x + h/2, y + k1/2)
            k3 = h * func(x + h/2, y + k2/2)
            k4 = h * func(x + h, y + k3)
            y_next = y + (k1 + 2*k2 + 2*k3 + k4) / 6
            y_pred = y + h * func(x, y)
            error = abs(y_next - y_pred)
            if error > epsilon:
                h = h * 0.5
            else:
                x += h
                y = y_next
                if error < epsilon / 4:
                    h = h * 2
        return y

if __name__ == '__main__':
    f = int(input().strip())
    epsilon = float(input().strip())
    a = float(input().strip())
    y_a = float(input().strip())
    b = float(input().strip())

    result = Result.solveByAdams(f, epsilon, a, y_a, b)
    print(str(result) + '\n')