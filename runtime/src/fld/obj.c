/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/error.h"


/**
 * Object field config.
 *
 * A pointer to this type is stored in VMT and is used for field object
 * construction.
 */
typedef struct o42a_fld_obj_conf {

	O42A_HEADER

	/**
	 * A pointer to value function, or NULL if the inherited one should be used.
	 */
	o42a_obj_value_ft *value_f;

	/**
	 * A pointer to VMT to use.
	 *
	 * This VMT will be prepended to VMT chain of the field object.
	 */
	const o42a_obj_vmt_t *vmt;

} o42a_fld_obj_conf_t;

#ifndef NDEBUG

const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_obj = {
	.type_code = 0x042a0200 | O42A_FLD_OBJ,
	.field_num = 1,
	.name = "o42a_fld_obj",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_obj, object),
			.name = "object",
		},
	},
};

const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_obj_conf = {
	.type_code = 0x042a0280 | O42A_FLD_OBJ,
	.field_num = 2,
	.name = "o42a_fld_obj_conf_t",
	.fields = {
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_obj_conf_t, value_f),
			.name = "value_f",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_obj_conf_t, vmt),
			.name = "vmt",
		},
	},
};

#endif /* NDEBUG */

static o42a_bool_t fld_obj_configure(
		const o42a_obj_vmtc_t *const vmtc,
		o42a_obj_t *const object,
		const o42a_fld_obj_conf_t *const conf,
		const o42a_rptr_t offset) {
	O42A_ENTER(return O42A_FALSE);

	o42a_debug_dump_mem("Field config: ", conf, 3);

	// Try to delegate to apply the previous VMT first.
	const o42a_obj_vmtc_t *const prev = vmtc->prev;

	if (prev) {
		// Previous VMT exists.
		const o42a_obj_vmt_t *const prev_vmt = prev->vmt;

		if (prev_vmt->size >= offset + sizeof(o42a_fld_obj_conf_t *)) {
			// Previous VMT is compatible.
			const o42a_fld_obj_conf_t *const prev_conf =
					*((o42a_fld_obj_conf_t **) ((char *) prev_vmt + offset));

			if (prev_conf) {
				// Previous field config exists.
				// Apply previous configuration.
				if (!O42A(fld_obj_configure(prev, object, prev_conf, offset))) {
					// Previous configuration failed.
					O42A_RETURN O42A_FALSE;
				}
			}
		}
	}

	// Update object's VMT chain with VMT from field config.
	o42a_obj_data_t *const data = &object->object_data;
	const o42a_obj_vmtc_t *const object_vmtc =
			O42A(o42a_obj_vmtc_alloc(conf->vmt, data->vmtc));

	if (!object_vmtc) {
		// VMT chain allocation failed.
		O42A_RETURN O42A_FALSE;
	}

	data->vmtc = object_vmtc;
	o42a_debug_dump_mem("Updated VMTC: ", object_vmtc, 3);

	// Override object value function if present in field config.
	o42a_obj_value_ft *const value_f = conf->value_f;

	if (value_f) {
		data->value_f = value_f;
		o42a_debug_func_name("Updated value function: ", value_f);
	}

	O42A_RETURN O42A_TRUE;
}

o42a_bool_t o42a_fld_obj_configure(
		const o42a_obj_vmtc_t *const vmtc,
		o42a_obj_t *const object,
		const o42a_rptr_t offset) {
	O42A_ENTER(return O42A_FALSE);

	const o42a_fld_obj_conf_t *const conf =
			*((o42a_fld_obj_conf_t **) ((char *) vmtc->vmt + offset));

	if (conf && O42A(fld_obj_configure(vmtc, object, conf, offset))) {
		// Field configuration exists and applied successfully.
		O42A_RETURN O42A_TRUE;
	}

	O42A_RETURN O42A_FALSE;
}

o42a_obj_t *o42a_obj_constructor_stub(
		const o42a_obj_vmtc_t *vmtc  __attribute__((unused)),
		o42a_obj_ctr_t *ctr  __attribute__((unused))) {
	O42A_ENTER(return NULL);
	o42a_error_print("Object constructor stub invoked");
	O42A_RETURN NULL;
}
