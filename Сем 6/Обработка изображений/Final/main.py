import cv2
import numpy as np
import pywt

img = cv2.imread('ray-hennessy-6-JIDCnZG2E-unsplash.jpg')
img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
h0, w0 = img_rgb.shape[:2]
ratio = w0 / h0
new_w = 2333
new_h = int(np.floor(h0 * new_w / w0))
resized = cv2.resize(img_rgb, (new_w, new_h), interpolation=cv2.INTER_AREA)

hist_r, _ = np.histogram(resized[:, :, 0].ravel(), bins=256, range=(0, 255), density=True)
hist_g, _ = np.histogram(resized[:, :, 1].ravel(), bins=256, range=(0, 255), density=True)
hist_b, _ = np.histogram(resized[:, :, 2].ravel(), bins=256, range=(0, 255), density=True)
max_density = max(hist_r.max(), hist_g.max(), hist_b.max())

norm = resized.astype(np.float32)
for c in range(3):
    ch = norm[:, :, c]
    norm[:, :, c] = (ch - ch.min()) * 255 / (ch.max() - ch.min())
norm = norm.astype(np.uint8)
R, G, B = norm[798, 1136]

gray = cv2.cvtColor(norm, cv2.COLOR_RGB2GRAY).astype(np.float32)
cA, _ = pywt.dwt2(gray, 'haar')
cA1, _ = pywt.dwt2(cA, 'haar')
mask = np.abs(cA1) < 80
count_zeroed = int(mask.sum())

print(f"{ratio:.3f}")
print(new_h)
print(f"{max_density:.4f}")
print(R)
print(G)
print(B)
print(count_zeroed)
