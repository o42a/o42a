/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

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
#ifndef O42A_FIELDS_H
#define O42A_FIELDS_H

#include "o42a/fld/assigner.h"
#include "o42a/fld/dep.h"
#include "o42a/fld/getter.h"
#include "o42a/fld/link.h"
#include "o42a/fld/obj.h"
#include "o42a/fld/scope.h"
#include "o42a/fld/var.h"


/**
 * A union of all field kinds.
 */
typedef union o42a_fld {

	O42A_HEADER;

	o42a_fld_obj obj;

	o42a_fld_link link;

	o42a_fld_var var;

	o42a_fld_getter getter;

	o42a_fld_scope scope;

	o42a_fld_dep dep;

	o42a_fld_assigner assigner;

} o42a_fld;

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

	O42A_HEADER;

	/** Previous construction structure in the list or NULL if first. */
	o42a_fld_ctr_t *prev;

	/** Next construction structure in the list or NULL if last. */
	o42a_fld_ctr_t *next;

	/** A constructing field pointer. */
	o42a_fld *fld;

	/** A thread constructing the field. */
	pthread_t thread;

};


#ifdef __cplusplus
extern "C" {
#endif


o42a_bool_t o42a_fld_start(o42a_obj_data_t *, o42a_fld_ctr_t *);

void o42a_fld_finish(o42a_obj_data_t *, o42a_fld_ctr_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_FIELDS_H */
