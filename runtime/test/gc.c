/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
#include <assert.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>

#include "o42a/memory/gc.h"


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
	O42A_DO("Mark");

	test_object_t *const object = (test_object_t *) data;
	const size_t num_refs = object->num_refs;

	O42A_DEBUG("Mark object: %s\n", object->name);
	for (size_t i = 0; i < num_refs; ++i) {

		const test_object_t *const ref = object->refs[i];

		if (ref) {
			O42A(o42a_gc_mark(o42a_gc_blockof(ref)));
		}
	}

	O42A_DONE;
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
	.mark = &mark_test_object,
	.sweep = &sweep_test_object
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
	for (size_t i = 0; i < num_refs; ++i) {
		object->refs[i] = NULL;
	}

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

static void test_non_referenced() {
	O42A_ENTER(return);
	O42A_DO("Non-referenced");

	setup_test(1);

	test_object_t *const object = O42A(alloc_test_object("noref", 0));

	O42A(o42a_gc_use(o42a_gc_blockof(object)));
	assert(!object_swept("noref") && "Block deallocated after use");

	O42A(o42a_gc_run());
	assert(!object_swept("noref") && "Block deallocated while it is in use");

	O42A(o42a_gc_unuse(o42a_gc_blockof(object)));
	assert(!object_swept("noref") && "Block deallocated after unuse");

	O42A(o42a_gc_run());
	assert(object_swept("noref") && "Block not deallocated");

	O42A_DONE;
	O42A_RETURN;
}

static void test_statically_referenced() {
	O42A_ENTER(return);
	O42A_DO("Statically referenced");

	setup_test(1);

	test_object_t *const static_object = O42A(alloc_test_object("static", 1));

	o42a_gc_blockof(static_object)->list = 0;// Mark it static.

	O42A(o42a_gc_static(o42a_gc_blockof(static_object)));

	test_object_t *const object = O42A(alloc_test_object("object", 0));

	O42A(o42a_gc_use(o42a_gc_blockof(object)));
	static_object->refs[0] = object;
	O42A(o42a_gc_unuse(o42a_gc_blockof(object)));
	O42A(o42a_gc_run());

	assert(!object_swept("object") && "Block is deallocated while in use");

	O42A(o42a_gc_use(o42a_gc_blockof(object)));
	static_object->refs[0] = NULL;
	O42A(o42a_gc_unuse(o42a_gc_blockof(object)));
	O42A(o42a_gc_run());

	assert(object_swept("object") && "Block not deallocated");

	O42A(o42a_gc_discard(o42a_gc_blockof(static_object)));
	O42A(o42a_gc_free(o42a_gc_blockof(static_object)));

	O42A_DONE;
	O42A_RETURN;
}

static void test_referenced() {
	O42A_ENTER(return);
	O42A_DO("Referenced");

	setup_test(2);

	test_object_t *const object1 = O42A(alloc_test_object("object1", 1));

	O42A(o42a_gc_use(o42a_gc_blockof(object1)));

	test_object_t *const object2 = O42A(alloc_test_object("object2", 0));

	O42A(o42a_gc_use(o42a_gc_blockof(object2)));
	object1->refs[0] = object2;
	O42A(o42a_gc_unuse(o42a_gc_blockof(object2)));
	O42A(o42a_gc_run());

	assert(!object_swept("object1") && "Used block is deallocated");
	assert(!object_swept("object2") && "Referenced block is deallocated");

	O42A(o42a_gc_use(o42a_gc_blockof(object2)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object2)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object1)));
	O42A(o42a_gc_run());

	assert(object_swept("object1") && "Referencing block not deallocated");
	assert(object_swept("object2") && "Referenced block not deallocated");

	O42A_DONE;
	O42A_RETURN;
}

static void test_self_referencing() {
	O42A_ENTER(return);
	O42A_DO("Self-referencing");

	setup_test(1);

	test_object_t *const object = O42A(alloc_test_object("object", 1));

	object->refs[0] = object;

	O42A(o42a_gc_use(o42a_gc_blockof(object)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object)));
	O42A(o42a_gc_run());

	assert(object_swept("object") && "Self-referencing block not deallocated");

	O42A_DONE;
	O42A_RETURN;
}

static void test_cyclic_references() {
	O42A_ENTER(return);
	O42A_DO("Cyclic references");

	setup_test(3);

	test_object_t *const object1 = O42A(alloc_test_object("object1", 1));
	test_object_t *const object2 = O42A(alloc_test_object("object2", 1));
	test_object_t *const object3 = O42A(alloc_test_object("object3", 1));

	object1->refs[0] = object2;
	object2->refs[0] = object3;
	object3->refs[0] = object1;

	O42A(o42a_gc_use(o42a_gc_blockof(object1)));
	O42A(o42a_gc_use(o42a_gc_blockof(object2)));
	O42A(o42a_gc_use(o42a_gc_blockof(object3)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object3)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object2)));
	O42A(o42a_gc_run());

	assert(!object_swept("object1") && "Cyclic reference 1 deallocated");
	assert(!object_swept("object2") && "Cyclic reference 2 deallocated");
	assert(!object_swept("object3") && "Cyclic reference 3 deallocated");

	O42A(o42a_gc_unuse(o42a_gc_blockof(object1)));
	O42A(o42a_gc_run());

	assert(object_swept("object1") && "Cyclic reference 1 not deallocated");
	assert(object_swept("object2") && "Cyclic reference 2 not deallocated");
	assert(object_swept("object3") && "Cyclic reference 3 not deallocated");

	O42A_DONE;
	O42A_RETURN;
}

static void test_break_cyclic_references() {
	O42A_ENTER(return);
	O42A_DO("Break cyclic references");

	setup_test(3);

	test_object_t *const object1 = O42A(alloc_test_object("object1", 1));
	test_object_t *const object2 = O42A(alloc_test_object("object2", 1));
	test_object_t *const object3 = O42A(alloc_test_object("object3", 1));

	object1->refs[0] = object2;
	object2->refs[0] = object3;
	object3->refs[0] = object1;

	O42A(o42a_gc_use(o42a_gc_blockof(object1)));
	O42A(o42a_gc_use(o42a_gc_blockof(object2)));
	O42A(o42a_gc_use(o42a_gc_blockof(object3)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object3)));
	O42A(o42a_gc_unuse(o42a_gc_blockof(object2)));
	O42A(o42a_gc_run());

	assert(!object_swept("object1") && "Cyclic reference 1 deallocated");
	assert(!object_swept("object2") && "Cyclic reference 2 deallocated");
	assert(!object_swept("object3") && "Cyclic reference 3 deallocated");

	O42A(o42a_gc_use(o42a_gc_blockof(object2)));
	object2->refs[0] = NULL;
	O42A(o42a_gc_unuse(o42a_gc_blockof(object2)));
	O42A(o42a_gc_run());

	assert(!object_swept("object1") && "Used block deallocated");
	assert(!object_swept("object2") && "Reference deallocated");
	assert(
			object_swept("object3")
			&& "Broken cyclic reference 3 not deallocated");

	O42A(o42a_gc_unuse(o42a_gc_blockof(object1)));
	O42A(o42a_gc_run());

	assert(object_swept("object1") && "Root block not deallocated");
	assert(object_swept("object2") && "Reference not deallocated");

	O42A_DONE;
	O42A_RETURN;
}

//////////////////// Main functions

static int32_t run_tests(
		int32_t argc __attribute__((unused)),
		char **argv __attribute__((unused))) {
	O42A_ENTER(return 0);

	O42A(test_non_referenced());
	O42A(test_statically_referenced());
	O42A(test_referenced());
	O42A(test_self_referencing());
	O42A(test_cyclic_references());
	O42A(test_break_cyclic_references());

	O42A_RETURN EXIT_SUCCESS;
}

const o42a_dbg_options_t o42a_dbg_default_options = {
	.quiet = 0,
	.no_debug_messages = 0,
	.debug_blocks_omitted = 0,
	.silent_calls = 0,
};

int main(int argc, char **argv) {
	return o42a_dbg_exec_main(&run_tests, argc, argv);
}
