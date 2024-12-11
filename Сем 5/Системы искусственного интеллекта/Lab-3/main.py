# Импорт необходимых библиотек
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Загрузка набора данных
data = pd.read_csv('california_housing_train.csv')

# 1. Получение и вывод статистики по датасету
stats = data.describe()
print("Статистика по датасету:")
print(stats)

# Визуализация статистики (гистограммы для каждого признака)
data.hist(bins=50, figsize=(15, 10))
plt.tight_layout()
plt.show()

# 2. Предварительная обработка данных

# Проверка на отсутствующие значения
print("\nПроверка на отсутствующие значения:")
print(data.isnull().sum())

# Создание синтетического признака до нормализации
data['rooms_per_household'] = data['total_rooms'] / (data['households'] + 1e-10)

# Нормализация признаков (Min-Max нормализация)
def min_max_normalize(df):
    return (df - df.min()) / (df.max() - df.min())

normalized_data = data.copy()
features = normalized_data.columns.drop('median_house_value')
normalized_data[features] = min_max_normalize(normalized_data[features])

# Разделение данных на обучающий и тестовый наборы (80% и 20%)
train_size = int(0.8 * len(normalized_data))
train_data = normalized_data[:train_size].reset_index(drop=True)
test_data = normalized_data[train_size:].reset_index(drop=True)

# Реализация линейной регрессии методом наименьших квадратов
def linear_regression(X, y):
    # Добавление столбца единиц для свободного члена
    X_b = np.c_[np.ones((X.shape[0], 1)), X]
    # Вычисление коэффициентов с использованием псевдообратной матрицы
    theta_best = np.linalg.pinv(X_b.T.dot(X_b)).dot(X_b.T).dot(y)
    return theta_best

def predict(X, theta):
    # Умножение матрицы признакок на вектор коэффициентов
    X_b = np.c_[np.ones((X.shape[0], 1)), X]
    return X_b.dot(theta)

def r_squared(y_true, y_pred):
    # Насколько данные разбросаны вокруг среднего
    ss_total = ((y_true - y_true.mean()) ** 2).sum()
    # Насколько наши предсказания отклоняются от реальных данных.
    ss_residual = ((y_true - y_pred) ** 2).sum()
    r2 = 1 - ss_residual / ss_total
    return r2

# Подготовка целевой переменной
y_train = train_data['median_house_value'].values
y_test = test_data['median_house_value'].values

# Обучение трех моделей с разными наборами признаков

# Модель 1: только 'median_income'
features_model1 = ['median_income']
X_train_m1 = train_data[features_model1].values
theta_m1 = linear_regression(X_train_m1, y_train)

# Прогноз и оценка на тестовом наборе
X_test_m1 = test_data[features_model1].values
y_pred_m1 = predict(X_test_m1, theta_m1)
r2_m1 = r_squared(y_test, y_pred_m1)
print(f"\nМодель 1 R^2: {r2_m1}")

# Модель 2: 'median_income' и 'housing_median_age'
features_model2 = ['median_income', 'housing_median_age']
X_train_m2 = train_data[features_model2].values
theta_m2 = linear_regression(X_train_m2, y_train)

X_test_m2 = test_data[features_model2].values
y_pred_m2 = predict(X_test_m2, theta_m2)
r2_m2 = r_squared(y_test, y_pred_m2)
print(f"Модель 2 R^2: {r2_m2}")

# Модель 3: Все признаки, кроме 'median_house_value' и 'rooms_per_household'
features_model3 = features.drop('rooms_per_household')
X_train_m3 = train_data[features_model3].values
theta_m3 = linear_regression(X_train_m3, y_train)

X_test_m3 = test_data[features_model3].values
y_pred_m3 = predict(X_test_m3, theta_m3)
r2_m3 = r_squared(y_test, y_pred_m3)
print(f"Модель 3 R^2: {r2_m3}")

# Сравнение результатов моделей
print("\nСравнение моделей:")
print(f"Модель 1 R^2: {r2_m1}")
print(f"Модель 2 R^2: {r2_m2}")
print(f"Модель 3 R^2: {r2_m3}")

# Бонусное задание: введение синтетического признака
# Модель с синтетическим признаком
features_model_bonus = features.tolist()
X_train_bonus = train_data[features_model_bonus].values
theta_bonus = linear_regression(X_train_bonus, y_train)

X_test_bonus = test_data[features_model_bonus].values
y_pred_bonus = predict(X_test_bonus, theta_bonus)
r2_bonus = r_squared(y_test, y_pred_bonus)
print(f"\nМодель с синтетическим признаком R^2: {r2_bonus}")

# Выводы
print("\nВыводы:")
print(f"Модель 1 R^2: {r2_m1}")
print(f"Модель 2 R^2: {r2_m2}")
print(f"Модель 3 R^2: {r2_m3}")
print(f"Модель с синтетическим признаком R^2: {r2_bonus}")

if r2_bonus > r2_m3:
    print("\nМодель с синтетическим признаком показывает улучшение качества модели по сравнению с моделью 3.")
else:
    print("\nМодель с синтетическим признаком не улучшает качество модели по сравнению с моделью 3.")
