from ultralytics import YOLO
from pathlib import Path

IMDIR = Path("data")            # папка с 10 картинками
MODEL_PATH = "yolov8n.pt"       # стандартные веса
CONF = 0.80
PERSON = 0

model = YOLO(MODEL_PATH)

TP = FP = FN = 0
for p in sorted(IMDIR.glob("*.jpg")):
    real = int(input(f"Сколько людей на {p.name}? "))        # экспертная разметка

    # число рамок‑«людей» (Python int, не Tensor)
    det = sum(
        1
        for b in model(p)[0].boxes
        if int(b.cls) == PERSON and b.conf >= CONF
    )

    TP += min(det, real)
    FP += max(det - real, 0)
    FN += max(real - det, 0)

precision = TP / (TP + FP) if TP + FP else 0.0
recall    = TP / (TP + FN) if TP + FN else 0.0
f1        = 2 * precision * recall / (precision + recall) if precision + recall else 0.0

print("\n===== Итог для Moodle =====")
print(f"TP = {TP}")
print(f"FP = {FP}")
print(f"FN = {FN}")
print("TN = 0")
print(f"F1 = {f1:.3f}")
