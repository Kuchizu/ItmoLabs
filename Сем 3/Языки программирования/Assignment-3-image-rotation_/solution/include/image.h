#ifndef IMAGE_TRANSFORMER_IMAGE_H
#define IMAGE_TRANSFORMER_IMAGE_H

#include <stdint.h>

#pragma pack(push, 1)
struct pixel {
    uint8_t b, g, r;
};

struct image {
    uint64_t width, height;
    struct pixel* data;
};
#pragma pack(pop)

#endif //IMAGE_TRANSFORMER_IMAGE_H
