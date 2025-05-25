import cv2

img = cv2.imread("image3.jpg")
h, w = img.shape[:2]
k = w / h
new_w = 710
new_h = int(new_w / k)
resized = cv2.resize(img, (new_w, new_h), interpolation=cv2.INTER_AREA)
rgb = cv2.cvtColor(resized, cv2.COLOR_BGR2RGB)
cv2.imwrite("resized_rgb.png", cv2.cvtColor(rgb, cv2.COLOR_RGB2BGR))
print(f"{k:.3f}")
print(new_h)
print(open("resized_rgb.png", "rb").read().__sizeof__())
