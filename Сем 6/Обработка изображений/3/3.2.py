import cv2, numpy as np, matplotlib.pyplot as plt

img=cv2.imread('image2.jpg')
img=cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
densities=[np.histogram(img[:,:,i],bins=256,range=(0,256),density=True)[0] for i in range(3)]
for d,c in zip(densities,('r','g','b')):
    plt.plot(d,color=c)
plt.xlim([0,256])
plt.show()
print(f'{max(map(np.max,densities)):.4f}')
