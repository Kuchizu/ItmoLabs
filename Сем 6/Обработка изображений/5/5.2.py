import cv2, pywt, numpy as np

img = cv2.imread('russia-1927758_1280.jpg', 0)
cA, _ = pywt.dwt2(img, 'haar')
cA1, _ = pywt.dwt2(cA, 'haar')
mask = np.abs(cA1) < 90
a = int(np.sum(mask))
cA1[mask] = 0
print(a)
