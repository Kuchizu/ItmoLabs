import sys, cv2, numpy as np, pywt

img = cv2.imread(sys.argv[1] if len(sys.argv) > 1 else 'img.jpg')
img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

h, w, _ = img.shape
k = w / h
print(f'{k:.3f}')

nw = 1967
nh = int(h * nw / w)
img = cv2.resize(img, (nw, nh), interpolation=cv2.INTER_AREA)
print(nh)

px = nw * nh
m = max(cv2.calcHist([c], [0], None, [256], [0, 256]).max() / px for c in cv2.split(img))
print(f'{m:.4f}')

chs = []
for c in cv2.split(img):
    cmin, cmax = c.min(), c.max()
    chs.append(((c - cmin) / (cmax - cmin) * 255).astype(np.uint8))
img = cv2.merge(chs)

print(int(img[1443, 954, 0]))
print(int(img[1443, 954, 1]))
print(int(img[1443, 954, 2]))

g = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY).astype(np.float32)
cA, _ = pywt.dwt2(g, 'haar')
cA1, _ = pywt.dwt2(cA, 'haar')
cnt = int((np.abs(cA1) < 90).sum())
print(cnt)
