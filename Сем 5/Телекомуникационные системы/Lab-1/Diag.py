import matplotlib.pyplot as plt
import numpy as np

# Исходная последовательность битов (первые 4 байта сообщения)
bits = [
    1,1,0,0,1,1,0,1,  # 'Н' (CD)
    1,1,1,0,1,1,1,0,  # 'о' (EE)
    1,1,1,0,0,1,0,0,  # 'д' (E4)
    1,1,1,0,1,0,0,0   # 'и' (E8)
]

# Общие параметры для графиков
bit_duration = 1  # Длительность одного бита
samples_per_bit = 100  # Количество точек на бит
time = np.arange(0, len(bits) * bit_duration, bit_duration / samples_per_bit)

def nrzl_encode(bits):
    signal = []
    for bit in bits:
        level = bit
        signal.extend([level]*samples_per_bit)
    return signal

def manchester_encode(bits):
    signal = []
    for bit in bits:
        if bit == 1:
            signal.extend([0]*(samples_per_bit//2) + [1]*(samples_per_bit//2))
        else:
            signal.extend([1]*(samples_per_bit//2) + [0]*(samples_per_bit//2))
    return signal

def differential_manchester_encode(bits):
    signal = []
    prev_level = 1  # Начальное состояние
    for bit in bits:
        if bit == 0:
            # Переход в начале интервала
            prev_level = 1 - prev_level
            signal.extend([prev_level]*(samples_per_bit//2))
            prev_level = 1 - prev_level
            signal.extend([prev_level]*(samples_per_bit//2))
        else:
            # Нет перехода в начале, только в середине
            signal.extend([prev_level]*(samples_per_bit//2))
            prev_level = 1 - prev_level
            signal.extend([prev_level]*(samples_per_bit//2))
    return signal

def mlt3_encode(bits):
    signal = []
    levels = [0, 1, 0, -1]
    current_level_index = 0
    for bit in bits:
        if bit == 1:
            # Переходим к следующему уровню
            current_level_index = (current_level_index + 1) % len(levels)
            # Если уровень нулевой, пропускаем его
            if levels[current_level_index] == 0:
                current_level_index = (current_level_index + 1) % len(levels)
        # Если бит 0, уровень не меняется
        signal.extend([levels[current_level_index]]*samples_per_bit)
    return signal

# Создание и сохранение графиков

# 1. NRZ-L
signal_nrzl = nrzl_encode(bits)
plt.figure(figsize=(12, 3))
plt.step(time, signal_nrzl, where='post')
plt.title('NRZ-L')
plt.ylabel('Уровень')
plt.xlabel('Время')
plt.ylim(-0.5, 1.5)
plt.grid(True)
plt.tight_layout()
plt.savefig('nrzl_diagram.png', dpi=300)
plt.close()

# 2. Манчестерское кодирование
signal_manchester = manchester_encode(bits)
plt.figure(figsize=(12, 3))
plt.step(time, signal_manchester, where='post')
plt.title('Манчестерское кодирование')
plt.ylabel('Уровень')
plt.xlabel('Время')
plt.ylim(-0.5, 1.5)
plt.grid(True)
plt.tight_layout()
plt.savefig('manchester_diagram.png', dpi=300)
plt.close()

# 3. Дифференциальное манчестерское кодирование
signal_diff_manchester = differential_manchester_encode(bits)
plt.figure(figsize=(12, 3))
plt.step(time, signal_diff_manchester, where='post')
plt.title('Дифференциальное манчестерское кодирование')
plt.ylabel('Уровень')
plt.xlabel('Время')
plt.ylim(-0.5, 1.5)
plt.grid(True)
plt.tight_layout()
plt.savefig('diff_manchester_diagram.png', dpi=300)
plt.close()

# 4. MLT-3
signal_mlt3 = mlt3_encode(bits)
plt.figure(figsize=(12, 3))
plt.step(time, signal_mlt3, where='post')
plt.title('Кодирование MLT-3')
plt.ylabel('Уровень')
plt.xlabel('Время')
plt.ylim(-1.5, 1.5)
plt.grid(True)
plt.tight_layout()
plt.savefig('mlt3_diagram.png', dpi=300)
plt.close()
