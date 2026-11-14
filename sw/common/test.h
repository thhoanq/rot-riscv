#ifndef _ROT_TEST_H
#define _ROT_TEST_H

#include <stdint.h>

void check(const char *name, int ok, unsigned long detail);
int finish(const char *prog);
int same64(const uint64_t *a, const uint64_t *b, uint32_t n);
void diff64(const uint64_t *got, const uint64_t *exp, uint32_t n);

#endif
