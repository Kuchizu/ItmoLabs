#!/bin/python3

import math
import os
import random
import re
import sys

class Solution:
    isSolutionExists = True
    errorMessage = ""

    @staticmethod
    def solveByGauss(n, matrix):
        for i in range(n):
            max_row = i
            for k in range(i+1, n):
                if abs(matrix[k][i]) > abs(matrix[max_row][i]):
                    max_row = k
            matrix[i], matrix[max_row] = matrix[max_row], matrix[i]

            if matrix[i][i] == 0:
                Solution.isSolutionExists = False
                Solution.errorMessage = "The system has no roots of equations or has an infinite set of them."
                return []

            for k in range(i+1, n):
                factor = matrix[k][i] / matrix[i][i]
                for j in range(i, n+1):
                    matrix[k][j] -= factor * matrix[i][j]

        x = [0 for _ in range(n)]
        for i in range(n-1, -1, -1):
            sum_ax = 0
            for j in range(i+1, n):
                sum_ax += matrix[i][j] * x[j]
            x[i] = (matrix[i][n] - sum_ax) / matrix[i][i]

        residuals = [0 for _ in range(n)]
        for i in range(n):
            actual = sum(matrix[i][j] * x[j] for j in range(n))
            residuals[i] = actual - matrix[i][-1]

        return x + residuals
        
if __name__ == '__main__':
    n = int(input().strip())

    matrix_rows = n
    matrix_columns = n + 1

    matrix = []

    for _ in range(matrix_rows):
        matrix.append(list(map(float, input().rstrip().split())))

    result = Solution.solveByGauss(n, matrix)
    if Solution.isSolutionExists:
        print('\n'.join(map(str, result)))
    else:
        print(f"{Solution.errorMessage}")
