import cv2, sys, numpy as np

g = cv2.imread(sys.argv[1], 0).astype(np.float32)
F = np.fft.fft2(g)

print(f'{F[711,1215].real:.2f}')
print(f'{F[711,1215].imag:.2f}')

amp = np.abs(np.fft.fftshift(F))
print(f'{amp.min():.2f}')
print(f'{amp.max():.2f}')

log_amp = np.log1p(amp)
print(f'{(log_amp.max() - log_amp.min()):.2f}')

cr, cc = g.shape[0] // 2, g.shape[1] // 2
F_shift = np.fft.fftshift(F)
F_shift[cr-40:cr+40, cc-40:cc+40] = 0
inv = np.fft.ifft2(np.fft.ifftshift(F_shift))

print(f'{np.abs(inv[603,440]):.2f}')
