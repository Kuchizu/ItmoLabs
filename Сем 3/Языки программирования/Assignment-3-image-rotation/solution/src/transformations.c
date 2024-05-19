#include "image.h"
#include <stdlib.h>

struct image create_empty_image(uint64_t width, uint64_t height) {
    struct image img;
    img.width = width;
    img.height = height;
    img.data = malloc(width * height * sizeof(struct pixel));
    return img;
}

struct image rotate_90(struct image const source) {
    struct image rotated = create_empty_image(source.height, source.width);
    if (rotated.data == NULL) {
        return rotated;
    }

    for (uint64_t y = 0; y < source.height; y++) {
        for (uint64_t x = 0; x < source.width; x++) {
            rotated.data[y + (source.width - x - 1) * rotated.width] = source.data[x + y * source.width];
        }
    }

    return rotated;
}


struct image rotate_180(struct image const source) {
    struct image rotated = create_empty_image(source.width, source.height);
    if (rotated.data == NULL) {
        return rotated;
    }

    for (uint64_t y = 0; y < source.height; y++) {
        for (uint64_t x = 0; x < source.width; x++) {
            rotated.data[(rotated.width - x - 1) + (rotated.height - y - 1) * rotated.width] = source.data[x + y * source.width];
        }
    }

    return rotated;
}

struct image rotate_270(struct image const source) {
    struct image rotated = create_empty_image(source.height, source.width);
    if (rotated.data == NULL) {
        return rotated;
    }

    for (uint64_t y = 0; y < source.height; y++) {
        for (uint64_t x = 0; x < source.width; x++) {
            rotated.data[(rotated.width - y - 1) + x * rotated.width] = source.data[x + y * source.width];
        }
    }

    return rotated;
}

struct image rotate(struct image const source, int angle) {
    switch (angle) {
        case 90:
        case -270:
            return rotate_90(source);
        case 180:
        case -180:
            return rotate_180(source);
        case 270:
        case -90:
            return rotate_270(source);
        case 0: {
            struct image temp_img = rotate_180(source);
            struct image result_img = rotate_180(temp_img);
            free(temp_img.data);
            return result_img;
        }

        default:
            return source;
    }
}
