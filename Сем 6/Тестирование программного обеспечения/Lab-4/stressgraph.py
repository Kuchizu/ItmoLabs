import sys, pathlib, pandas as pd, matplotlib.pyplot as plt

CSV_FILE = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "stress_conf2.csv")
if not CSV_FILE.exists():
    raise FileNotFoundError(CSV_FILE)

# ── чтение ───────────────────────────────────────────────────────
df = pd.read_csv(CSV_FILE)

# ── базовая диагностика (выводит максимум и количество неуспешных) ─
print("max elapsed  :", df['elapsed'].max(), "мс")
print("failed rows  :", (~df['success']).sum())

high_elapsed = df[df['elapsed'] > 10_000]
print(f"\nRows with elapsed > 20000 ms: {len(high_elapsed)}")
print(high_elapsed[["timeStamp", "elapsed", "success", "failureMessage"]].to_string(index=False))

# ── очистка: только успешные и без экстремумов >10 000 мс ─────────
df = df[df['success']]          # берём только success==True
df = df[df['elapsed'] < 20_000] # отсекаем крайние выбросы

# ── переименования и агрегирование ───────────────────────────────
df = df.rename(columns={'allThreads': 'load', 'elapsed': 'rt_ms'})
agg = (df.groupby('load')['rt_ms']
         .agg(avg_ms='mean',
              median_ms='median',
              p95_ms=lambda x: x.quantile(.95))
         .reset_index()
         .sort_values('load'))

SLA = 890
breach = agg.query('p95_ms > @SLA').head(1)

# ── визуализация ─────────────────────────────────────────────────
fig, ax = plt.subplots(figsize=(9,5), dpi=110)
ax.scatter(df['load'], df['rt_ms'], s=8, alpha=.25, label='отдельные запросы')
ax.plot(agg['load'], agg['p95_ms'], 'o-', lw=2, label='95‑й перцентиль', color='tab:red')
ax.plot(agg['load'], agg['avg_ms'], 'd-', lw=1, label='среднее', color='tab:blue')
ax.axhline(SLA, color='black', ls='--', lw=1, label=f'SLA {SLA} мс')

if not breach.empty:
    x, y = breach.iloc[0][['load','p95_ms']]
    ax.annotate(f'нарушение SLA\n{x:.0f} users', xy=(x,y),
                xytext=(x+0.5, y+100), arrowprops=dict(arrowstyle='->', color='red'),
                color='red', fontsize=9)

ax.set_title(f'Зависимость времени отклика от нагрузки\n{CSV_FILE.name}')
ax.set_xlabel('Параллельные пользователи')
ax.set_ylabel('Время отклика, мс')
ax.grid(alpha=.3)
ax.legend(loc='upper left')
plt.tight_layout()
plt.show()

# ── мини‑отчёт в консоль ─────────────────────────────────────────
if breach.empty:
    print(f'SLA {SLA} мс не нарушен')
else:
    print(f'SLA {SLA} мс впервые превышен при ~{int(breach.load)} пользователях')
