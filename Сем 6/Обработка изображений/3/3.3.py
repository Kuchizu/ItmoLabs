import cv2
import numpy as np

img = cv2.imread("image3.jpg")

img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

norm_img = np.zeros_like(img, dtype=np.uint8)

for i in range(3):
    ch = img[:, :, i]
    ch_min, ch_max = ch.min(), ch.max()
    ch_norm = ((ch - ch_min) / (ch_max - ch_min) * 255).astype(np.uint8)
    norm_img[:, :, i] = ch_norm

r, g, b = norm_img[434, 663]
print(r)
print(g)
print(b)
