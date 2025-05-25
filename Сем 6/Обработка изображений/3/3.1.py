import cv2
import numpy as np

img = cv2.imread("image1.jpg", cv2.IMREAD_GRAYSCALE)
hist = cv2.calcHist([img], [0], None, [256], [0,256])
hist = hist.flatten()
max_val = int(hist.max())
max_idx = int(hist.argmax())
print(max_val)
print(max_idx)
