import cv2, pywt, numpy as np

img = cv2.imread('src.jpg')
g   = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

CA, (CH, CV, CD) = pywt.dwt2(g, 'haar')

h, _ = np.histogram(CA, 256, (0, 256))
print('maxCA', h.max())          # ⇦ впиши в первое поле

def n(a): return ((a - a.min()) / (a.max() - a.min()) * 255).astype(np.uint8)
cv2.imwrite('CA.png', n(CA))
cv2.imwrite('CH.png', n(CH))
cv2.imwrite('CV.png', n(CV))
cv2.imwrite('CD.png', n(CD))
print('saved CA CH CV CD')       # открытки покажут, что куда
