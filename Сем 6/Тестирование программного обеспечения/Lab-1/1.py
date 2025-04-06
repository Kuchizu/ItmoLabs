import unittest
import math
import random

def arctg(x):
    if x > 1:
        return math.pi / 2 - arctg(1 / x)

    elif x < -1:
        return -math.pi / 2 - arctg(1 / x)

    n_terms = 30
    result = 0
    for n in range(n_terms):
        result += ((-1) ** n) * (x ** (2 * n + 1)) / (2 * n + 1)

    return result

class TestArctgSeries(unittest.TestCase):
    def test_arctg_0(self):
        x = 0
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")
    
    def test_arctg_0_5(self):
        x = 0.5
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")
    
    def test_arctg_minus_0_5(self):
        x = -0.5
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")
    
    def test_arctg_1(self):
        x = 1
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")
    
    def test_arctg_minus_1(self):
        x = -1
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")
    
    def test_arctg_10(self):
        x = 10
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")

    def test_arctg_10(self):
        x = -10
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")    

    def test_arctg_2000(self):
        for x in range(-1000, 1001):
            approx_value = arctg(x)
            exact_value = math.atan(x)
            error = abs(approx_value - exact_value)
            self.assertLess(error, 0.01, f"Test failed for x={x}. Error: {error:.6f}")    
    
    def test_arctg_random(self):
        x = random.randint(-10000, 10000)
        approx_value = arctg(x)
        exact_value = math.atan(x)
        error = abs(approx_value - exact_value)
        self.assertLess(error, 0.01, f"Test failed for random x={x}. Error: {error:.6f}")
    
    def test_arctg_infinity(self):
        x = float('inf')
        approx_value = arctg(x)
        exact_value = math.atan(x)
        self.assertEqual(approx_value, exact_value, f"Test failed for x={x}.")
    
    def test_arctg_none(self):
        with self.assertRaises(TypeError):
            arctg(None)

if __name__ == "__main__":
    unittest.main()
