import cv2
import numpy as np
img = cv2.imread('bird-9950_1280.jpg')
lap = cv2.Laplacian(img, cv2.CV_64F)
sum_lap = np.abs(lap).sum()
edges = cv2.Canny(img, 100, 200)
sum_edges = edges.sum()
print(sum_lap)
print(sum_edges)
