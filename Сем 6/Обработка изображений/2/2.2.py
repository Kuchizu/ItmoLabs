import cv2

img = cv2.imread("image2.jpg")

rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
sub = rgb[1450:2320, 2380:3190]

cv2.imwrite("cropped.png", cv2.cvtColor(sub, cv2.COLOR_RGB2BGR))
