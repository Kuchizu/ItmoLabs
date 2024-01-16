#define _DEFAULT_SOURCE

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "mem_internals.h"
#include "mem.h"
#include "util.h"

void debug_block(struct block_header* b, const char* fmt, ... );
void debug(const char* fmt, ... );

extern inline block_size size_from_capacity( block_capacity cap );
extern inline block_capacity capacity_from_size( block_size sz );

static bool            block_is_big_enough( size_t query, struct block_header* block ) { return block->capacity.bytes >= query; }
static size_t          pages_count   ( size_t mem )                      { return mem / getpagesize() + ((mem % getpagesize()) > 0); }
static size_t          round_pages   ( size_t mem )                      { return getpagesize() * pages_count( mem ) ; }

static void block_init( void* restrict addr, block_size block_sz, void* restrict next ) {
    *((struct block_header*)addr) = (struct block_header) {
            .next = next,
            .capacity = capacity_from_size(block_sz),
            .is_free = true
    };
}

static size_t region_actual_size( size_t query ) { return size_max( round_pages( query ), REGION_MIN_SIZE ); }

extern inline bool region_is_invalid( const struct region* r );

static void* map_pages(void const* addr, size_t length, int additional_flags) {
    return mmap( (void*) addr, length, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS | additional_flags , -1, 0 );
}

/*  Аллоцировать регион памяти и инициализировать его блоком */
static struct region alloc_region(void const *addr, size_t query) {
    size_t region_size = region_actual_size(query + offsetof(struct block_header, contents));

    void *mapped = map_pages(addr, region_size, MAP_FIXED_NOREPLACE);
    bool extends = true;

    if (mapped == MAP_FAILED) {
        extends = false;
        mapped = map_pages(addr, region_size, 0);
        if (mapped == MAP_FAILED) {
            return REGION_INVALID;
        }
    }
    block_init(mapped, (block_size) { .bytes = region_size }, NULL);

    return (struct region) { .addr = mapped, .size = region_size, .extends = extends };
}


static void *block_after(struct block_header const *block);

static bool blocks_continuous (
        struct block_header const* fst,
        struct block_header const* snd ) {
    return (void*)snd == block_after(fst);
}

void* heap_init( size_t initial ) {
    const struct region region = alloc_region(HEAP_START, initial);
    if (region_is_invalid(&region)) return NULL;

    return region.addr;
}

/*  Освободить всю память, выделенную под кучу */
void heap_term() {
    struct block_header* block = (struct block_header*) HEAP_START;
    void* region_start = HEAP_START;
    size_t region_size = 0;

    while (block != NULL) {
        region_size += size_from_capacity(block -> capacity).bytes;
        struct block_header* next_block = block -> next;

        if (next_block == NULL || !blocks_continuous(block, next_block)) {
            munmap(region_start, region_size);
            region_start = next_block;
            region_size = 0;
        }

        block = next_block;
    }
}

#define BLOCK_MIN_CAPACITY 24

/*  --- Разделение блоков (если найденный свободный блок слишком большой) --- */

static bool block_splittable( struct block_header* restrict block, size_t query) {
    return block-> is_free && query + offsetof( struct block_header, contents ) + BLOCK_MIN_CAPACITY <= block->capacity.bytes;
}

static void split_block(struct block_header *block, size_t query) {
    block_capacity block1_capacity = {size_max(query, BLOCK_MIN_CAPACITY)};
    struct block_header *block2_addr = (struct block_header *)(block->contents + block1_capacity.bytes);
    size_t remaining_capacity = block->capacity.bytes - block1_capacity.bytes;

    block_init(block2_addr, (block_size){remaining_capacity}, block->next);
    block2_addr->is_free = block->is_free;

    block_init(block, size_from_capacity(block1_capacity), block2_addr);
    block->is_free = block->is_free;
}

static bool split_if_too_big(struct block_header *block, size_t query) {
    if (!block_splittable(block, query)) return false;
    split_block(block, query);
    return true;
}


/*  --- Слияние соседних свободных блоков --- */

static void* block_after( struct block_header const* block ) {
    return  (void*) (block->contents + block->capacity.bytes);
}

static bool mergeable(struct block_header const* restrict fst, struct block_header const* restrict snd) {
    return fst->is_free && snd->is_free && blocks_continuous( fst, snd ) ;
}

static bool try_merge_with_next(struct block_header* block) {
    if (!block || !block -> next || !mergeable(block, block -> next)) {
        return false;
    }

    size_t new_size = size_from_capacity(block->capacity).bytes + size_from_capacity(block->next->capacity).bytes;
    block_init(block, (block_size) {.bytes = new_size}, block->next->next);

    return true;
}

/*  --- ... ecли размера кучи хватает --- */

struct block_search_result {
    enum {BSR_FOUND_GOOD_BLOCK, BSR_REACHED_END_NOT_FOUND, BSR_CORRUPTED} type;
    struct block_header* block;
};

static void merge_blocks(struct block_header *current) {
    while (current->next && try_merge_with_next(current)) {
    }
}

static struct block_search_result find_good_or_last(struct block_header *block, size_t sz) {
    if (!block) {
        return (struct block_search_result) { .type = BSR_CORRUPTED, .block = NULL };
    }

    struct block_header *current = block;
    struct block_header *last_valid = NULL;

    while (current) {
        merge_blocks(current);

        if (current->is_free && block_is_big_enough(sz, current)) {
            return (struct block_search_result) { .type = BSR_FOUND_GOOD_BLOCK, .block = current };
        }

        last_valid = current;
        current = current->next;
    }

    return (struct block_search_result) { .type = BSR_REACHED_END_NOT_FOUND, .block = last_valid };
}


/*  Попробовать выделить память в куче начиная с блока `block` не пытаясь расширить кучу
 Можно переиспользовать как только кучу расширили. */
static struct block_search_result try_memalloc_existing(size_t query, struct block_header *block) {
    struct block_search_result result = find_good_or_last(block, query);
    if (result.type == BSR_FOUND_GOOD_BLOCK && split_if_too_big(result.block, query)) {
        result.block->is_free = false;
    }
    return result;
}

static struct block_header *grow_heap(struct block_header *restrict last, size_t query) {
    if (!last) return NULL;

    struct region new_region = alloc_region(block_after(last), query);
    if (region_is_invalid(&new_region)) return NULL;

    last->next = new_region.addr;
    return (try_merge_with_next(last)) ? last : last->next;
}

/*  Реализует основную логику malloc и возвращает заголовок выделенного блока */
static struct block_header *memalloc(size_t query, struct block_header *heap_start) {
    if (!heap_start) return NULL;

    query = size_max(query, BLOCK_MIN_CAPACITY);
    struct block_search_result result = try_memalloc_existing(query, heap_start);

    if (result.type == BSR_REACHED_END_NOT_FOUND) {
        struct block_header *new_block = grow_heap(result.block, query);
        if (new_block) {
            result = try_memalloc_existing(query, new_block);
        }
    }

    return (result.type == BSR_FOUND_GOOD_BLOCK) ? result.block : NULL;
}

void* _malloc( size_t query ) {
    struct block_header* const addr = memalloc( query, (struct block_header*) HEAP_START );
    if (addr) return addr->contents;
    else return NULL;
}

static struct block_header* block_get_header(void* contents) {
    return (struct block_header*) (((uint8_t*)contents)-offsetof(struct block_header, contents));
}

void _free( void* mem ) {
    if (!mem) return;
    struct block_header* header = block_get_header(mem);
    header->is_free = true;
    merge_blocks(header);
}
