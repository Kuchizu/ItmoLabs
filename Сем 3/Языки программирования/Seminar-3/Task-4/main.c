#include "headers.h"

int main(void) {
    int choice;
    char filename[256], old_name[256], new_name[256];

    while (1) {
        printf("1. Create File\n");
        printf("2. Delete File\n");
        printf("3. Rename File\n");
        printf("4. List Files\n");
        printf("5. Exit\n");
        printf("Enter your choice: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                printf("Enter filename: ");
                scanf("%255s", filename);
                create_file(filename);
                break;
            case 2:
                printf("Enter filename: ");
                scanf("%255s", filename);
                delete_file(filename);
                break;
            case 3:
                printf("Enter old filename: ");
                scanf("%255s", old_name);
                printf("Enter new filename: ");
                scanf("%255s", new_name);
                rename_file(old_name, new_name);
                break;
            case 4:
                list_files();
                break;
            case 5:
                exit(0);
            default:
                printf("Invalid choice! Try again.\n");
        }
    }

    return 0;
}