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

typedef struct o42a_obj o42a_obj_t;

typedef struct o42a_obj_data o42a_obj_data_t;

typedef struct o42a_obj_vmt o42a_obj_vmt_t;

typedef struct o42a_obj_vmtc o42a_obj_vmtc_t;

/**
 * Object value function.
 *
 * \param result[out] object value to fill (stack-allocated).
 * \param object[in] object pointer.
 */
typedef void o42a_obj_value_ft(o42a_val_t *, o42a_obj_t *);


/**
 * Object lock.
 */
typedef struct o42a_obj_lock {

	O42A_HEADER

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
	 * Pointer to the head of the constructing fields list.
	 *
	 * This is maintained with o42a_fld_start and o42a_fld_finish functions.
	 */
	struct o42a_fld_ctr *fld_ctrs;

} o42a_obj_lock_t;

/**
 * Object data.
 *
 * Contains object value and combines other data necessary to maintain object
 * and it's inheritance.
 *
 * There is exactly one data section per object.
 */
struct o42a_obj_data {

	O42A_HEADER

	/**
	 * Object value calculator function.
	 */
	o42a_obj_value_ft *value_f;

	/**
	 * Object value.
	 *
	 * This is only used by stateful objects. Stateless ones have a
	 * O42A_VAL_STATELESS flag set.
	 */
	o42a_val_t value;

	/**
	 * Pointer to virtual method tables chain.
	 */
	const o42a_obj_vmtc_t *vmtc;

	/**
	 * Pointer to object type descriptor.
	 */
	const o42a_obj_desc_t *desc;

};

/**
 * Object type descriptor.
 */
struct o42a_obj_desc {

	O42A_HEADER

	/**
	 * Pointer to object value type descriptor.
	 */
	const o42a_val_type_t *value_type;

#ifndef NDEBUG

	/**
	 * Pointer to run-time debug type info.
	 */
	const o42a_dbg_type_info_t *type_info;

#endif /* NDEBUG */

	/**
	 * Relative pointer to the list of field descriptors.
	 */
	o42a_rlist_t fields;

	/**
	 * Relative pointer to the list of ascendant descriptors.
	 */
	o42a_rlist_t ascendants;

	/**
	 * Object size in bytes.
	 */
	uint32_t object_size;

};


/**
 * Object structure.

 * This structure is only a header common to every object. The fields are
 * allocated after this header at proper alignments.
 */
struct o42a_obj {

	O42A_HEADER

	/**
	 * Object data.
	 */
	o42a_obj_data_t object_data;

	/**
	 * Object fields.
	 *
	 * The number and types of these fields are specific to object type.
	 */
	char fields[];

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
 * is specific to particular object type. Such object data contains
 * a pointer to the chain of compatible VMTs.
 *
 * All VMTs are statically allocated and may not be altered at run time.
 */
struct o42a_obj_vmt {

	O42A_HEADER

	/**
	 * The size of this VMT.
	 */
	uint32_t size;

	/**
	 * VMT chain terminator.
	 *
	 * This chain link always refers to owning VMT, and doesn't have a previous
	 * chain link (terminator.prev == NULL).
	 *
	 * Every VMT chain should terminate with one of terminators.
	 */
	o42a_obj_vmtc_t terminator;

	/**
	 * Object methods.
	 *
	 * The number and types of these methods are specific to the object type.
	 */
	char methods[];

};

/**
 * Object construction data.
 */
typedef struct o42a_obj_ctr {

	O42A_HEADER

	/**
	 * Pointer to object to construct.
	 *
	 * This should be an object allocated with o42a_obj_alloc function.
	 */
	o42a_obj_t *object;

	/**
	 * Pointer to enclosing object.
	 *
	 * May be NULL when constructing object is exactly known.
	 */
	o42a_obj_t *owner;

	/**
	 * Pointer to ancestor object.
	 *
	 * Can be NULL if ancestor does not contain any inheritable fields,
	 * i.e. owner references or run time dependencies.
	 */
	const o42a_obj_t *ancestor;

} o42a_obj_ctr_t;


typedef struct o42a_obj_ctable {

	O42A_HEADER

	/**
	 * Pointer to the object's owner (i.e. enclosing object).
	 *
	 * May be NULL when constructing object is local or exactly known.
	 */
	o42a_obj_t *owner;

	const o42a_obj_desc_t *const ancestor_desc;

	const o42a_obj_t *from;

	o42a_obj_t *const to;

	const o42a_obj_desc_t *body_desc;

	o42a_obj_field_t *field;

	union o42a_fld *from_fld;

	union o42a_fld *to_fld;

} o42a_obj_ctable_t;


#ifndef NDEBUG

extern const o42a_dbg_type_info4f_t _O42A_DEBUG_TYPE_o42a_obj_data;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_lock;

extern const o42a_dbg_type_info5f_t _O42A_DEBUG_TYPE_o42a_obj_desc;

extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_obj_ascendant;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_field;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_ctr;

extern const struct _O42A_DEBUG_TYPE_o42a_obj_ctable {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[8];
} _O42A_DEBUG_TYPE_o42a_obj_ctable;

#endif /* NDEBUG */

/** Void object type descriptor. */
extern const o42a_obj_desc_t o42a_obj_void_desc;

/** False object type descriptor. */
extern const struct o42a_obj_false_desc {

	o42a_obj_desc_t desc;

	o42a_obj_ascendant_t ascendants[1];

} o42a_obj_false_desc;

/** None object type descriptor. */
extern const o42a_obj_desc_t o42a_obj_none_desc;

/** Garbage-collected object descriptor. */
extern const o42a_gc_desc_t o42a_obj_gc_desc;

/**
 * Retrieves object ascendant's descriptors.
 *
 * \param desc[in] object type descriptor pointer.
 *
 * \return pointer to the first element of ascendant descriptors array.
 */
inline const o42a_obj_ascendant_t *o42a_obj_ascendants(
		const o42a_obj_desc_t *const desc) {

	const o42a_rlist_t *const list = &desc->ascendants;

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

	const o42a_rlist_t *const list = &desc->fields;

	return (o42a_obj_field_t *) (((char *) list) + list->list);
}

/**
 * Allocates a new VMT chain.
 *
 * If the VMT of the previous link is the same as provided one, then just
 * returns it.
 *
 * If the VMT of the previous link is empty (e.g. VMT of void type),
 * then returns a terminator of the given VMT.
 *
 * The chain link instances are reference-counted. This function sets the
 * reference count of newly allocated link to zero and increases the reference
 * count of previous link in the chain by one, unless it is a terminator link.
 *
 * The allocated link chain should be either passed to object constructor
 * function, or freed by o42a_obj_vmtc_free function.
 *
 * If allocation fails, then frees the previous link with o42a_obj_vmtc_free
 * function.
 *
 * \param vmt VMT of the new chain link.
 * \param prev previous link in VMT chain.
 *
 * \return a pointer to new VMT chain, or NULL if allocation failed.
 */
const o42a_obj_vmtc_t *o42a_obj_vmtc_alloc(
		const o42a_obj_vmt_t *,
		const o42a_obj_vmtc_t *);

/**
 * Frees the VMT chain.
 *
 * If VMT chain link reference count is not zero, or the chain link is
 * terminator, then does nothing.
 *
 * If the previous link is not a terminator, then decreases its reference
 * count and frees the memory allocated for it if this count drops to zero.
 *
 * \param vmtc VMT chain to free.
 */
void o42a_obj_vmtc_free(const o42a_obj_vmtc_t *);

/**
 * Allocates new object.
 *
 * \param desc[in] object descriptor pointer.
 *
 * \return allocated object, or NULL is allocation failed.
 */
o42a_obj_t *o42a_obj_alloc(const o42a_obj_desc_t *);

/**
 * Initializes newly allocated object.
 *
 * This method is called instead of o42a_obj_alloc if memory for object
 * is allocated by other means, e.g. on stack.
 *
 * \param object pointer to memory allocated for object.
 * \param desc[in] object descriptor pointer.
 */
void o42a_obj_init(o42a_obj_t *, const o42a_obj_desc_t *);

/**
 * Initializes object lock.
 *
 * \param lock object lock to initialize.
 */
void o42a_obj_lock_init(o42a_obj_lock_t *);

/**
 * Constructs previously allocated object.
 *
 * Disposes object construction if construction failed.
 *
 * The object VMT chain pointer should be filled. If it points to VMT chain
 * terminator, then VMT chain will be constructed from the given terminator
 * and ancestor VMT chain. Otherwise, or if ancestor not specified, this VMT
 * chain will be used.
 *
 * This should be either VMT chain from existing object, or the one
 * allocated with o42a_obj_vmtc_alloc function.
 *
 * The reference count of VMT chain link will be increased upon successful
 * object construction (unless it is a terminator). Otherwise the
 * VMT chain will be freed with o42a_obj_vmtc_free function.
 *
 * \param ctr[in] filled-in object construction data.
 *
 * \return pointer to new object or NULL if construction failed.
 */
o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *);

/**
 * Disposes object construction.
 *
 * This method frees memory allocated by o42a_obj_alloc and frees the VMT chain.
 *
 * Call this method instead of o42a_obj_new if object construction failed.
 *
 * \param ctr[in] filled-in object construction data.
 */
void o42a_obj_dispose(const o42a_obj_ctr_t *);

/**
 * Object condition evaluation function.
 *
 * \param object target object.
 *
 * \return O42A_TRUE if object value is successfully evaluated,
 * or O42A_FALSE otherwise.
 */
o42a_bool_t o42a_obj_cond(o42a_obj_t *);

/**
 * Returns eagerly evaluated object value.
 */
void o42a_obj_value_eager(o42a_val_t *, o42a_obj_t *);

/**
 * False object value.
 */
void o42a_obj_value_false(o42a_val_t *, o42a_obj_t *);

/**
 * Void object value.
 */
void o42a_obj_value_void(o42a_val_t *, o42a_obj_t *);

/**
 * Unknown object value.
 *
 * This function does not modify the value. So, after the function call,
 * the value remain indefinite, as it was before.
 */
void o42a_obj_value_unknown(o42a_val_t *, o42a_obj_t *);

/**
 * Object value evaluation stub.
 */
void o42a_obj_value_stub(o42a_val_t *, o42a_obj_t *);

/**
 * Initializes static object if not initialized yet.
 *
 * Does nothing if the given object is not static, or initialized already.
 *
 * \param object an object to initialize.
 * \param lock object lock, or NULL if object has no lock.
 */
void o42a_obj_static(o42a_obj_t *, o42a_obj_lock_t *);

/**
 * Locks an object mutex, initializing it if necessary.
 *
 * Use o42a_obj_unlock to unlock the mutex. An object mutex is recursive, which
 * means it can be locked multiple times by the same thread and remains locked
 * until unlocked the same number of times.
 */
void o42a_obj_lock(o42a_obj_lock_t *);

/**
 * Unlocks an object mutex previously locked with o42a_obj_lock by the same
 * thread.
 */
void o42a_obj_unlock(o42a_obj_lock_t *);


/**
 * Waits for an object condition.
 */
void o42a_obj_wait(o42a_obj_lock_t *);

/**
 * Unblocks at least one thread waiting on an object condition.
 */
void o42a_obj_signal(o42a_obj_lock_t *);

/**
 * Unblocks all threads waiting on an object condition.
 */
void o42a_obj_broadcast(o42a_obj_lock_t *);

/**
 * An object use by current thread.
 *
 * Invoke o42a_obj_start_use to declare the object is used and o42a_obj_end_use
 * when it is no longer needed.
 */
typedef struct o42a_obj_use {

	O42A_HEADER

	/**
	 * Used object pointer or NULL if no object use declared.
	 *
	 * This should be initialized to NULL initially.
	 */
	o42a_obj_t *object;

} o42a_obj_use_t;


#ifndef NDEBUG
extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_obj_use;
#endif /* NDEBUG */

/**
 * Declares the object is used.
 *
 * This function invokes o42a_gc_use for object data block.
 */
void o42a_obj_use(o42a_obj_t *);

/**
 * Declares the indirectly pointed object is used by current thread.
 *
 * Performs the same as o42a_obj_use with respect to GC and mutability
 * by invoking o42a_gc_use_mutable method.
 */
o42a_obj_t *o42a_obj_use_mutable(o42a_obj_t **);

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
void o42a_obj_start_use(o42a_obj_use_t *, o42a_obj_t *);

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
void o42a_obj_start_val_use(const o42a_val_t *);

/**
 * Releases the object previously used by o42a_obj_start_val_use.
 *
 * This function calls o42a_gc_unuse if the value condition is true and it
 * contains a non-NULL object pointer.
 */
void o42a_obj_end_val_use(const o42a_val_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_OBJECT_H */
