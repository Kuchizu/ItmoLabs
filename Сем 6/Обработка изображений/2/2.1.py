import cv2

img = cv2.imread("image.jpg")
h, w, _ = img.shape
print(w)
print(h)

b, g, r = img[1520, 2287]
print(r)
print(g)
print(b)
