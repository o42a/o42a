/*
    Run-Time Library
    Copyright (C) 2010,2011 Ruslan Lopatin

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
#ifndef NDEBUG

#include "o42a/debug.h"

#include <signal.h>
#include <stdarg.h>
#include <string.h>
#include <wchar.h>


static volatile sig_atomic_t program_error_in_progress = 0;

static void program_error_signal(int sig) {
	if (program_error_in_progress) {
		raise(sig);
	}
	program_error_in_progress = 1;

	fwprintf(stderr, L"Program error (%i) occurred at:\n", sig);
	o42a_dbg_print_stack_trace(o42a_dbg_stack());

	signal(sig, SIG_DFL);
	raise(sig);
}

int32_t o42a_dbg_exec_main(
		int32_t (*main)(int32_t, char**),
		int32_t argc,
		char** argv) {
	O42A_ENTER;

	signal(SIGFPE, &program_error_signal);
	signal(SIGILL, &program_error_signal);
	signal(SIGSEGV, &program_error_signal);
	signal(SIGBUS, &program_error_signal);
	signal(SIGABRT, &program_error_signal);
	signal(SIGIOT, &program_error_signal);
	signal(SIGTRAP, &program_error_signal);
	signal(SIGSYS, &program_error_signal);

	O42A_DEBUG(L"Executing main\n");

	const int32_t result = main(argc, argv);

	if (sizeof (long) == 4) {
		O42A_DEBUG(L"Return code: %li\n", result);
	} else {
		O42A_DEBUG(L"Return code: %i\n", result);
	}

	O42A_RETURN result;
}

static inline void dbg_print_prefix() {
	fwprintf(stderr, L"[%s] ", o42a_dbg_stack()->name);
}

void o42a_debug(const wchar_t *const message) {
	dbg_print_prefix();
	fputws(message, stderr);
}

static const o42a_dbg_global_t *dbg_mem(
		const void*,
		o42a_dbg_field_t**,
		int);

void o42a_debug_mem_name(
		const wchar_t *const prefix,
		const void *const ptr) {
	o42a_debug(prefix);

	o42a_dbg_field_t *field = NULL;

	dbg_mem(ptr, &field, 1);
	fputwc(L'\n', stderr);
}

static void dbg_print_func_name(
		const void *const ptr,
		o42a_dbg_func_t *const func) {
	if (!ptr) {
		fputws(L"NULL", stderr);
		return;
	}
	if (!func) {
		fwprintf(stderr, L"<unknown>@%lx", (long) ptr);
		return;
	}
	fwprintf(stderr, L"%s", func->name);
}

void o42a_debug_func_name(
		const wchar_t *const prefix,
		const void *const ptr) {

	o42a_dbg_func_t *const func = o42a_dbg_func(ptr);

	o42a_debug(prefix);
	dbg_print_func_name(ptr, func);
	fputwc(L'\n', stderr);
}

static void dbg_print_indent(const size_t indent) {
	for (int i = indent - 1; i >= 0; --i) {
		fputwc(L' ', stderr);
		fputwc(L' ', stderr);
	}
}

static void dbg_print_field_value(
		const void *const data,
		o42a_dbg_field_t *field,
		const int depth,
		const size_t indent);

static void dbg_print_field_struct(
		const void *const data,
		o42a_dbg_struct_t *dbg_struct,
		const int depth,
		const size_t indent) {

	o42a_dbg_field_t *const fields = dbg_struct->fields;
	const size_t size = dbg_struct->size;

	for (size_t i = 0; i < size; ++i) {

		o42a_dbg_field_t *const field = fields + i;

		dbg_print_indent(indent);
		fwprintf(stderr, L"%s", field->name);
		dbg_print_field_value(data + field->offset, field, depth, indent);
		fputwc(L'\n', stderr);
	}
}

static void dbg_print_field_value(
		const void *const data,
		o42a_dbg_field_t *field,
		const int depth,
		const size_t indent) {

	o42a_dbg_struct_t *const dbg_struct = field->dbg_struct;

	switch (field->data_type) {
	case O42A_TYPE_INT32:
		if (sizeof (int) == 4) {

			const int val = *((int*) data);

			if (!val) {
				fputws(L": int32 = 0", stderr);
			} else {
				fwprintf(stderr, L": int32 = %i (%1$#lx)", val);
			}
		} else {

			const long val = *((long*) data);

			if (!val) {
				fputws(L": int32 = 0", stderr);
			} else {
				fwprintf(stderr, L": int32 = %li (%1$#x)", val);
			}
		}

		break;
	case O42A_TYPE_INT64:
		if (sizeof (long) == 8) {

			const long val = *((long*) data);

			if (!val) {
				fputws(L": int64 = 0", stderr);
			} else {
				fwprintf(stderr, L": int64 = %li (%1$#lx)", val);
			}
		} else {

			const long long val = *((long long*) data);

			if (!val) {
				fputws(L": int64 = 0", stderr);
			} else {
				fwprintf(stderr, L": int64 = %lli (%1$#llx)", val);
			}
		}

		break;
	case O42A_TYPE_FP64:
		fwprintf(stderr, L": fp64 = %Lf", *((double*) data));
		break;
	case O42A_TYPE_CODE_PTR: {

		void *func_ptr = *((void**) data);
		o42a_dbg_func_t *const func = o42a_dbg_func(func_ptr);

		fputws(L": function -> ", stderr);
		dbg_print_func_name(func_ptr, func);

		break;
	}
	case O42A_TYPE_REL_PTR: {

		const o42a_rptr_t val = *((o42a_rptr_t*) data);

		if (sizeof (int) == 4) {
			fwprintf(stderr, L": rptr = %+i", val);
		} else {
			fwprintf(stderr, L": rptr = %+li", val);
		}

		break;
	}
	case O42A_TYPE_DATA_PTR: {
		if (dbg_struct) {
			fwprintf(stderr, L": *%s -> ", dbg_struct->name);
		} else {
			fputws(L": * -> ", stderr);
		}

		void *data_ptr = *((void**) data);
		const o42a_dbg_field_t *data_field = NULL;

		dbg_mem(data_ptr, &data_field, 1);

		break;
	}
	case O42A_TYPE_STRUCT:
		if (!depth) {
			fwprintf(stderr, L": %s = ...\n", dbg_struct->name);
			break;
		}

		fwprintf(stderr, L": %s = {\n", dbg_struct->name);
		dbg_print_field_struct(data, field->dbg_struct, depth - 1, indent + 1);
		dbg_print_indent(indent);
		fputwc(L'}', stderr);

		break;
	default:
		fwprintf(stderr, L": <unknown:%x>", field->data_type);
	}
}

void o42a_dbg_dump_mem(const void *const ptr, const uint32_t depth) {

	o42a_dbg_field_t *field = NULL;

	dbg_mem(ptr, &field, 1);
	if (field) {
		dbg_print_field_value(ptr, field, depth, 0);
	}
	fputwc(L'\n', stderr);
}

void o42a_dbg_dump_field(
		const void *ptr,
		const o42a_dbg_field_t *const field,
		const uint32_t depth) {
	if (!ptr) {
		fputws(L": NULL\n", stderr);
	}
	if (!field) {
		fwprintf(stderr, L": <unknown@%lx>\n", (long) ptr);
	}
	dbg_print_field_value(ptr, field, depth, 0);
	fputwc(L'\n', stderr);
}

void o42a_dbg_dump_struct(
		const void *ptr,
		o42a_dbg_struct_t *const dbg_struct,
		const uint32_t depth) {
	if (!ptr) {
		fputws(L": NULL\n", stderr);
	}
	if (!dbg_struct) {
		fwprintf(stderr, L": <unknown@%lx>\n", (long) ptr);
	}
	fwprintf(stderr, L": %s = {\n", dbg_struct->name);
	dbg_print_field_struct(ptr, dbg_struct, depth, 1);
	fputws(L"}\n", stderr);
}

const o42a_dbg_func_t *o42a_dbg_func(const void* ptr) {
	if (!ptr) {
		return NULL;
	}

	o42a_dbg_func_t *const funcs = o42a_debug_info.functions;
	const size_t num_funcs = o42a_debug_info.num_functions;

	for (size_t i = 0; i < num_funcs; ++i) {

		o42a_dbg_func_t *const func = funcs + i;

		if (func->function == ptr) {
			return func;
		}
	}

	return NULL;
}

static const o42a_dbg_global_t *dbg_global(const void *ptr, const int print) {
	if (!ptr) {
		if (print) {
			fputws(L"NULL", stderr);
		}
		return NULL;
	}

	o42a_dbg_global_t *const globals = o42a_debug_info.globals;
	const size_t size = o42a_debug_info.num_globals;

	for (size_t i = 0; i < size; ++i) {

		const o42a_dbg_global_t *const global = globals + i;

		if (ptr < global->start) {
			continue;
		}
		if (ptr == global->start) {
			if (print) {
				fwprintf(stderr, L"%s", global->name);
			}
			return global;
		}

		if (global->content.data_type != O42A_TYPE_STRUCT) {
			continue;
		}

		o42a_dbg_struct_t *const dbg_struct = global->content.dbg_struct;
		void *end =
				global->start + o42a_layout_size(dbg_struct->layout);

		if (ptr >= end) {
			continue;
		}

		if (print) {
			if (print > 0) {
				fwprintf(stderr, L"%s :: ", global->name);
			} else {
				fwprintf(stderr, L"%s", global->name);
			}
		}

		return global;
	}

	if (print) {
		fwprintf(stderr, L"<unknown@%lx>", (long) ptr);
	}

	return NULL;
}

static const o42a_dbg_field_t* dbg_field(
		o42a_dbg_struct_t *const dbg_struct,
		const size_t offset,
		const int print) {

	o42a_dbg_field_t *const fields = dbg_struct->fields;
	const size_t num_fields = dbg_struct->size;

	for (size_t i = 0; i < num_fields; ++i) {

		o42a_dbg_field_t *const field = fields + i;
		const int diff = offset - field->offset;

		if (!diff) {
			if (print) {
				fwprintf(stderr, L"%s", field->name);
			}
			return field;
		}
		if (diff < 0) {
			continue;
		}
		if (field->data_type != O42A_TYPE_STRUCT) {
			continue;
		}

		o42a_dbg_struct_t *const field_struct = field->dbg_struct;
		const size_t new_offset = diff;

		if (new_offset >= o42a_layout_size(field_struct->layout)) {
			continue;
		}
		if (print) {
			fwprintf(stderr, L"%s:", field->name);
		}

		o42a_dbg_field_t *const sub_field =
				dbg_field(field_struct, new_offset, print);

		if (sub_field) {
			return sub_field;
		}

		break;
	}

	if (print) {
		fwprintf(stderr, L"<+%zu>", offset);
	}

	return NULL;
}

static const o42a_dbg_global_t *dbg_mem(
		const void *const ptr,
		o42a_dbg_field_t **const field,
		const int print) {

	o42a_dbg_global_t *global =
			dbg_global(ptr, print ? (field ? 1 : -1) : 0);

	if (!global) {
		return NULL;
	}
	if (!field) {
		return global;
	}

	const ptrdiff_t offset = ptr - global->start;

	if (!offset) {
		*field = &global->content;
		return global;
	}

	if (global->content.data_type != O42A_TYPE_STRUCT) {
		fwprintf(stderr, L"<%+ti>", offset);
		return global;
	}

	*field = dbg_field(global->content.dbg_struct, offset, print);

	return global;
}

inline const o42a_dbg_global_t *o42a_dbg_mem(
		const void *const ptr,
		o42a_dbg_field_t **const field) {
	return dbg_mem(ptr, field, 0);
}

inline const o42a_dbg_field_t *o42a_dbg_field(const void *const ptr) {

	o42a_dbg_field_t* field = NULL;

	dbg_mem(ptr, &field, 0);

	return field;
}

const o42a_dbg_field_t *o42a_dbg_subfield(const o42a_dbg_field_t *field, ...) {
	if (!field) {
		return field;
	}

	va_list va_names;

	va_start(va_names, field);

	iterate_names: for (;;) {
		if (field->data_type != O42A_TYPE_STRUCT) {
			break;
		}

		const char *name = va_arg(va_names, const char*);

		if (!name) {
			break;
		}

		o42a_dbg_struct_t *const dbg_struct = field->dbg_struct;
		o42a_dbg_field_t *const sub_fields = dbg_struct->fields;
		const size_t num_fields = dbg_struct->size;

		for (size_t i = 0; i < num_fields; ++i) {

			const o42a_dbg_field_t *const sub_field = sub_fields + i;

			if (!strcmp(sub_field->name, name)) {
				field = sub_field;
				goto iterate_names;
			}
		}

		break;
	}

	va_end(va_names);

	return field;
}


static o42a_dbg_stack_frame_t *stack_frame = NULL;

void o42a_dbg_enter(struct o42a_dbg_stack_frame *const frame) {
	frame->prev = stack_frame;
	stack_frame = frame;
	fwprintf(stderr, L"<< %s <<\n", frame->name);
}

void o42a_dbg_exit() {

	o42a_dbg_stack_frame_t *const prev = stack_frame->prev;

	if (!prev) {
		fwprintf(stderr, L">> %s >>\n", stack_frame->name);
	} else {
		fwprintf(stderr, L">> %s >> %s\n", stack_frame->name, prev->name);
	}
	assert(stack_frame || "Empty stack frame");
	stack_frame = prev;
}

o42a_dbg_stack_frame_t* o42a_dbg_stack() {
	return stack_frame;
}

inline void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t *const frame) {
	fwprintf(stderr, L"%s", frame->name);
}

void o42a_dbg_print_stack_trace(o42a_dbg_stack_frame_t *frame) {
	while (frame) {
		fputws(L"  ", stderr);
		o42a_dbg_print_stack_frame(frame);
		fputwc(L'\n', stderr);
		frame = frame->prev;
	}
}


#endif
