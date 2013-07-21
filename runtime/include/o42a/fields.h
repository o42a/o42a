/*
    Copyright (C) 2010-2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FIELDS_H
#define O42A_FIELDS_H

#include "o42a/fld/dep.h"
#include "o42a/fld/link.h"
#include "o42a/fld/obj.h"
#include "o42a/fld/scope.h"
#include "o42a/fld/var.h"


#ifdef __cplusplus
extern "C" {
#endif


/**
 * A union of all field kinds.
 */
union o42a_fld {

	O42A_HEADER

	o42a_fld_obj obj;

	o42a_fld_link link;

	o42a_fld_var var;

	o42a_fld_scope scope;

	o42a_fld_dep dep;

};

typedef struct o42a_fld_ctr o42a_fld_ctr_t;

/**
 * Constructing field info.
 *
 * This structure is allocated on stack while the field is constructing.
 *
 * Multiple such structures for the same object form a linked list, which head
 * is stored in o42a_obj_data.fld_ctrs.
 *
 * The list of constructing fields is maintained by o42a_fld_start
 * and o42a_fld_finish functions.
 */
struct o42a_fld_ctr {

	O42A_HEADER

	/** Previous construction structure in the list or NULL if first. */
	o42a_fld_ctr_t *prev;

	/** Next construction structure in the list or NULL if last. */
	o42a_fld_ctr_t *next;

	/** A constructing field pointer. */
	void *fld;

	/** A thread constructing the field. */
	pthread_t thread;

	/**
	 * Field kind.
	 *
	 * One of the o42a_fld_kind values.
	 */
	uint16_t fld_kind;

};

o42a_bool_t o42a_fld_start(o42a_obj_data_t *, o42a_fld_ctr_t *);

o42a_bool_t o42a_fld_val_start(o42a_obj_data_t *, o42a_fld_ctr_t *);

void o42a_fld_finish(o42a_obj_data_t *, o42a_fld_ctr_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_FIELDS_H */
