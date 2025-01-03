import matplotlib.pyplot as plt
import pandas as pd

# Загрузка данных для потоков
threads_data = pd.read_csv("threads_stat.csv")

# Загрузка данных для процессов
processes_data = pd.read_csv("processes_stat_alternative.csv")

# Создание графика
plt.figure(figsize=(10, 6))

# Данные для потоков
plt.plot(
    threads_data['Threads'],
    threads_data['ContextSwitches'],
    marker='o',
    linestyle='-',
    label='Threads'
)

# Данные для процессов
plt.plot(
    processes_data['Processes'],
    processes_data['ContextSwitches'],
    marker='s',
    linestyle='--',
    label='Processes'
)

# Настройка графика
plt.xlabel('Number of Threads/Processes')
plt.ylabel('Context Switches')
plt.title('Context Switches: Threads vs Processes')
plt.grid()
plt.legend()

# Сохранение и отображение графика
plt.savefig('context_switches_comparison3.png')
plt.show()
