import matplotlib.pyplot as plt
import numpy as np

# Скремблированное сообщение (первые 32 бита)
scrambled_bits = [
    0,0,1,1,0,0,1,1, # Первые 8 бит
    0,1,0,1,1,0,0,1, # Следующие 8 бит
    # Продолжить для остальных битов
]

# Общие параметры для графика
bit_duration = 1
samples_per_bit = 100
time = np.arange(0, len(scrambled_bits) * bit_duration, bit_duration / samples_per_bit)

def nrzl_encode(bits):
    signal = []
    for bit in bits:
        level = bit
        signal.extend([level]*samples_per_bit)
    return signal

# Построение графика
signal_nrzl = nrzl_encode(scrambled_bits)
plt.figure(figsize=(12, 3))
plt.step(time, signal_nrzl, where='post')
plt.title('Временная диаграмма для скремблированного сообщения (NRZ-L)')
plt.ylabel('Уровень')
plt.xlabel('Время')
plt.ylim(-0.5, 1.5)
plt.grid(True)
plt.tight_layout()
plt.savefig('scrambled_diagram.png', dpi=300)
plt.show()