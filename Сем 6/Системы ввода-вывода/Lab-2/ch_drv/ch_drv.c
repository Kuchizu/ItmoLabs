#include <linux/module.h>
#include <linux/version.h>
#include <linux/kernel.h>
#include <linux/types.h>
#include <linux/kdev_t.h>
#include <linux/fs.h>
#include <linux/device.h>
#include <linux/cdev.h>
#include <linux/uaccess.h>
#include <linux/slab.h>

#define DEVICE_NAME "var1"
#define MAX_RECORDS 100
#define MAX_STR_LEN 256
typedef struct {
	int chars;
	char strings[MAX_STR_LEN];
} entries_t;

entries_t entries[MAX_RECORDS];

static int eidx = 0;

static dev_t first;
static struct cdev c_dev;
static struct class *cl;

static int my_open(struct inode *i, struct file *f)
{
  printk(KERN_INFO "Driver: open()\n");
  return 0;
}

static int my_close(struct inode *i, struct file *f)
{
  printk(KERN_INFO "Driver: close()\n");
  return 0;
}

static ssize_t my_read(struct file *f, char __user *buf, size_t len, loff_t *off)
{
	int k,bytes;
	char *obuf;
	size_t obuf_sz = 1024;
	size_t lenb = 0;

	if (*off > 0) {
		return 0;
	}

	obuf = kmalloc(obuf_sz, GFP_KERNEL);
	if (!obuf) {
		return -ENOMEM;
	}

	for (k = 0; k < eidx && lenb < obuf_sz - 50; k++) {
		bytes = snprintf(obuf + lenb, obuf_sz - lenb, 
				"String length \"%s\": %d\n", entries[k].strings, entries[k].chars);

		if (bytes > 0) {
			lenb += bytes;
		} else {
			break;
		}
	}

	if (eidx == 0) {
		bytes = snprintf(obuf, obuf_sz, "No records.\n");
		if (bytes > 0) {
			lenb = bytes;
		}
	}

	if (copy_to_user(buf, obuf, lenb)) {
		kfree(obuf);
		return -EFAULT;
	}

	*off += lenb;
	kfree(obuf);

	return lenb;
}

static ssize_t my_write(struct file *f, const char __user *buf,  size_t len, loff_t *off)
{

  int i,cnt=0;
  char *kbuf;
  printk(KERN_INFO "Driver: write()\n");

  kbuf = kmalloc(len + 1, GFP_KERNEL);
  if(copy_from_user(kbuf, buf, len) != 0) {
      kfree(kbuf);
      return -EFAULT;
  }
  
  kbuf[len] = '\0';
  for (i = 0; i < len; i++) {
	      cnt++;
  }

  strncpy(entries[eidx].strings, kbuf, MAX_STR_LEN - 1);
  entries[eidx].strings[MAX_STR_LEN - 1] = '\0';
  entries[eidx].chars = cnt;
  eidx++;

  printk(KERN_INFO "Driver: Saved string with length %d\n", cnt);

  kfree(kbuf);

  return len;
}

static struct file_operations mychdev_fops =
{
  .owner = THIS_MODULE,
  .open = my_open,
  .release = my_close,
  .read = my_read,
  .write = my_write
};
 
static int __init ch_drv_init(void)
{
    printk(KERN_INFO "Hello!\n");
    if (alloc_chrdev_region(&first, 0, 1, "ch_dev") < 0)
	  {
		return -1;
	  }
    if ((cl = class_create(THIS_MODULE, "chardrv")) == NULL)
	  {
		unregister_chrdev_region(first, 1);
		return -1;
	  }
    if (device_create(cl, NULL, first, NULL, DEVICE_NAME) == NULL)
	  {
		class_destroy(cl);
		unregister_chrdev_region(first, 1);
		return -1;
	  }
    cdev_init(&c_dev, &mychdev_fops);
    if (cdev_add(&c_dev, first, 1) == -1)
	  {
		device_destroy(cl, first);
		class_destroy(cl);
		unregister_chrdev_region(first, 1);
		return -1;
	  }
    return 0;
}
 
static void __exit ch_drv_exit(void)
{
    cdev_del(&c_dev);
    device_destroy(cl, first);
    class_destroy(cl);
    unregister_chrdev_region(first, 1);
    printk(KERN_INFO "Bye!!!\n");
}
 
module_init(ch_drv_init);
module_exit(ch_drv_exit);
 
MODULE_LICENSE("GPL");
MODULE_AUTHOR("Author");
MODULE_DESCRIPTION("The first kernel module");

