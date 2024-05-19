#include "bmp.h"
#include "image.h"
#include "transformations.h"
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char* argv[]) {
    if (argc != 4) {
        fprintf(stderr, "Usage: %s <source-image.bmp> <transformed-image.bmp> <angle>\n", argv[0]);
        return 1;
    }

    const char* source_filename = argv[1];
    const char* transformed_filename = argv[2];
    int angle = atoi(argv[3]);

    if (angle != 0 && angle != 90 && angle != -90 && angle != 180 && angle != -180 && angle != 270 && angle != -270) {
        fprintf(stderr, "Unsupported angle. Use one of the following: 90, -90, 180, -180, 270, -270\n");
        return 1;
    }

    FILE* source_file = fopen(source_filename, "rb");
    if (!source_file) {
        perror("Failed to open source file");
        return 1;
    }

    struct image img;
    enum read_status read_result = from_bmp(source_file, &img);
    fclose(source_file);

    if (read_result != READ_OK) {
        fprintf(stderr, "Error reading BMP file\n");
        return 1;
    }

    struct image transformed_img = rotate(img, angle);
    free(img.data);

    FILE* transformed_file = fopen(transformed_filename, "wb");
    if (!transformed_file) {
        perror("Failed to open/create transformed file");
        free(transformed_img.data);
        return 1;
    }

    enum write_status write_result = to_bmp(transformed_file, &transformed_img);
    fclose(transformed_file);

    if (write_result != WRITE_OK) {
        fprintf(stderr, "Error writing BMP file\n");
        free(transformed_img.data);
        return 1;
    }

    free(transformed_img.data);
    printf("Image successfully transformed and saved to %s\n", transformed_filename);
    return 0;
}
