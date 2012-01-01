/*
    Run-Time Library
    Copyright (C) 2011,2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
#ifndef O42A_MEMORY_H
#define O42A_MEMORY_H


#include "o42a/types.h"


#define o42a_mem_block(mem) \
	((o42a_mem_block_t*) (((void*) (mem)) - offsetof(o42a_mem_block_t, data)))


enum o42a_mem_flags {

	O42A_MEM_GC = 0x00,

	O42A_MEM_RC = 0x01,

};

typedef struct o42a_mem_block {

	union {

		struct {

		} gc;

		struct {

			uint64_t ref_count;

		} rc;

	} hdr;

	uint8_t flags;

	char data[0] __attribute__ ((aligned (__BIGGEST_ALIGNMENT__)));

} o42a_mem_block_t;


#ifdef __cplusplus
extern "C" {
#endif


o42a_mem_block_t *o42a_mem_alloc_rc_block(O42A_DECLS size_t);

o42a_mem_block_t *o42a_mem_alloc_gc_block(O42A_DECLS size_t);

void o42a_mem_free_block(O42A_DECLS o42a_mem_block_t *);


void *o42a_mem_alloc_rc(O42A_DECLS size_t);

void *o42a_mem_alloc_gc(O42A_DECLS size_t);

void o42a_mem_free(O42A_DECLS void *);


#ifdef __cplusplus
}
#endif


#endif /* O42A_MEMORY_H */
