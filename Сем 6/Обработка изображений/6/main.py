import cv2
img = cv2.imread('img.jpg')
crop = img[329:509, 564:789]
h, w = crop.shape[:2]
print(w, h)
sift = cv2.SIFT_create()
kp1, des1 = sift.detectAndCompute(cv2.cvtColor(img, cv2.COLOR_BGR2GRAY), None)
kp2, des2 = sift.detectAndCompute(cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY), None)
m = min(cv2.BFMatcher().match(des2, des1), key=lambda x: x.distance)
x, y = map(int, map(round, kp1[m.trainIdx].pt))
print(x, y)
b, g, r = img[y, x]
print(b, g, r)
