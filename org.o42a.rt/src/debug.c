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
#include "o42a/debug.h"

#include <assert.h>
#include <signal.h>
#include <string.h>

#include "unicode/ustdio.h"


static volatile sig_atomic_t program_error_in_progress = 0;

static void program_error_signal(int sig) {
	if (program_error_in_progress) {
		raise(sig);
	}
	program_error_in_progress = 1;

	fprintf(stderr, "Program error (%i) occurred at:\n", sig);
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

	O42A_DEBUG("Executing main\n");

	const int32_t result = main(argc, argv);

	O42A_DEBUG("Return code: %li\n", (long) result);

	O42A_RETURN result;
}

static inline void dbg_print_prefix() {
	fprintf(stderr, "[%s] ", o42a_dbg_stack()->name);
}

void o42a_dbg_print(const char *const message) {
	dbg_print_prefix();
	fputs(message, stderr);
}

static const o42a_dbg_global_t *dbg_mem(
		const void*,
		o42a_dbg_field_t**,
		int);

void o42a_dbg_mem_name(
		const char *const prefix,
		const void *const ptr) {
	o42a_debug(prefix);

	o42a_dbg_field_t *field = NULL;

	dbg_mem(ptr, &field, 1);
	fputc('\n', stderr);
}

static void dbg_print_func_name(
		const void *const ptr,
		o42a_dbg_func_t *const func) {
	if (!ptr) {
		fputs("NULL", stderr);
		return;
	}
	if (!func) {
		fprintf(stderr, "<unknown>@%lx", (long) ptr);
		return;
	}
	fprintf(stderr, "%s", func->name);
}

void o42a_dbg_func_name(
		const char *const prefix,
		const void *const ptr) {

	o42a_dbg_func_t *const func = o42a_dbg_func(ptr);

	o42a_debug(prefix);
	dbg_print_func_name(ptr, func);
	fputc('\n', stderr);
}

static void dbg_print_indent(const size_t indent) {
	for (int i = indent - 1; i >= 0; --i) {
		fputc(' ', stderr);
		fputc(' ', stderr);
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
		fputs(field->name, stderr);
		dbg_print_field_value(data + field->offset, field, depth, indent);
		fputc('\n', stderr);
	}
}

static void dbg_print_field_value(
		const void *const data,
		o42a_dbg_field_t *field,
		const int depth,
		const size_t indent) {

	o42a_dbg_struct_t *const dbg_struct = field->dbg_struct;

	switch (field->data_type) {
	case O42A_TYPE_INT32: {

		const long val =
				sizeof (int) == 4 ? *((int*) data) : *((long*) data);

		if (!val) {
			fputs(": int32 = 0", stderr);
		} else {
			fprintf(stderr, ": int32 = %1$li (%1$#lx)", val);
		}

		break;
	}
	case O42A_TYPE_INT64: {

		const long long val =
				sizeof (long) == 8 ? *((long*) data) : *((long long*) data);

		if (!val) {
			fputs(": int64 = 0", stderr);
		} else {
			fprintf(stderr, ": int64 = %1$lli (%1$#llx)", val);
		}

		break;
	}
	case O42A_TYPE_FP64:
		fprintf(stderr, ": fp64 = %f", *((double*) data));
		break;
	case O42A_TYPE_CODE_PTR: {

		void *func_ptr = *((void**) data);
		o42a_dbg_func_t *const func = o42a_dbg_func(func_ptr);

		fputs(": function -> ", stderr);
		dbg_print_func_name(func_ptr, func);

		break;
	}
	case O42A_TYPE_REL_PTR: {

		const o42a_rptr_t val = *((o42a_rptr_t*) data);

		fprintf(stderr, ": rptr = %+li", (long) val);

		break;
	}
	case O42A_TYPE_DATA_PTR: {
		if (dbg_struct) {
			fprintf(stderr, ": *%s -> ", dbg_struct->name);
		} else {
			fputs(": * -> ", stderr);
		}

		void *data_ptr = *((void**) data);
		const o42a_dbg_field_t *data_field = NULL;

		dbg_mem(data_ptr, &data_field, 1);

		break;
	}
	case O42A_TYPE_STRUCT:
		if (!depth) {
			fprintf(stderr, ": %s = ...\n", dbg_struct->name);
			break;
		}

		fprintf(stderr, ": %s = {\n", dbg_struct->name);
		dbg_print_field_struct(data, field->dbg_struct, depth - 1, indent + 1);
		dbg_print_indent(indent);
		fputc('}', stderr);

		break;
	default:
		fprintf(stderr, ": <unknown:%x>", field->data_type);
	}
}

void o42a_dbg_dump_mem(const void *const ptr, const uint32_t depth) {

	o42a_dbg_field_t *field = NULL;

	dbg_mem(ptr, &field, 1);
	if (field) {
		dbg_print_field_value(ptr, field, depth, 0);
	}
	fputc('\n', stderr);
}

void o42a_dbg_dump_field(
		const void *ptr,
		const o42a_dbg_field_t *const field,
		const uint32_t depth) {
	if (!ptr) {
		fputs(": NULL\n", stderr);
		return;
	}
	if (!field) {
		fprintf(stderr, ": <unknown@%lx>\n", (long) ptr);
		return;
	}
	dbg_print_field_value(ptr, field, depth, 0);
	fputc('\n', stderr);
}

void o42a_dbg_dump_struct(
		const void *ptr,
		o42a_dbg_struct_t *const dbg_struct,
		const uint32_t depth) {
	if (!ptr) {
		fputs(": NULL\n", stderr);
		return;
	}
	if (!dbg_struct) {
		fprintf(stderr, ": <unknown@%lx>\n", (long) ptr);
		return;
	}
	fprintf(stderr, ": %s = {\n", dbg_struct->name);
	dbg_print_field_struct(ptr, dbg_struct, depth, 1);
	fputs("}\n", stderr);
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
			fputs("NULL", stderr);
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
				fputs(global->name, stderr);
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
			fputs(global->name, stderr);
			if (print > 0) {
				fputs(" :: ", stderr);
			}
		}

		return global;
	}

	if (print) {
		fprintf(stderr, "<unknown@%lx>", (long) ptr);
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
				fprintf(stderr, "%s", field->name);
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
			fprintf(stderr, "%s:", field->name);
		}

		o42a_dbg_field_t *const sub_field =
				dbg_field(field_struct, new_offset, print);

		if (sub_field) {
			return sub_field;
		}

		break;
	}

	if (print) {
		fprintf(stderr, "<+%zu>", offset);
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
		fprintf(stderr, "<%+ti>", offset);
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
	fprintf(stderr, "<< %s <<\n", frame->name);
}

void o42a_dbg_exit() {

	o42a_dbg_stack_frame_t *const prev = stack_frame->prev;

	if (!prev) {
		fprintf(stderr, ">> %s >>\n", stack_frame->name);
	} else {
		fprintf(stderr, ">> %s >> %s\n", stack_frame->name, prev->name);
	}
	assert(stack_frame || "Empty stack frame");
	stack_frame = prev;
}

o42a_dbg_stack_frame_t* o42a_dbg_stack() {
	return stack_frame;
}

inline void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t *const frame) {
	fputs(frame->name, stderr);
}

void o42a_dbg_print_stack_trace(o42a_dbg_stack_frame_t *frame) {
	while (frame) {
		fputs("  ", stderr);
		o42a_dbg_print_stack_frame(frame);
		fputc('\n', stderr);
		frame = frame->prev;
	}
}


void o42a_dbg_fill_header(
		const o42a_dbg_type_info_t *const type_info,
		o42a_dbg_header_t *const header,
		const o42a_dbg_header_t *const enclosing) {
	header->flags = 0;
	header->type_code = type_info->type_code;
	header->name = type_info->name;
	header->type_info = type_info;
	if (enclosing) {
		header->enclosing = ((void*) enclosing) - ((void*) header);
	} else {
		header->enclosing = 0;
	}

	size_t field_num = type_info->field_num;

	if (!field_num) {
		return;
	}

	const o42a_dbg_field_info_t *field = type_info->fields;

	for (;;) {

		const o42a_dbg_type_info_t *const field_type_info =
				field->type_info;

		if (field_type_info) {

			o42a_dbg_header_t *const to =
					(o42a_dbg_header_t *) ((void*) header) + field->offset;

			o42a_dbg_fill_header(field_type_info, to, header);
		}

		if (!(--field_num)) {
			break;
		}
		++field;
	}
}

void o42a_dbg_copy_header(
		const o42a_dbg_header_t *const from,
		o42a_dbg_header_t *const to,
		const o42a_dbg_header_t *const enclosing) {
	to->flags = from->flags;
	to->type_code = from->type_code;
	to->name = from->name;
	to->type_info = from->type_info;
	if (enclosing) {
		to->enclosing = ((void*) enclosing) - ((void*) to);
	} else {
		to->enclosing = 0;
	}

	const o42a_dbg_type_info_t *const type_info = from->type_info;
	size_t field_num = type_info->field_num;

	if (!field_num) {
		return;
	}

	const o42a_dbg_field_info_t *field = type_info->fields;

	for (;;) {

		const o42a_dbg_type_info_t *const field_type_info =
				field->type_info;

		if (field_type_info) {

			const o42a_dbg_header_t *const f =
					(o42a_dbg_header_t *) ((void*) from) + field->offset;
			o42a_dbg_header_t *const t =
					(o42a_dbg_header_t *) ((void*) to) + field->offset;

			o42a_dbg_copy_header(f, t, to);
		}

		if (!(--field_num)) {
			break;
		}
		++field;
	}
}
