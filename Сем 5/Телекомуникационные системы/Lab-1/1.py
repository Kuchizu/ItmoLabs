import matplotlib.pyplot as plt
import numpy as np

# Закодированное сообщение (первые 16 5-битных символов)
encoded_bits = [
    1,1,0,1,0,  # 11010
    1,1,0,1,1,  # 11011
    1,1,1,0,0,  # 11100
    1,1,1,0,0,  # 11100
    1,1,1,0,0,  # 11100
    0,1,0,1,0,  # 01010
    1,1,1,0,0,  # 11100
    1,0,0,1,0,  # 10010
]

# Общие параметры для графика
bit_duration = 1
samples_per_bit = 100
time = np.arange(0, len(encoded_bits) * bit_duration, bit_duration / samples_per_bit)

def nrzl_encode(bits):
    signal = []
    for bit in bits:
        level = bit
        signal.extend([level]*samples_per_bit)
    return signal

# Построение графика
signal_nrzl = nrzl_encode(encoded_bits)
plt.figure(figsize=(12, 3))
plt.step(time, signal_nrzl, where='post')
plt.title('Временная диаграмма для кодирования 4B/5B (NRZ-L)')
plt.ylabel('Уровень')
plt.xlabel('Время')
plt.ylim(-0.5, 1.5)
plt.grid(True)
plt.tight_layout()
plt.savefig('4b5b_diagram.png', dpi=300)
plt.close()
