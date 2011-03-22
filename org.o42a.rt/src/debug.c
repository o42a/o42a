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


static o42a_dbg_env_t dbg_env = {
	stack_frame: NULL,
	command: O42A_DBG_CMD_EXEC,
	indent: 0,
};

static volatile sig_atomic_t program_error_in_progress = 0;

static void program_error_signal(int sig) {
	if (program_error_in_progress) {
		raise(sig);
	}
	program_error_in_progress = 1;

	fprintf(stderr, "Program error (%i) occurred at:\n", sig);
	o42a_dbg_print_stack_trace(dbg_env.stack_frame);

	signal(sig, SIG_DFL);
	raise(sig);
}

int32_t o42a_dbg_exec_main(
		int32_t (*main)(O42A_DECLS int32_t, char**),
		int32_t argc,
		char** argv) {

	o42a_dbg_env_t *__o42a_dbg_env__ = &dbg_env;

	O42A_ENTER(return 0);

	signal(SIGFPE, &program_error_signal);
	signal(SIGILL, &program_error_signal);
	signal(SIGSEGV, &program_error_signal);
	signal(SIGBUS, &program_error_signal);
	signal(SIGABRT, &program_error_signal);
	signal(SIGIOT, &program_error_signal);
	signal(SIGTRAP, &program_error_signal);
	signal(SIGSYS, &program_error_signal);

	O42A_DEBUG("Executing main\n");

	const int32_t result = main(O42A_ARGS argc, argv);

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


static void dbg_indent(o42a_dbg_env_t *const env, const uint8_t indent) {
	for (uint8_t i = (0x0f & (indent + env->indent)); i > 0; --i) {
		fputc(' ', stderr);
		fputc(' ', stderr);
	}
}

static inline void dbg_print_prefix(O42A_PARAMS o42a_dbg_env_t *env) {
	dbg_indent(__o42a_dbg_env__, 0);
	fprintf(stderr, "[%s] ", env->stack_frame->name);
}

void o42a_dbg_print(O42A_PARAMS const char *const message) {
	dbg_print_prefix(O42A_ARGS __o42a_dbg_env__);
	fputs(message, stderr);
}

void o42a_dbg_print_wo_prefix(O42A_PARAMS const char *const message) {
	fputs(message, stderr);
}

void o42a_dbg_printf(O42A_PARAMS const char *format, ...) {
	va_list args;

	va_start(args, format);
	dbg_print_prefix(O42A_ARGS __o42a_dbg_env__);

	vfprintf(stderr, format, args);

	va_end(args);
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
		O42A_PARAMS
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

static void dbg_func_name(O42A_PARAMS const void *const ptr) {
	if (!ptr) {
		fputs("NULL", stderr);
		return;
	}

	void (*const func) (o42a_dbg_env_t *) = (void (*) (o42a_dbg_env_t *)) ptr;

	const uint32_t old_command = __o42a_dbg_env__->command;

	__o42a_dbg_env__->command = O42A_DBG_CMD_REPORT;
	func(O42A_ARG);
	__o42a_dbg_env__->command = old_command;
}

void o42a_dbg_func_name(
		O42A_PARAMS
		const char *const prefix,
		const void *const ptr) {
	o42a_debug(prefix);
	dbg_func_name(O42A_ARGS ptr);
	fputc('\n', stderr);
}

static void dbg_exit(o42a_dbg_env_t *const env, o42a_bool_t print) {

	o42a_dbg_stack_frame_t *const stack_frame = env->stack_frame;

	assert(stack_frame || "Empty stack frame");

	o42a_dbg_stack_frame_t *const prev = stack_frame->prev;

	if (print) {
		dbg_indent(env, 0);
		if (!prev) {
			fprintf(stderr, ">> %s >>\n", stack_frame->name);
		} else {
			fprintf(stderr, ">> %s >> %s\n", stack_frame->name, prev->name);
		}
	}

	env->stack_frame = prev;
}

o42a_bool_t o42a_dbg_exec_command(o42a_dbg_env_t *env) {
	switch (env->command) {
	case O42A_DBG_CMD_REPORT:
		fputs(env->stack_frame->name, stderr);
		dbg_exit(env, O42A_FALSE);
		return O42A_TRUE;
	default:
		return O42A_FALSE;
	}
}

static void dbg_field_value(
		O42A_DECLS
		const void *const data,
		const o42a_dbg_field_info_t *const field_info,
		const int depth,
		const uint8_t indent);

static void dbg_struct(
		O42A_PARAMS
		const o42a_dbg_header_t *const header,
		const int depth,
		const uint8_t indent) {

	const o42a_dbg_type_info_t *const type_info = header->type_info;
	const void *const data = (void*) header;
	const o42a_dbg_field_info_t *field_info = type_info->fields;
	size_t field_num = type_info->field_num;

	while (field_num > 0) {
		dbg_indent(__o42a_dbg_env__, indent);
		fputs(field_info->name, stderr);

		const void *field_ptr = data + field_info->offset;

		fprintf(stderr, " <0x%lx>", (long) field_ptr);
		dbg_field_value(
				O42A_ARGS
				field_ptr,
				field_info,
				depth,
				indent);
		fputc('\n', stderr);
		++field_info;
		--field_num;
	}
}

static void dbg_field_value(
		O42A_PARAMS
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
			fprintf(stderr, ": int8 = %1$i (%1$#x)", val);
		}

		break;
	}
	case O42A_TYPE_INT16: {

		const int val = *((int16_t*) data);

		if (!val) {
			fputs(": int16 = 0", stderr);
		} else {
			fprintf(stderr, ": int16 = %1$i (%1$#x)", val);
		}

		break;
	}
	case O42A_TYPE_INT32: {

		const long val = *((int32_t*) data);

		if (!val) {
			fputs(": int32 = 0", stderr);
		} else {
			fprintf(stderr, ": int32 = %1$li (%1$#lx)", val);
		}

		break;
	}
	case O42A_TYPE_INT64: {

		const long long val = *((int64_t*) data);

		if (!val) {
			fputs(": int64 = 0", stderr);
		} else {
			fprintf(stderr, ": int64 = %1$lli (%1$#llx)", val);
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
		dbg_func_name(__o42a_dbg_env__, func_ptr);

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
		dbg_struct(O42A_ARGS data_header, depth - 1, indent + 1);
		dbg_indent(__o42a_dbg_env__, indent);
		fputc('}', stderr);

		break;
	}
	default:
		fprintf(stderr, ": <unknown:%x>", field_info->data_type);
	}
}

void o42a_dbg_dump_mem(
		O42A_PARAMS
		const void *const ptr,
		const uint32_t depth) {

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_indent(__o42a_dbg_env__, 0);
	dbg_mem_name(header);
	fprintf(stderr, " <0x%lx>: %s = {\n", (long) ptr, header->type_info->name);
	dbg_struct(O42A_ARGS header, depth, 1);
	dbg_indent(__o42a_dbg_env__, 0);
	fputs("}\n", stderr);
}

void o42a_dbg_enter(o42a_dbg_env_t *const env) {
	dbg_indent(env, 0);
	fprintf(stderr, "<< %s <<\n", env->stack_frame->name);
}

void o42a_dbg_exit(o42a_dbg_env_t *const env) {
	dbg_exit(env, O42A_TRUE);
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
		O42A_PARAMS
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

			o42a_dbg_fill_header(O42A_ARGS field_info->type_info, to, header);
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
		O42A_PARAMS
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

			o42a_dbg_copy_header(O42A_ARGS f, t, to);
		}

		if (!(--field_num)) {
			break;
		}
		++field_info;
	}

	O42A_RETURN;
}

inline void o42a_dbg_fill_field_info(
		O42A_PARAMS
		const o42a_dbg_header_t *const header,
		o42a_dbg_field_info_t *const field_info) {
	field_info->data_type = O42A_TYPE_STRUCT;
	field_info->offset = -header->enclosing;
	field_info->name = header->name;
	field_info->type_info = header->type_info;
}
