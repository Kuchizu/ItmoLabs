import cv2

img = cv2.imread("image5.jpg", cv2.IMREAD_GRAYSCALE)

for ksize in range(1, 50, 2):
    filtered = cv2.medianBlur(img, ksize)
    cv2.imwrite(f"filtered_{ksize}.png", filtered)
