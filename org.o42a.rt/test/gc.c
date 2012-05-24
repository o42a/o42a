/*
    Run-Time Library Tests
    Copyright (C) 2012 Ruslan Lopatin

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
#include <assert.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>

#include "o42a/memory/gc.h"

const struct o42a_dbg o42a_dbg;


//////////////////// Test object definition

typedef struct test_object test_object_t;

struct test_object {

	const char *name;

	size_t num_refs;

	const test_object_t *refs[];

};

static struct swept_objects {
	size_t num_swept;
	size_t max_swept;
	const char **names;
} swept_objects;


static void mark_test_object(void *const data) {
	O42A_ENTER(return);

	test_object_t *const object = (test_object_t *) data;
	const size_t num_refs = object->num_refs;

	for (size_t i = 0; i < num_refs; ++i) {
		O42A(o42a_gc_mark(o42a_gc_blockof(object->refs[0])));
	}

	O42A_RETURN;
}

static void sweep_test_object(void *const data) {
	O42A_ENTER(return);

	assert(
			(swept_objects.num_swept < swept_objects.max_swept)
			&& "Too many objects swept");

	test_object_t *const object = (test_object_t *) data;

	swept_objects.names[swept_objects.num_swept++] = object->name;

	O42A_RETURN;
}

static const o42a_gc_desc_t test_object_desc = {
	mark: &mark_test_object,
	sweep: &sweep_test_object
};

static test_object_t *alloc_test_object(
		const char *const name,
		const size_t num_refs) {
	O42A_ENTER(return NULL);

	test_object_t *object = (test_object_t *) O42A(o42a_gc_alloc(
			&test_object_desc,
			sizeof(test_object_t) + num_refs * sizeof(test_object_t *)));

	object->name = name;
	object->num_refs = num_refs;

	O42A_RETURN object;
}

//////////////////// Test functions

#define setup_test(max_swept) \
	const char *__swept__[max_swept]; \
	_setup_test((max_swept), (const char **) &__swept__)

static inline void _setup_test(size_t max_swept, const char **names) {
	swept_objects.num_swept = 0; \
	swept_objects.max_swept = max_swept; \
	swept_objects.names = names;
	memset(names, 0, max_swept * sizeof(char *)); \
}

static o42a_bool_t object_swept(const char *const name) {
	for (size_t i = 0; i < swept_objects.num_swept; ++i) {
		if (!strcmp(name, swept_objects.names[i])) {
			return O42A_TRUE;
		}
	}
	return O42A_FALSE;
}

//////////////////// Tests

static o42a_bool_t test_non_referenced_object() {
	O42A_ENTER(return O42A_FALSE);
	O42A_DO("Non-referenced object");

	setup_test(1);

	test_object_t * test = O42A(alloc_test_object("noref", 0));

	O42A(o42a_gc_use(o42a_gc_blockof(test)));
	assert(!object_swept("noref") && "Object deallocated after use");

	O42A(o42a_gc_run());
	assert(!object_swept("noref") && "Object deallocated while it is in use");

	O42A(o42a_gc_unuse(o42a_gc_blockof(test)));
	assert(!object_swept("noref") && "Object deallocated after unuse");

	O42A(o42a_gc_run());
	assert(object_swept("noref") && "Object not deallocated");

	O42A_DONE;
	O42A_RETURN O42A_TRUE;
}

//////////////////// Main functions

static int32_t run_tests(int32_t argc, char **argv) {
	O42A_ENTER(return 0);

	o42a_bool_t ok = O42A_TRUE;

	ok = test_non_referenced_object() & ok;

	O42A_RETURN ok ? EXIT_SUCCESS : EXIT_FAILURE;
}

int main(int argc, char **argv) {
	return o42a_dbg_exec_main(&run_tests, argc, argv);
}
