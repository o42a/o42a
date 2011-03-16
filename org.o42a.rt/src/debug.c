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
#include <stdlib.h>
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


inline const o42a_dbg_header_t *o42a_dbg_header(const void *const ptr) {

	const o42a_dbg_header_t *const header = (o42a_dbg_header_t*) ptr;
	const o42a_dbg_type_info_t *const type_info = header->type_info;

	if (header->type_code != type_info->type_code) {
		fprintf(stderr, "Wrong debug header at <0x%lx>\n", (long) header);
		exit(EXIT_FAILURE);
	}

	return header;
}


static inline void dbg_print_prefix() {
	fprintf(stderr, "[%s] ", o42a_dbg_stack()->name);
}

void o42a_dbg_print(const char *const message) {
	dbg_print_prefix();
	fputs(message, stderr);
}

static inline void dbg_mem_name(const o42a_dbg_header_t *const header) {

	const o42a_rptr_t enclosing = header->enclosing;

	if (enclosing) {
		dbg_mem_name(o42a_dbg_header((void*) header + enclosing));
		fputc(':', stderr);
	}

	fputs(header->name, stderr);
}

void o42a_dbg_mem_name(
		const char *const prefix,
		const void *const ptr) {
	o42a_debug(prefix);
	if (!ptr) {
		fputs("NULL\n", stderr);
		return;
	}

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_mem_name(header);
	fprintf(stderr, " <0x%lx>: %s\n", (long) ptr, header->type_info->name);
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

static void dbg_indent(const size_t indent) {
	for (int i = indent - 1; i >= 0; --i) {
		fputc(' ', stderr);
		fputc(' ', stderr);
	}
}

static void dbg_field_value(
		const void *const data,
		const o42a_dbg_field_info_t *const field_info,
		const int depth,
		const size_t indent);

static void dbg_struct(
		const o42a_dbg_header_t *const header,
		const int depth,
		const size_t indent) {

	const o42a_dbg_type_info_t *const type_info = header->type_info;
	const void *const data = (void*) header;
	const o42a_dbg_field_info_t *field_info = type_info->fields;
	size_t field_num = type_info->field_num;

	while (field_num > 0) {
		dbg_indent(indent);
		fputs(field_info->name, stderr);
		dbg_field_value(data + field_info->offset, field_info, depth, indent);
		fputc('\n', stderr);
		++field_info;
		--field_num;
	}
}

static void dbg_field_value(
		const void *const data,
		const o42a_dbg_field_info_t *const field_info,
		const int depth,
		const size_t indent) {
	switch (field_info->data_type) {
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
	case O42A_TYPE_PTR:
		fputs(": * -> ", stderr);

		void *data_ptr = *((void**) data);

		if (!data_ptr) {
			fputs("NULL", stderr);
			break;
		}

		fprintf(stderr, "<0x%lx>", (long) data_ptr);

		break;
	case O42A_TYPE_DATA_PTR: {

		const o42a_dbg_type_info_t *const type_info = field_info->type_info;

		if (type_info) {
			fprintf(stderr, ": *%s -> ", type_info->name);
		} else {
			fputs(": * -> ", stderr);
		}

		void *data_ptr = *((void**) data);

		if (!data_ptr) {
			fputs("NULL", stderr);
			break;
		}

		const o42a_dbg_header_t *const data_header =
				o42a_dbg_header(data_ptr);

		if (type_info && data_header->type_info != type_info) {
			fputs("(!) ", stderr);
		}

		dbg_mem_name(data_header);

		break;
	}
	case O42A_TYPE_STRUCT: {

		const o42a_dbg_type_info_t *const type_info = field_info->type_info;

		if (!depth) {
			fprintf(stderr, ": %s = ...\n", type_info->name);
			break;
		}

		const o42a_dbg_header_t *const data_header = o42a_dbg_header(data);

		if (data_header->type_info != type_info) {
			fprintf(
					stderr,
					"Incorrect pointer to %s, but %s expected",
					data_header->type_info->name,
					type_info->name);
			exit(EXIT_FAILURE);
		}

		fprintf(stderr, ": %s = {\n", type_info->name);
		dbg_struct(data_header, depth - 1, indent + 1);
		dbg_indent(indent);
		fputc('}', stderr);

		break;
	}
	default:
		fprintf(stderr, ": <unknown:%x>", field_info->data_type);
	}
}

void o42a_dbg_dump_mem(const void *const ptr, const uint32_t depth) {

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_mem_name(header);
	fprintf(stderr, " <0x%lx>: %s = {\n", (long) ptr, header->type_info->name);
	dbg_struct(header, depth, 1);
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
	O42A_ENTER;

	O42A_DEBUG("Type: %s\n", type_info->name);
	if (enclosing) {
		o42a_dbg_mem_name("Enclosing: ", enclosing);
	}

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
		O42A_RETURN;
	}

	const o42a_dbg_field_info_t *field_info = type_info->fields;

	for (;;) {
		if (field_info->data_type == O42A_TYPE_STRUCT) {

			o42a_dbg_header_t *const to =
					(o42a_dbg_header_t*)
					(((void*) header) + field_info->offset);

			o42a_dbg_fill_header(field_info->type_info, to, header);
			to->name = field_info->name;
		}

		if (!(--field_num)) {
			break;
		}
		++field_info;
	}

	O42A_RETURN;
}

void o42a_dbg_copy_header(
		const o42a_dbg_header_t *const from,
		o42a_dbg_header_t *const to,
		const o42a_dbg_header_t *const enclosing) {
	O42A_ENTER;

	o42a_dbg_mem_name("From: ", from);
	if (enclosing) {
		o42a_dbg_mem_name("Enclosing: ", enclosing);
	}

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
		O42A_RETURN;
	}

	const o42a_dbg_field_info_t *field_info = type_info->fields;

	for (;;) {
		if (field_info->data_type == O42A_TYPE_STRUCT) {

			const o42a_dbg_header_t *const f =
					(o42a_dbg_header_t*) (((void*) from) + field_info->offset);
			o42a_dbg_header_t *const t =
					(o42a_dbg_header_t*) (((void*) to) + field_info->offset);

			o42a_dbg_copy_header(f, t, to);
		}

		if (!(--field_num)) {
			break;
		}
		++field_info;
	}

	O42A_RETURN;
}

inline void o42a_dbg_fill_field_info(
		const o42a_dbg_header_t *const header,
		o42a_dbg_field_info_t *const field_info) {
	field_info->data_type = O42A_TYPE_STRUCT;
	field_info->offset = -header->enclosing;
	field_info->name = header->name;
	field_info->type_info = header->type_info;
}
