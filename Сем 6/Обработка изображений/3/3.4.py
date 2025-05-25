import cv2, glob, re, numpy as np
from skimage.metrics import structural_similarity as ssim

img = cv2.cvtColor(cv2.imread('image4.jpg'), cv2.COLOR_BGR2RGB)
detail = cv2.absdiff(img, cv2.GaussianBlur(img, (7, 7), 0))

best, best_id = -1, None
for path in glob.glob('samples/*.jpg'):
    ref = cv2.cvtColor(cv2.imread(path), cv2.COLOR_BGR2RGB)
    score = ssim(detail, ref, channel_axis=2)
    if score > best:
        best, best_id = score, path

print(best_id)
