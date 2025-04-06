import math
import csv
import unittest

EPS = 1e-10


def sin_series(x, eps=EPS):
    term = x
    s = term
    k = 1
    while abs(term) > eps:
        term *= -x * x / ((2 * k) * (2 * k + 1))
        s += term
        k += 1
    return s


def ln_series(x, eps=EPS):
    if x <= 0:
        return float('nan')
    z = (x - 1) / (x + 1)
    s = 0.0
    n = 0
    term = 2.0 * (z ** (2 * n + 1)) / (2 * n + 1)
    while abs(term) > eps:
        s += term
        n += 1
        term = 2.0 * (z ** (2 * n + 1)) / (2 * n + 1)
    return s


def csc_series(x, eps=EPS):
    val_sin = sin_series(x, eps)
    if abs(val_sin) < eps:
        return float('nan')
    return 1.0 / val_sin


def log2_series(x, eps=EPS):
    l = ln_series(x, eps)
    if math.isnan(l):
        return float('nan')
    ln2 = ln_series(2.0, eps)
    return l / ln2


def log10_series(x, eps=EPS):
    l = ln_series(x, eps)
    if math.isnan(l):
        return float('nan')
    ln10 = ln_series(10.0, eps)
    return l / ln10


def sin_stub(x):
    table = {
        0.0: 0.0,
        -math.pi / 2: -1.0,
        -math.pi: 0.0,
        math.pi / 2: 1.0,
        math.pi: 0.0
    }
    if x in table:
        return table[x]
    return sin_series(x)


def csc_stub(x):
    table = {
        -math.pi / 2: -1.0,
        math.pi / 2: 1.0
    }
    if x in table:
        return table[x]
    s = sin_stub(x)
    if abs(s) < EPS:
        return float('nan')
    return 1.0 / s


def ln_stub(x):
    table = {
        1.0: 0.0,
        math.e: 1.0,
        2.0: 0.69314718,
        10.0: 2.30258509
    }
    if x in table:
        return table[x]
    return ln_series(x)


def log2_stub(x):
    table = {
        1.0: 0.0,
        2.0: 1.0,
        4.0: 2.0
    }
    if x in table:
        return table[x]
    return ln_stub(x) / ln_stub(2.0)


def log10_stub(x):
    table = {
        1.0: 0.0,
        10.0: 1.0,
        100.0: 2.0
    }
    if x in table:
        return table[x]
    return ln_stub(x) / ln_stub(10.0)


def system_function(x, sin_func=sin_stub, csc_func=csc_stub, ln_func=ln_stub,
                    log2_func=log2_stub, log10_func=log10_stub):
    if x <= 0:
        s = sin_func(x)
        c = csc_func(x)
        if math.isnan(s) or math.isnan(c):
            return float('nan')
        # Выражение упрощается до (s - c)
        return (s - c) * (c / c)
    else:
        l2 = log2_func(x)
        l10 = log10_func(x)
        l = ln_func(x)
        if math.isnan(l2) or math.isnan(l10) or math.isnan(l) or abs(l10) < EPS:
            return float('nan')
        return ((((l2 + l10) ** 3) / l10) ** 3) + l


def module_to_csv(func, start, end, step, filename):
    with open(filename, 'w', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        x = start
        while x <= end + EPS:
            val = func(x)
            writer.writerow([x, val])
            x += step


class TestSystemFunctions(unittest.TestCase):
    def test_sin_series(self):
        self.assertAlmostEqual(sin_series(0), 0.0, delta=1e-7)
        self.assertAlmostEqual(sin_series(-math.pi / 2), -1.0, delta=1e-5)
        self.assertAlmostEqual(sin_series(math.pi / 2), 1.0, delta=1e-5)

    def test_ln_series(self):
        self.assertTrue(math.isnan(ln_series(-1)))
        self.assertAlmostEqual(ln_series(1), 0.0, delta=1e-7)
        self.assertAlmostEqual(ln_series(math.e), 1.0, delta=1e-5)

    def test_log2_series(self):
        self.assertAlmostEqual(log2_series(1), 0.0, delta=1e-7)
        self.assertAlmostEqual(log2_series(2), 1.0, delta=1e-5)

    def test_log10_series(self):
        self.assertAlmostEqual(log10_series(1), 0.0, delta=1e-7)
        self.assertAlmostEqual(log10_series(10), 1.0, delta=1e-5)

    def test_system_function_neg(self):
        self.assertTrue(math.isnan(system_function(0)))  # csc(0) неопределён
        self.assertTrue(math.isnan(system_function(-math.pi)))  # sin(-pi)=0
        val = system_function(-math.pi / 2)
        self.assertAlmostEqual(val, 0.0, delta=1e-5)

    def test_system_function_pos(self):
        self.assertTrue(math.isnan(system_function(1)))  # log10(1)=0, деление на 0
        self.assertFalse(math.isnan(system_function(10)))  # должно вычисляться

    def test_system_function_additional_negative(self):
        test_cases = {
            -3.0: 6.945047387680084,
            -2.5: 1.0724494014545183,
            -2.0: 0.19045274346902474,
            -1.5: 0.005016317642633994,
            -1.0: 0.3469241209702315,
            -0.5: 1.60640410432939
        }
        for x, expected in test_cases.items():
            self.assertAlmostEqual(system_function(x), expected, delta=1e-5)

    def test_system_function_additional_positive(self):
        test_cases = {
            0.1: 526133.4758754328,
            0.6: 62.214041182131915,
            1.1: 0.09795648199878335,
            1.6: 38.52493757173488,
            2.1: 589.5952090963884,
            2.6: 2687.654822601158,
            3.1: 7405.799779675479,
            3.6: 15595.510003220896,
            4.1: 27858.88472575037,
            4.6: 44590.45561539221
        }
        for x, expected in test_cases.items():
            self.assertAlmostEqual(system_function(x), expected, delta=1e-5)

    def test_system_function_integration_stubs(self):
        test_cases_neg = {
            -3.0: 6.945047387680084,
            -2.5: 1.0724494014545183,
            -2.0: 0.19045274346902474,
            -1.5: 0.005016317642633994,
            -1.0: 0.3469241209702315,
            -0.5: 1.60640410432939
        }
        test_cases_pos = {
            0.1: 526133.4758754328,
            0.6: 62.214041182131915,
            1.1: 0.09795648199878335,
            1.6: 38.52493757173488,
            2.1: 589.5952090963884,
            2.6: 2687.654822601158,
            3.1: 7405.799779675479,
            3.6: 15595.510003220896,
            4.1: 27858.88472575037,
            4.6: 44590.45561539221
        }
        for x, expected in test_cases_neg.items():
            res = system_function(x,
                                  sin_func=sin_stub, csc_func=csc_stub,
                                  ln_func=ln_stub, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)
        for x, expected in test_cases_pos.items():
            res = system_function(x,
                                  sin_func=sin_stub, csc_func=csc_stub,
                                  ln_func=ln_stub, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)

    def test_integration_replace_sin(self):
        test_cases_neg = {
            -3.0: 6.945047387680084,
            -1.0: 0.3469241209702315
        }
        test_cases_pos = {
            0.1: 526133.4758754328,
            1.6: 38.52493757173488
        }
        for x, expected in test_cases_neg.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_stub,
                                  ln_func=ln_stub, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)
        for x, expected in test_cases_pos.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_stub,
                                  ln_func=ln_stub, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)

    def test_integration_replace_sin_and_csc(self):
        test_cases_neg = {
            -3.0: 6.945047387680084,
            -1.0: 0.3469241209702315
        }
        test_cases_pos = {
            0.1: 526133.4758754328,
            1.6: 38.52493757173488
        }
        for x, expected in test_cases_neg.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_stub, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)
        for x, expected in test_cases_pos.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_stub, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)

    def test_integration_replace_sin_csc_ln(self):
        test_cases_neg = {
            -3.0: 6.945047387680084,
            -1.0: 0.3469241209702315
        }
        test_cases_pos = {
            0.1: 526133.4758754328,
            1.6: 38.52493757173488
        }
        for x, expected in test_cases_neg.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)
        for x, expected in test_cases_pos.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_stub, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)

    def test_integration_replace_sin_csc_ln_log2(self):
        test_cases_neg = {
            -3.0: 6.945047387680084,
            -1.0: 0.3469241209702315
        }
        test_cases_pos = {
            0.1: 526133.4758754328,
            1.6: 38.52493757173488
        }
        for x, expected in test_cases_neg.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-5)
        for x, expected in test_cases_pos.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_stub)
            self.assertAlmostEqual(res, expected, delta=1e-2)

    # Small bang
    def test_system_function_integration_series(self):
        test_cases_neg = {
            -3.0: 6.945047387680084,
            -2.5: 1.0724494014545183,
            -2.0: 0.19045274346902474,
            -1.5: 0.005016317642633994,
            -1.0: 0.3469241209702315,
            -0.5: 1.60640410432939
        }
        test_cases_pos = {
            0.1: 526133.4758754328,
            0.6: 62.214041182131915,
            1.1: 0.09795648199878335,
            1.6: 38.52493757173488,
            2.1: 589.5952090963884,
            2.6: 2687.654822601158,
            3.1: 7405.799779675479,
            3.6: 15595.510003220896,
            4.1: 27858.88472575037,
            4.6: 44590.45561539221
        }
        for x, expected in test_cases_neg.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_series)
            self.assertAlmostEqual(res, expected, delta=1e-5)
        for x, expected in test_cases_pos.items():
            res = system_function(x,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_series)
            self.assertAlmostEqual(res, expected, delta=1e-2)
        # Краевые случаи
        self.assertTrue(math.isnan(system_function(0,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_series)))
        self.assertTrue(math.isnan(system_function(-math.pi,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_series)))
        self.assertAlmostEqual(system_function(-math.pi/2,
                                  sin_func=sin_series, csc_func=csc_series,
                                  ln_func=ln_series, log2_func=log2_series, log10_func=log10_series),
                               0.0, delta=1e-5)


if __name__ == '__main__':
    unittest.main(argv=[''], exit=False, verbosity=2)

    module_to_csv(system_function, -50.0, 0, 0.5, 'negative_part.csv')
    module_to_csv(system_function, 0.1, 50.0, 0.5, 'positive_part.csv')
