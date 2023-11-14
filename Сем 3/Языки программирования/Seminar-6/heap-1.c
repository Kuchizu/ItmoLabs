/* heap-1.c */

#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#define HEAP_BLOCKS 16
#define BLOCK_CAPACITY 1024

enum block_status { BLK_FREE = 0, BLK_ONE, BLK_FIRST, BLK_CONT, BLK_LAST };

struct heap {
  struct block {
    char contents[BLOCK_CAPACITY];
  } blocks[HEAP_BLOCKS];
  enum block_status status[HEAP_BLOCKS];
} global_heap = {0};

struct block_id {
  size_t       value;
  bool         valid;
  struct heap* heap;
};

struct block_id block_id_new(size_t value, struct heap* from) {
  return (struct block_id){.valid = true, .value = value, .heap = from};
}
struct block_id block_id_invalid() {
  return (struct block_id){.valid = false};
}

bool block_id_is_valid(struct block_id bid) {
  return bid.valid && bid.value < HEAP_BLOCKS;
}

/* Find block */

bool block_is_free(struct block_id bid) {
  if (!block_id_is_valid(bid))
    return false;
  return bid.heap->status[bid.value] == BLK_FREE;
}

/* Allocate */
//? ? ?
struct block_id block_allocate(struct heap* heap, size_t size) {
  if (size == 0 || size > HEAP_BLOCKS) return block_id_invalid();

  for (size_t start = 0; start <= HEAP_BLOCKS - size; ++start) {
    // Check for enough consecutive free blocks
    bool can_allocate = true;
    for (size_t i = 0; i < size; ++i) {
      if (heap->status[start + i] != BLK_FREE) {
        can_allocate = false;
        break;
      }
    }

    // Allocate blocks
    if (can_allocate) {
      for (size_t i = 0; i < size; ++i) {
        if (size == 1) {
          heap->status[start + i] = BLK_ONE; // Handle single block allocation
        } else {
          heap->status[start + i] = (i == 0) ? BLK_FIRST : (i == size - 1) ? BLK_LAST : BLK_CONT;
        }
      }
      return block_id_new(start, heap);
    }
  }

  return block_id_invalid();
}


/* Free */
void block_free(struct block_id b) {
  if (!block_id_is_valid(b)) return;

  size_t index = b.value;
  enum block_status status = b.heap->status[index];

  while (status != BLK_FREE && index < HEAP_BLOCKS) {
    b.heap->status[index] = BLK_FREE;
    if (status == BLK_LAST || status == BLK_ONE) {
      break;
    }
    index++;
    status = b.heap->status[index];
  }
}



/* Printer */
const char* block_repr(struct block_id b) {
  static const char* const repr[] = {[BLK_FREE] = " .",
                                     [BLK_ONE] = " *",
                                     [BLK_FIRST] = "[=",
                                     [BLK_LAST] = "=]",
                                     [BLK_CONT] = " ="};
  if (b.valid)
    return repr[b.heap->status[b.value]];
  else
    return "INVALID";
}

void block_debug_info(struct block_id b, FILE* f) {
  fprintf(f, "%s", block_repr(b));
}

void block_foreach_printer(struct heap* h, size_t count,
                           void printer(struct block_id, FILE* f), FILE* f) {
  for (size_t c = 0; c < count; c++)
    printer(block_id_new(c, h), f);
}

void heap_debug_info(struct heap* h, FILE* f) {
  block_foreach_printer(h, HEAP_BLOCKS, block_debug_info, f);
  fprintf(f, "\n");
}
/*  -------- */

void test_single_block_allocation_and_free() {
  printf("Test: Single Block Allocation and Free\n");

  struct block_id bid = block_allocate(&global_heap, 1);
  assert(block_id_is_valid(bid));
  assert(global_heap.status[bid.value] == BLK_ONE);

  block_free(bid);
  assert(global_heap.status[bid.value] == BLK_FREE);

  printf("Test Passed\n");
}
void test_multiple_blocks_allocation() {
  printf("Test: Multiple Blocks Allocation\n");

  struct block_id bid = block_allocate(&global_heap, 3);
  assert(block_id_is_valid(bid));
  assert(global_heap.status[bid.value] == BLK_FIRST);
  assert(global_heap.status[bid.value + 1] == BLK_CONT);
  assert(global_heap.status[bid.value + 2] == BLK_LAST);

  block_free(bid);
  for (size_t i = 0; i < 3; ++i) {
    assert(global_heap.status[bid.value + i] == BLK_FREE);
  }

  printf("Test Passed\n");
}
void test_allocation_exceeding_capacity() {
  printf("Test: Allocation Exceeding Capacity\n");

  struct block_id bid = block_allocate(&global_heap, HEAP_BLOCKS + 1);
  assert(!block_id_is_valid(bid));

  printf("Test Passed\n");
}
void test_allocation_after_free() {
  printf("Test: Allocation After Free\n");

  struct block_id bid1 = block_allocate(&global_heap, 2);
  block_free(bid1);

  struct block_id bid2 = block_allocate(&global_heap, 2);
  assert(block_id_is_valid(bid2));
  assert(global_heap.status[bid2.value] == BLK_FIRST);
  assert(global_heap.status[bid2.value + 1] == BLK_LAST);

  block_free(bid2);

  printf("Test Passed\n");
}


int main() {
  heap_debug_info(&global_heap, stdout);
  test_single_block_allocation_and_free();
  test_multiple_blocks_allocation();
  test_allocation_exceeding_capacity();
  test_allocation_after_free();
  return 0;
}

