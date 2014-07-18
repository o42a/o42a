/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_OBJECT_H
#define O42A_OBJECT_H

#include <pthread.h>

#include "o42a/memory/gc.h"
#include "o42a/types.h"
#include "o42a/value.h"


#ifdef __cplusplus
extern "C" {
#endif

union o42a_fld;
struct o42a_fld_ctr;

typedef struct o42a_obj_desc o42a_obj_desc_t;

/** Object represented by it's body. */
typedef struct o42a_obj_body o42a_obj_t;

typedef struct o42a_obj_data o42a_obj_data_t;

typedef struct o42a_obj_vmt o42a_obj_vmt_t;

typedef struct o42a_obj_vmtc o42a_obj_vmtc_t;

/**
 * Object value definition function.
 *
 * \param result[out] object value to fill by function (stack-allocated).
 * \param object[in] object pointer.
 */
typedef void o42a_obj_def_ft(o42a_val_t *, o42a_obj_t *);

/**
 * Object value evaluation function.
 *
 * \param result[out] object value to fill (stack-allocated).
 * \param data[in] object data pointer.
 * \param object[in] object pointer.
 */
typedef void o42a_obj_value_ft(o42a_val_t *, o42a_obj_data_t *, o42a_obj_t *);

/**
 * Object condition evaluation function.
 *
 * \param object[in] object pointer.
 */
typedef o42a_bool_t o42a_obj_cond_ft(o42a_obj_t *);


/**
 * Object type flags.
 *
 * Used in o42a_obj_data.flags.
 */
enum o42a_obj_type_flags {

	/**
	 * Object is created at run-time.
	 *
	 * If this is set, than object type implementation is o42a_obj_rtype.
	 * Otherwise it's o42a_obj_stype.
	 */
	O42A_OBJ_RT = 0x1,

	/** Object is abstract. */
	O42A_OBJ_ABSTRACT = 0x2,

	/** Object is prototype. */
	O42A_OBJ_PROTOTYPE = 0x4,

	/** Object value definition is inherited from ancestor. */
	O42A_OBJ_ANCESTOR_DEF = 0x8,

	/** Object is a VOID special object. */
	O42A_OBJ_VOID = 0x8000,

	/**
	 * Object is a NONE special object.
	 *
	 * NONE is a placeholder for non-existing objects. E.g. when the link target
	 * can not be evaluated the NONE object is used instead.
	 *
	 * This object's value is always false of type VOID. It also can not be
	 * inherited and is not intended for other normal operations.
	 */
	O42A_OBJ_NONE = 0x4000,

	/** Type flags mask inherited when constructing new instance. */
	O42A_OBJ_INHERIT_MASK = 0xFF & ~O42A_OBJ_ANCESTOR_DEF,

};

/**
 * Object body flags.
 *
 * Used in o42a_obj_body.flags.
 */
enum o42a_obj_body_flags {

	/**
	 * The mask to apply to flags to gain a kind of body.
	 *
	 * \see o42a_obj_body_kind for possible values.
	 */
	O42A_OBJ_BODY_TYPE = 0x3,

};

/**
 * The kinds of object bodies.
 *
 * Apply O42A_OBJ_BODY_TYPE to o42a_obj_body.flags to gain one of these values.
 *
 * Body kind is exact only for static objects. When constructing object at run
 * time, it is only kept when O42A_OBJ_CTR_FIELD_PROPAGATION flags is set.
 * Otherwise the value is dropped to O42A_OBJ_BODY_INHERITED.
 *
 * This is used to update scope fields properly.
 */
enum o42a_obj_body_kind {

	/** The body is inherited from ancestor. */
	O42A_OBJ_BODY_INHERITED = 0,

	/** The body is from explicit sample. */
	O42A_OBJ_BODY_EXPLICIT = 1,

	/** The body is propagated from ascendant field. */
	O42A_OBJ_BODY_PROPAGATED = 2,

	/** The body is main one. */
	O42A_OBJ_BODY_MAIN = 3,

};

/**
 * Object body.
 *
 * This structure is only a header common to every object body. The fields
 * are allocated after this header at proper alignments.
 *
 * Each object contains one body per each ascendant, except void. One of the
 * bodies is called main and corresponds to an object type.
 */
typedef struct o42a_obj_body {

	O42A_HEADER

	/**
	 * Pointer to object type descriptor, where corresponding body were first
	 * declared in.
	 */
	const o42a_obj_desc_t *declared_in;

	/**
	 * Pointer to virtual method tables chain.
	 */
	const o42a_obj_vmtc_t *vmtc;

	/*
	 * Relative pointer to object data.
	 *
	 * Each object body of the same object refers to the same data instance
	 * of that object.
	 */
	o42a_rptr_t object_data;

	/**
	 * Object body flags.
	 *
	 * \see o42a_obj_body_flags for possible values.
	 */
	uint32_t flags;

} o42a_obj_body_t;

/**
 * Object data.
 *
 * Contains object value and combines other data necessary to maintain object
 * and it's inheritance.
 *
 * There is exactly one data section per object.
 *
 * Object type always starts with data field, so it is safe to cast type pointer
 * to data pointer.
 */
struct o42a_obj_data {

	O42A_HEADER

	/** Relative pointer to main object body. */
	o42a_rptr_t object;

	/**
	 * Relative pointer to memory block containing this object.
	 *
	 * This may be a virtual block start when ancestor body is not physically
	 * present.
	 */
	o42a_rptr_t start;

	/**
	 * Type flags.
	 *
	 * This can be used to distinguish object type implementation and contains
	 * other information. See o42a_obj_type_flags enum.
	 */
	uint16_t flags;

	/**
	 * Object mutex initialization flag.
	 *
	 * This is set to by atomic operations and should not be accessed directly.
	 */
	int8_t mutex_init;

	/**
	 * Object mutex.
	 *
	 * The mutex is valid only when mutex_init flags set. The o42a_obj_lock
	 * function takes care of proper mutex initialization.
	 *
	 * Use o42a_obj_lock and o42a_obj_unlock respectively to lock or unlock this
	 * mutex.
	 */
	pthread_mutex_t mutex;

	/**
	 * Threading condition of object.
	 *
	 * Waiting on this condition is always happens when the thread owns
	 * an object mutex.
	 *
	 * The o42a_obj_lock function takes care of proper thread_cond
	 * initialization.
	 *
	 * Use o42a_obj_wait to start waiting on this condition, o42a_obj_signal
	 * to unblock at least one waiting thread, or o42a_obj_broadcast to unblock
	 * all waiting thread.
	 */
	pthread_cond_t thread_cond;

	/**
	 * Object value calculator function.
	 */
	o42a_obj_value_ft *value_f;

	/**
	 * Object condition calculator function.
	 */
	o42a_obj_cond_ft *cond_f;

	/**
	 * Object value definition function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_obj_def_ft *def_f;

	/**
	 * Object value.
	 *
	 * This is only used by stateful objects. Stateless ones have a
	 * O42A_VAL_STATELESS flag set.
	 */
	o42a_val_t value;

	/**
	 * Value evaluation resume pointer.
	 *
	 * It is a position inside the object's value definition function (defs_f).

	 * This value is updated by the yield statement. The next time the value is
	 * requested, the evaluation starts from this position.
	 *
	 * Always NULL for newly constructed objects. In this case the value
	 * evaluation starts from the beginning.
	 */
	void *resume_from;

	/**
	 * Pointer to object type descriptor.
	 */
	const o42a_obj_desc_t *desc;

	/**
	 * Pointer to object value type descriptor.
	 */
	const o42a_val_type_t *value_type;

	/**
	 * Pointer to the head of the constructing fields list.
	 *
	 * This is maintained with o42a_fld_start and o42a_fld_finish functions.
	 */
	struct o42a_fld_ctr *fld_ctrs;

	/** Relative pointer to the list of ascendant descriptors. */
	o42a_rlist_t ascendants;

};

/**
 * Object type descriptor.
 */
struct o42a_obj_desc {

	O42A_HEADER

	/**
	 * Pointer to the descriptor this declaration copies the structure of.
	 *
	 * This can be either a pointer to this descriptor, or to the descriptor
	 * of the object this one is propagated from.
	 */
	o42a_obj_desc_t *declaration;

	/**
	 * Pointer to the data of the object this descriptor is created for.
	 */
	o42a_obj_data_t *data;

	/**
	 * Relative pointer to the list of field descriptors.
	 *
	 * Note that this list is empty for propagated objects.
	 * Always use `declaration.fields` to get a real list.
	 */
	o42a_rlist_t fields;

	/**
	 * Relative pointer to the list of field overrider descriptors.
	 *
	 * The list is ordered by field descriptor pointers.
	 */
	o42a_rlist_t overriders;

	/** Main body layout. */
	o42a_layout_t main_body_layout;

};


/**
 * Object ascendant descriptor.
 *
 * Describes object body corresponding to ascending type.
 */
typedef struct o42a_obj_ascendant {

	O42A_HEADER

	/** Pointer to the ascending object's type descriptor. */
	const o42a_obj_desc_t *desc;

	/** Relative pointer to ascendant body. */
	o42a_rptr_t body;

} o42a_obj_ascendant_t;

/**
 * Field descriptor.
 *
 * This is used to describe the new fields only. Fields generated due to
 * override described with o42a_override.
 */
typedef const struct o42a_obj_field {

	O42A_HEADER

	/** Pointer to object type the field first declared in. */
	o42a_obj_desc_t *declared_in;

	/**
	 * Field kind.
	 *
	 * One of the o42a_obj_field_kind enum values.
	 */
	uint32_t kind;

	/** Pointer to the field content within main body. */
	o42a_rptr_t fld;

} o42a_obj_field_t;

/**
 * Field overrider descriptor.
 */
typedef const struct o42a_obj_overrider {

	O42A_HEADER

	/** Pointer to descriptor of the overridden field. */
	o42a_obj_field_t *field;

	/**
	 * Pointer to the type descriptor of the object the overrider field were
	 * defined in.
	 */
	o42a_obj_desc_t *defined_in;

	/** Relative pointer to the body containing overriding field. */
	o42a_rptr_t body;

} o42a_obj_overrider_t;

/**
 * A chain of VMTs.
 *
 * The chain is represented as a linked list of VMTs. Each VMT in such list has
 * the same structure.
 */
struct o42a_obj_vmtc {

	O42A_HEADER

	/** A pointer to VMT. */
	const o42a_obj_vmt_t *vmt;

	/**
	 * A pointer to previous link in VMT chain, or NULL.
	 *
	 * Only VMT terminators have NULL as this pointer.
	 */
	const o42a_obj_vmtc_t *prev;

};

#ifndef NDEBUG
extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_vmtc;
#endif /* NDEBUG */

/**
 * Virtual methods table.
 *
 * This structure is only a header common to every VMT. An actual VMT structure
 * is specific to particular object body structure. Such object body contains
 * a pointer to the chain of compatible VMTs.
 *
 * All VMTs are statically allocated and may not be altered at run time.
 */
struct o42a_obj_vmt {

	O42A_HEADER

	/**
	 * VMT chain terminator.
	 *
	 * This chain link always refers to owning VMT, and doesn't have a previous
	 * chain link (terminator.prev == NULL).
	 *
	 * Every VMT chain should terminate with one of terminators.
	 */
	o42a_obj_vmtc_t terminator;

};

/**
 * Object construction data.
 */
typedef struct o42a_obj_ctr {

	O42A_HEADER

	/**
	 * Pointer to enclosing object's data.
	 *
	 * May be NULL when constructing object is local or exactly known.
	 */
	o42a_obj_data_t *owner_data;

	/**
	 * Ancestor data.
	 */
	o42a_obj_data_t *ancestor_data;

	/**
	 * Sample object type descriptor.
	 */
	o42a_obj_desc_t *desc;

	/**
	 * Eagerly evaluated object value.
	 *
	 * An O42A_VAL_INDEFINITE bit should be set if the value is not eagerly
	 * evaluated.
	 */
	o42a_val_t value;

} o42a_obj_ctr_t;

typedef struct o42a_obj_ctable {

	O42A_HEADER

	/**
	 * Pointer to the data of this object's owner (i.e. enclosing object).
	 *
	 * May be NULL when constructing object is local or exactly known.
	 */
	const o42a_obj_data_t *owner_data;

	const o42a_obj_data_t *const ancestor_data;

	const o42a_obj_desc_t *const sample_desc;

	o42a_obj_data_t *const object_data;

	const o42a_obj_desc_t *body_desc;

	o42a_obj_field_t *field;

	struct o42a_obj_cside {

		O42A_HEADER

		o42a_obj_body_t *body;

		union o42a_fld *fld;

	} from;

	struct o42a_obj_cside to;

} o42a_obj_ctable_t;


#ifndef NDEBUG

extern const struct _O42A_DEBUG_TYPE_o42a_obj_data {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[15];
} _O42A_DEBUG_TYPE_o42a_obj_data;

extern const o42a_dbg_type_info5f_t _O42A_DEBUG_TYPE_o42a_obj_desc;

extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_ascendant;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_field;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_overrider;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_ctr;

extern const struct _O42A_DEBUG_TYPE_o42a_obj_ctable {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[8];
} _O42A_DEBUG_TYPE_o42a_obj_ctable;

#endif /* NDEBUG */


/** Garbage-collected object descriptor. */
extern const o42a_gc_desc_t o42a_obj_gc_desc;

/**
 * Retrieves object data from it's body.
 *
 * \param body[in] object body pointer.
 *
 * \return object type pointer.
 */
inline o42a_obj_data_t *o42a_obj_data(const o42a_obj_body_t *const body) {
	return (o42a_obj_data_t *) (((char *) body) + body->object_data);
}

/**
 * Retrieves object from it's data.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to object's main body.
 */
inline o42a_obj_t *o42a_obj_by_data(const o42a_obj_data_t *const data) {
	return (o42a_obj_t *) (((char *) data) + data->object);
}

/**
 * Retrieves object ascendant's descriptors.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to the first element of ascendant descriptors array.
 */
inline o42a_obj_ascendant_t *o42a_obj_ascendants(
		const o42a_obj_data_t *const data) {

	const o42a_rlist_t *const list = &data->ascendants;

	return (o42a_obj_ascendant_t *) (((char *) list) + list->list);
}

/**
 * Retrieves field descriptors.
 *
 * \param desc[in] type descriptor pointer.
 *
 * \return pointer to the first element of the field descriptors array.
 */
inline o42a_obj_field_t *o42a_obj_fields(const o42a_obj_desc_t *const desc) {

	const o42a_rlist_t *const list = &desc->declaration->fields;

	return (o42a_obj_field_t *) (((char *) list) + list->list);
}

/**
 * Retrieves field override descriptors.
 *
 * \param desc[in] type descriptor pointer.
 *
 * \return pointer to the first element of the field override descriptors array.
 */
inline o42a_obj_overrider_t *o42a_obj_overriders(
		const o42a_obj_desc_t *const desc) {

	const o42a_rlist_t *const list = &desc->overriders;

	return (o42a_obj_overrider_t *) (((char *) list) + list->list);
}

/**
 * Retrieves object body corresponding to the given ascendant.
 *
 * \param ascendant[in] pointer to ascendant descriptor.
 *
 * \return body pointer.
 */
inline o42a_obj_body_t *o42a_obj_ascendant_body(
		const o42a_obj_ascendant_t *const ascendant) {
	return (o42a_obj_body_t *) (((char *) ascendant) + ascendant->body);
}

/**
 * Retrieved the given field overrider.
 *
 * \param sample_desc[in] type descriptor of the sample to search the field in.
 * \param dield[in] target field descriptor.
 *
 * \return field overrider, or NULL if not found.
 */
o42a_obj_overrider_t *o42a_obj_field_overrider(
		const o42a_obj_desc_t *,
		const o42a_obj_field_t *);

/**
 * Searches for ascendant descriptor of the given type.
 *
 * \param data object data to search sample descriptor in.
 * \param desc the descriptor of the type to search for.
 *
 * \return ascendant descriptor or NULL if not found.
 */
const o42a_obj_ascendant_t *o42a_obj_ascendant_of_type(
		const o42a_obj_data_t *,
		const o42a_obj_desc_t *);

/**
 * Searches for the object's body of the given type.
 *
 * \param object[in] object to cast.
 * \param desc[in] the descriptor of the type to cast to.
 *
 * \return object body pointer corresponding to the given type,
 * or NULL if the object is not derived from it.
 */
o42a_obj_body_t *o42a_obj_cast(o42a_obj_t *, const o42a_obj_desc_t *);

/**
 * Instantiates a new object.
 *
 * \param ctr[in] filled-in construction data.
 *
 * \return pointer to object's body of the sample type.
 */
o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *);


/**
 * False value definition.
 */
void o42a_obj_def_false(o42a_val_t *, o42a_obj_t *);

/**
 * False object value.
 */
void o42a_obj_value_false(o42a_val_t *, o42a_obj_data_t *, o42a_obj_t *);

/**
 * False object condition.
 */
o42a_bool_t o42a_obj_cond_false(o42a_obj_t *);


/**
 * Void value definition.
 */
void o42a_obj_def_void(o42a_val_t *, o42a_obj_t *);

/**
 * Void object value.
 */
void o42a_obj_value_void(o42a_val_t *, o42a_obj_data_t *, o42a_obj_t *);

/**
 * True object condition.
 */
o42a_bool_t o42a_obj_cond_true(o42a_obj_t *);


/**
 * Unknown value definition.
 *
 * This function does not modify the value. So, after the function call,
 * the value remain indefinite, as it was before.
 */
void o42a_obj_def_unknown(o42a_val_t *, o42a_obj_t *);

/**
 * Object value definition stub.
 */
void o42a_obj_def_stub(o42a_val_t *, o42a_obj_t *);

/**
 * Object value evaluation stub.
 */
void o42a_obj_value_stub(o42a_val_t *, o42a_obj_data_t *, o42a_obj_t *);

/**
 * Object condition evaluation stub.
 */
o42a_bool_t o42a_obj_cond_stub(o42a_obj_t *);


/**
 * Locks an object mutex, initializing it if necessary.
 *
 * Use o42a_obj_unlock to unlock the mutex. An object mutex is recursive, which
 * means it can be locked multiple times by the same thread and remains locked
 * until unlocked the same number of times.
 */
void o42a_obj_lock(o42a_obj_data_t *);

/**
 * Unlocks an object mutex previously locked with o42a_obj_lock by the same
 * thread.
 */
void o42a_obj_unlock(o42a_obj_data_t *);


/**
 * Waits for an object condition.
 */
void o42a_obj_wait(o42a_obj_data_t *);

/**
 * Unblocks at least one thread waiting on an object condition.
 */
void o42a_obj_signal(o42a_obj_data_t *);

/**
 * Unblocks all threads waiting on an object condition.
 */
void o42a_obj_broadcast(o42a_obj_data_t *);

/**
 * An object use by current thread.
 *
 * Invoke o42a_obj_start_use to declare the object is used and o42a_obj_end_use
 * when it is no longer needed.
 */
typedef struct o42a_obj_use {

	O42A_HEADER

	/**
	 * Used object data pointer or NULL if no object use declared.
	 *
	 * This should be initialized to NULL initially.
	 */
	o42a_obj_data_t *data;

} o42a_obj_use_t;


#ifndef NDEBUG
extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_obj_use;
#endif /* NDEBUG */

/**
 * Declares the object is used.
 *
 * This function invokes o42a_gc_use for object data block.
 */
void o42a_obj_use(o42a_obj_data_t *);

/**
 * Declares the indirectly pointed object is used by current thread.
 *
 * Performs the same as o42a_obj_use with respect to GC and mutability
 * by invoking o42a_gc_use_mutable method.
 */
o42a_obj_t *o42a_obj_use_mutable(o42a_obj_t **);

/**
 * Declares the static object is used.
 *
 * This function submits the static object to GC.
 *
 * The static objects does not require to be released, so no "unuse" function
 * exists.
 *
 * This should only be called for static objects.
 */
void o42a_obj_use_static(o42a_obj_data_t *);

/**
 * Declares the object is used.
 *
 * This function informs GC the object can not be disposed.
 *
 * Invoke o42a_obj_end_use to release object.
 *
 * This can be invoked for static object too. Then o42a_obj_end_use won't do
 * anything.
 *
 * It is an error to call this function multiple times with the same use
 * structure.
 */
void o42a_obj_start_use(o42a_obj_use_t *, o42a_obj_data_t *);

/**
 * Releases the object previously used by o42a_obj_start_use.
 *
 * This function informs the GC the object is no longer needed an thus can be
 * processed. The o42a_gc_signal function should be called to initiate the GC
 * processing.
 *
 * This function can be invoked even if o42a_obj_start_use was never called for
 * the given use. In this case the function does nothing.
 */
void o42a_obj_end_use(o42a_obj_use_t *);

/**
 * Declares the object stored in value is used.
 *
 * This function calls o42a_gc_use if the value condition is true and it
 * contains a non-NULL object pointer.
 */
void o42a_obj_start_val_use(o42a_val_t *);

/**
 * Releases the object previously used by o42a_obj_start_val_use.
 *
 * This function calls o42a_gc_unuse if the value condition is true and it
 * contains a non-NULL object pointer.
 */
void o42a_obj_end_val_use(o42a_val_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_OBJECT_H */
