#include "bmp.h"
#include "image.h"
#include <stdio.h>
#include <stdlib.h>

enum read_status from_bmp(FILE* in, struct image* img) {
    struct bmp_header header;
    if (fread(&header, sizeof(struct bmp_header), 1, in) != 1) {
        return READ_INVALID_HEADER;
    }

    if (header.bfType != 0x4D42) {
        return READ_INVALID_SIGNATURE;
    }

    if (header.biBitCount != 24) {
        return READ_INVALID_BITS;
    }

    img->width = header.biWidth;
    img->height = header.biHeight;

    size_t padding = (4 - (img->width * sizeof(struct pixel)) % 4) % 4;
    img->data = malloc(img->height * (img->width * sizeof(struct pixel) + padding));

    if (img->data == NULL) {
        return READ_INVALID_HEADER;
    }

    for (uint32_t i = 0; i < img->height; i++) {
        if (fread(img->data + i * img->width, sizeof(struct pixel), img->width, in) != img->width) {
            free(img->data);
            return READ_INVALID_HEADER;
        }
        if (padding > 0x7fffffffL) {
            free(img->data);
            return READ_ERROR; // Or a more specific error code
        }
        if (fseek(in, (long)padding, SEEK_CUR) != 0) {
            free(img->data);
            return READ_ERROR;
        }
    }

    return READ_OK;
}

enum write_status to_bmp(FILE* out, struct image const* img) {
    size_t padding = (4 - (img->width * sizeof(struct pixel)) % 4) % 4;
    uint32_t rowSize = img->width * sizeof(struct pixel) + padding;

    struct bmp_header header = {
            .bfType = 0x4D42,
            .bfileSize = sizeof(struct bmp_header) + img->height * rowSize,
            .bfReserved = 0,
            .bOffBits = sizeof(struct bmp_header),
            .biSize = 40,
            .biWidth = img->width,
            .biHeight = img->height,
            .biPlanes = 1,
            .biBitCount = 24,
            .biCompression = 0,
            .biSizeImage = img->height * rowSize,
            .biXPelsPerMeter = 0,
            .biYPelsPerMeter = 0,
            .biClrUsed = 0,
            .biClrImportant = 0
    };

    if (fwrite(&header, sizeof(struct bmp_header), 1, out) != 1) {
        return WRITE_ERROR;
    }

    char pad[3] = {0, 0, 0};

    for (uint32_t i = 0; i < img->height; i++) {
        if (fwrite(img->data + i * img->width, sizeof(struct pixel), img->width, out) != img->width) {
            return WRITE_ERROR;
        }
        if (padding > 0) {
            if (fwrite(pad, padding, 1, out) != 1) {
                return WRITE_ERROR;
            }
        }
    }

    return WRITE_OK;
}
