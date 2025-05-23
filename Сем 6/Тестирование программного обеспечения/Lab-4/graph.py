import pandas as pd
import matplotlib.pyplot as plt

FILE = "conf2_results.csv"          # путь к CSV‑файлу JMeter

# ──────────────────── Чтение и подготовка данных ────────────────────
df = (
    pd.read_csv(FILE)
      .assign(timestamp=lambda d: pd.to_datetime(d["timeStamp"], unit="ms"))
      .set_index("timestamp")
)

# Число завершённых запросов в каждую секунду
throughput = df.resample("1S").size()        # .size() быстрее, чем .count()

# ─────────────────────────── Визуализация ───────────────────────────
fig, ax = plt.subplots(figsize=(10, 4), dpi=110)

throughput.plot(ax=ax, lw=1)

ax.set(
    title="Throughput over time – conf2",
    xlabel="Time",
    ylabel="Requests / second"
)
ax.grid(True, alpha=0.3)
plt.tight_layout()
plt.show()
