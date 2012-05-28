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
#include "o42a/debug.h"

#include <assert.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>

#include "unicode/ustdio.h"


const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_rlist = {
	.type_code = 0x042a0001,
	.field_num = 2,
	.name = "o42a_rlist_t",
	.fields = {
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_rlist_t, list),
			.name = "list",
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_rlist_t, size),
			.name = "size",
		},
	},
};

static __thread o42a_dbg_env_t *dbg_env;

void o42a_dbg_start_thread(struct o42a_dbg_env *env) {
	dbg_env = env;
}

static volatile sig_atomic_t program_error_in_progress = 0;

static void program_error_signal(int sig) {
	if (program_error_in_progress) {
		raise(sig);
	}
	program_error_in_progress = 1;

	fprintf(stderr, "Program error (%i) occurred at:\n", sig);
	o42a_dbg_print_stack_trace(dbg_env->stack_frame);

	signal(sig, SIG_DFL);
	raise(sig);
}

int32_t o42a_dbg_exec_main(
		int32_t (*main)(int32_t, char**),
		int32_t argc,
		char** argv) {
	O42A_START_THREAD;
	O42A_ENTER(return 0);

	signal(SIGFPE, &program_error_signal);
	signal(SIGILL, &program_error_signal);
	signal(SIGSEGV, &program_error_signal);
	signal(SIGBUS, &program_error_signal);
	signal(SIGABRT, &program_error_signal);
	signal(SIGIOT, &program_error_signal);
	signal(SIGTRAP, &program_error_signal);
	signal(SIGSYS, &program_error_signal);

	o42a_debug("Executing main\n");

	const int32_t result = O42A(main(argc, argv));

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


static void dbg_indent(const uint8_t indent) {
	for (uint8_t i = (0x0f & (indent + dbg_env->indent)); i > 0; --i) {
		fputc(' ', stderr);
		fputc(' ', stderr);
	}
}

static inline void dbg_print_prefix() {
	dbg_indent(0);
	fprintf(stderr, "[%s] ", dbg_env->stack_frame->name);
}

void o42a_dbg_print(const char *const message) {
	dbg_print_prefix();
	fputs(message, stderr);
}

void o42a_dbg_print_wo_prefix(const char *const message) {
	fputs(message, stderr);
}

void o42a_dbg_printf(const char *format, ...) {
	va_list args;

	va_start(args, format);
	dbg_print_prefix();

	vfprintf(stderr, format, args);

	va_end(args);
}

static inline void dbg_mem_name(const o42a_dbg_header_t *const header) {

	const o42a_rptr_t enclosing = header->enclosing;

	if (enclosing) {
		dbg_mem_name(o42a_dbg_header(((char *) header) + enclosing));
		fputc(':', stderr);
	}

	fputs(header->name, stderr);
}

void o42a_dbg_mem_name(
		const char *const prefix,
		const void *const ptr) {
	o42a_dbg_print(prefix);
	if (!ptr) {
		fputs("NULL\n", stderr);
		return;
	}

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_mem_name(header);
	fprintf(stderr, " <0x%lx>: %s\n", (long) ptr, header->type_info->name);
}

union ptr_and_func {
	const void *ptr;
	void (*const func) ();
};

static void dbg_func_name(const void *const ptr) {
	if (!ptr) {
		fputs("NULL", stderr);
		return;
	}

	const union ptr_and_func val = {.ptr = ptr};

	o42a_dbg_env_t *const env = dbg_env;
	const uint32_t old_command = env->command;

	env->command = O42A_DBG_CMD_REPORT;
	val.func();
	env->command = old_command;
}

void o42a_dbg_func_name(const char *const prefix, const void *const ptr) {
	o42a_dbg_print(prefix);
	dbg_func_name(ptr);
	fputc('\n', stderr);
}

static void dbg_field_value(
		const void *const data,
		const o42a_dbg_field_info_t *const field_info,
		const int depth,
		const uint8_t indent);

static void dbg_struct(
		const o42a_dbg_header_t *const header,
		const int depth,
		const uint8_t indent) {

	const o42a_dbg_type_info_t *const type_info = header->type_info;
	const void *const data = (void*) header;
	const o42a_dbg_field_info_t *field_info = type_info->fields;
	size_t field_num = type_info->field_num;

	while (field_num > 0) {
		dbg_indent(indent);
		fputs(field_info->name, stderr);

		const void *field_ptr = ((char *) data) + field_info->offset;

		fprintf(stderr, " <0x%lx>", (long) field_ptr);
		dbg_field_value(field_ptr, field_info, depth, indent);
		fputc('\n', stderr);
		++field_info;
		--field_num;
	}
}

static void dbg_field_value(
		const void *const data,
		const o42a_dbg_field_info_t *const field_info,
		const int depth,
		const uint8_t indent) {
	switch (field_info->data_type) {
	case O42A_TYPE_INT8: {

		const int val = *((int8_t*) data);

		if (!val) {
			fputs(": int8 = 0", stderr);
		} else {
			fprintf(stderr, ": int8 = %i (%#x)", val, val);
		}

		break;
	}
	case O42A_TYPE_INT16: {

		const int val = *((int16_t*) data);

		if (!val) {
			fputs(": int16 = 0", stderr);
		} else {
			fprintf(stderr, ": int16 = %i (%#x)", val, val);
		}

		break;
	}
	case O42A_TYPE_INT32: {

		const long val = *((int32_t*) data);

		if (!val) {
			fputs(": int32 = 0", stderr);
		} else {
			fprintf(stderr, ": int32 = %li (%#lx)", val, val);
		}

		break;
	}
	case O42A_TYPE_INT64: {

		const long long val = *((int64_t*) data);

		if (!val) {
			fputs(": int64 = 0", stderr);
		} else {
			fprintf(stderr, ": int64 = %lli (%#llx)", val, val);
		}

		break;
	}
	case O42A_TYPE_FP32:
		fprintf(stderr, ": fp32 = %f", (double) (*((float*) data)));
		break;
	case O42A_TYPE_FP64:
		fprintf(stderr, ": fp64 = %f", *((double*) data));
		break;
	case O42A_TYPE_CODE_PTR: {

		void *func_ptr = *((void**) data);

		fputs(": code -> ", stderr);
		if (!func_ptr) {
			fputs("NULL", stderr);
			break;
		}
		fprintf(stderr, "<0x%lx>", (long) func_ptr);

		break;
	}
	case O42A_TYPE_FUNC_PTR: {

		void *func_ptr = *((void**) data);

		fputs(": function -> ", stderr);
		dbg_func_name(func_ptr);

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
		break;
	}
}

void o42a_dbg_dump_mem(
		const char *const prefix,
		const void *const ptr,
		const uint32_t depth) {
	o42a_dbg_print(prefix);

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_mem_name(header);
	fprintf(stderr, " <0x%lx>: %s = {\n", (long) ptr, header->type_info->name);
	dbg_struct(header, depth, 1);
	dbg_indent(0);
	fputs("}\n", stderr);
}

static void dbg_exit(o42a_dbg_env_t *const env, o42a_bool_t print) {

	o42a_dbg_stack_frame_t *const stack_frame = env->stack_frame;

	assert(stack_frame && "Empty stack frame");

	o42a_dbg_stack_frame_t *const prev = stack_frame->prev;

	if (print) {
		dbg_indent(0);
		if (!prev) {
			fprintf(stderr, ">> %s >>\n", stack_frame->name);
		} else {
			fprintf(stderr, ">> %s >> %s\n", stack_frame->name, prev->name);
		}
	}

	env->stack_frame = prev;
}

o42a_bool_t o42a_dbg_enter(o42a_dbg_stack_frame_t *const stack_frame) {

	o42a_dbg_env_t *const env = dbg_env;

	stack_frame->prev = env->stack_frame;
	env->stack_frame = stack_frame;

	if (env->command == O42A_DBG_CMD_REPORT) {
		fputs(env->stack_frame->name, stderr);
		dbg_exit(env, O42A_FALSE);
		return O42A_FALSE;
	}

	dbg_indent(0);
	fprintf(stderr, "<< %s <<\n", env->stack_frame->name);

	return O42A_TRUE;
}

void o42a_dbg_exit() {
	dbg_exit(dbg_env, O42A_TRUE);
}

void o42a_dbg_do(
		o42a_dbg_stack_frame_t *const stack_frame,
		const char *const comment) {

	o42a_dbg_env_t *const env = dbg_env;
	o42a_dbg_stack_frame_t *const prev = env->stack_frame;

	stack_frame->name = prev->name;
	stack_frame->prev = prev;
	prev->line = stack_frame->line;
	prev->comment = comment;
	if (stack_frame->file) {
		o42a_dbg_printf(
				"((( /* %s */ (%s:%lu)\n",
				comment,
				stack_frame->file,
				(unsigned long) stack_frame->line);
	} else {
		o42a_dbg_printf("((( /* %s */\n", comment);
	}
	env->stack_frame = stack_frame;
	++env->indent;
}

void o42a_dbg_done(const uint32_t line) {

	o42a_dbg_env_t *const env = dbg_env;

	--env->indent;
	o42a_dbg_stack_frame_t *const prev = env->stack_frame->prev;
	if (env->stack_frame->file) {
		o42a_dbg_printf(
				"))) /* %s */ (%s:%lu)\n",
				prev->comment,
				prev->file,
				(unsigned long) line);
	} else {
		o42a_dbg_printf("))) /* %s */\n", prev->comment);
	}
	prev->line = line;
	prev->comment = NULL;
	env->stack_frame = prev;
}

void o42a_dbg_set_line(uint32_t line) {
	dbg_env->stack_frame->line = line;
}

inline void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t *const frame) {
	fputs(frame->name, stderr);
	if (frame->file) {
		fputs(" (", stderr);
		fputs(frame->file, stderr);
		if (frame->line) {
			fprintf(stderr, ":%lu", (long) frame->line);
		}
		fputc(')', stderr);
	}
	if (frame->comment) {
		fputs(" /* ", stderr);
		fputs(frame->comment, stderr);
		fputs(" */", stderr);
	}
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
	O42A_ENTER(return);

	O42A_DEBUG("Type: %s\n", type_info->name);
	if (enclosing) {
		o42a_debug_mem_name("Enclosing: ", enclosing);
	}

	header->type_code = type_info->type_code;
	header->name = type_info->name;
	header->type_info = type_info;
	if (enclosing) {
		header->enclosing = ((char *) enclosing) - ((char *) header);
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
					(((char *) header) + field_info->offset);

			o42a_dbg_fill_header(
					field_info->type_info,
					to,
					header);
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
	O42A_ENTER(return);

	o42a_debug_mem_name("From: ", from);
	if (enclosing) {
		o42a_debug_mem_name("Enclosing: ", enclosing);
	}

	to->type_code = from->type_code;
	to->name = from->name;
	to->type_info = from->type_info;
	if (enclosing) {
		to->enclosing = ((char *) enclosing) - ((char *) to);
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
					(o42a_dbg_header_t*) (((char *) from) + field_info->offset);
			o42a_dbg_header_t *const t =
					(o42a_dbg_header_t*) (((char *) to) + field_info->offset);

			O42A(o42a_dbg_copy_header(f, t, to));
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
