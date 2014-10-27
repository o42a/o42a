/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/debug.h"

#include <assert.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

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

inline o42a_bool_t o42a_dbg_ison(uint32_t debug_type) {
	return dbg_env->enabled_debug_types & debug_type;
}

static const char *dbg_thread_getenv(
		const char *const thread_name,
		const char *const env_name) {

	const ssize_t tname_len = thread_name ? strlen(thread_name) : 0;
	const size_t ename_len = strlen(env_name);
	char full_env_name[14 + ename_len + tname_len];
	char *str = full_env_name;

	memcpy(str, "O42A_THREAD_", 12);

	if (!thread_name) {
		memcpy(str + 12, env_name, ename_len + 1);
		return getenv(full_env_name);
	}

	memcpy(str += 12, env_name, ename_len);
	str += ename_len;
	*str = '_';
	memcpy(str + 1, thread_name, tname_len + 1);

	const char *result = getenv(full_env_name);

	if (result) {
		return result;
	}
	// No value for the given thread.
	// Retrieve the default value.
	return dbg_thread_getenv(NULL, env_name);
}

static o42a_bool_t dbg_thread_isenv(
		const char *const thread_name,
		const char *const env_name,
		o42a_bool_t default_value) {

	const char *value = dbg_thread_getenv(thread_name, env_name);

	if (!value) {
		return default_value;
	}

	if (default_value) {
		if (!strcmp(value, "0")) {
			return O42A_FALSE;
		}
		if (!strcasecmp(value, "off")) {
			return O42A_FALSE;
		}
		if (!strcasecmp(value, "false")) {
			return O42A_FALSE;
		}
	} else {
		if (!strcmp(value, "1")) {
			return O42A_TRUE;
		}
		if (!strcasecmp(value, "on")) {
			return O42A_TRUE;
		}
		if (!strcasecmp(value, "true")) {
			return O42A_TRUE;
		}
	}

	return default_value;
}

static o42a_bool_t dbg_flag_enabled(
		const char *const value,
		const char *const flag_name,
		size_t flag_name_len,
		o42a_bool_t default_value) {

	const char *found = strstr(value, flag_name);

	if (!found) {
		// The flag is not found.
		return default_value;
	}
	if (!flag_name_len) {
		flag_name_len = strlen(flag_name);
	}

	const char suffix = *(found + flag_name_len);

	if (suffix == '\0' || suffix == ',' || suffix == ' ') {
		// Flag name is not a prefix of another flag name.
		if (found == value) {
			// The flag is the very first one in the list.
			return O42A_TRUE;
		}

		const char prefix = *(found - 1);

		if (prefix == '-') {
			// The flag is disabled.
			return O42A_FALSE;
		}
		if (prefix == '+' || prefix == ',' || prefix == ' ') {
			// The flag is enabled.
			return O42A_TRUE;
		}
	}

	// Wrong flag (i.e. suffix or prefix of another flag name).
	// Search for the flag after the next colon.
	const char *colon = index(value + flag_name_len, ',');

	if (!colon) {
		// That was a last flag.
		return default_value;
	}

	return dbg_flag_enabled(
			colon + 1,
			flag_name,
			flag_name_len,
			default_value);
}

static uint32_t dbg_thread_debug_types(const char *const thread_name) {

	const char *value = dbg_thread_getenv(thread_name, "DEBUG");

	if (!value) {
		return O42A_DBG_TYPE_ALL;
	}

	uint32_t debug_types = 0;
	const o42a_bool_t all = dbg_flag_enabled(value, "all", 3, O42A_FALSE);

	if (dbg_flag_enabled(value, "default", 7, all)) {
		debug_types |= O42A_DBG_TYPE_DEFAULT;
	}
	if (dbg_flag_enabled(value, "gc", 2, all)) {
		debug_types |= O42A_DBG_TYPE_GC;
	}

	return debug_types;
}

void o42a_dbg_start_thread(struct o42a_dbg_env *env) {
	env->options = o42a_dbg_default_options;
	env->output = stderr;
	env->enabled_debug_types = O42A_DBG_TYPE_ALL;
	dbg_env = env;

	// Set the output according to the environment variable.
	const char *const file = dbg_thread_getenv(env->thread_name, "LOG");

	if (file) {
		env->output = fopen(file, "w");
		if (env->output) {
			setvbuf(env->output, (char *) NULL, _IOLBF, 0);
		} else {
			env->output = stderr;
			fprintf(stderr, "Cannot write to log file: %s\n", file);
		}
	}

	// Set debug options.
	const o42a_bool_t quiet = dbg_thread_isenv(
			env->thread_name,
			"QUIET",
			o42a_dbg_default_options.quiet);

	env->options.quiet = quiet;
	env->options.no_debug_messages = dbg_thread_isenv(
			env->thread_name,
			"NO_DEBUG_MESSAGES",
			quiet || o42a_dbg_default_options.no_debug_messages);
	env->options.debug_blocks_omitted = dbg_thread_isenv(
			env->thread_name,
			"DEBUG_BLOCKS_OMITTED",
			quiet || o42a_dbg_default_options.debug_blocks_omitted);
	env->options.silent_calls = dbg_thread_isenv(
			env->thread_name,
			"SILENT_CALLS",
			quiet || o42a_dbg_default_options.silent_calls);
	env->enabled_debug_types = dbg_thread_debug_types(env->thread_name);
}

static volatile sig_atomic_t program_error_in_progress = 0;

static void program_error_signal(int sig) {
	if (program_error_in_progress) {
		raise(sig);
	}
	program_error_in_progress = 1;

	fprintf(
			dbg_env->output,
			"Program error (%i) occurred at thread \"%s\":\n",
			sig,
			dbg_env->thread_name);
	o42a_dbg_print_stack_trace(dbg_env->stack_frame);

	signal(sig, SIG_DFL);
	raise(sig);
}

int32_t o42a_dbg_exec_main(
		int32_t (*main)(int32_t, char**),
		int32_t argc,
		char** argv) {
	O42A_START_THREAD("main");
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

	const o42a_dbg_header_t *const header = ptr;
	const o42a_dbg_type_info_t *const type_info = header->type_info;

	if (header->type_code != type_info->type_code) {
		fprintf(
				dbg_env->output,
				"Wrong debug header at <%#lx>\n",
				(long) header);
		exit(EXIT_FAILURE);
	}

	return header;
}


static void dbg_indent(const uint8_t indent) {
	for (uint8_t i = (0x0f & (indent + dbg_env->indent)); i > 0; --i) {
		fputc(' ', dbg_env->output);
		fputc(' ', dbg_env->output);
	}
}

static inline void dbg_print_prefix() {
	dbg_indent(0);
	fprintf(dbg_env->output, "[%s] ", dbg_env->stack_frame->name);
}

static inline void dbg_print(const char *const message) {
	dbg_print_prefix();
	fputs(message, dbg_env->output);
}

void o42a_dbg_print(const char *const message) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}
	dbg_print(message);
}

void o42a_dbg_print_nl(const char *const message) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}
	dbg_print(message);
	fputs("\n", dbg_env->output);
}

static inline void dbg_printf(const char *format, ...) {
	va_list args;

	va_start(args, format);
	dbg_print_prefix();

	vfprintf(dbg_env->output, format, args);

	va_end(args);
}

void o42a_dbg_printf(const char *format, ...) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}
	va_list args;

	va_start(args, format);
	dbg_print_prefix();

	vfprintf(dbg_env->output, format, args);

	va_end(args);
}

static inline void dbg_mem_name(const o42a_dbg_header_t *const header) {

	const o42a_rptr_t enclosing = header->enclosing;

	if (enclosing) {
		dbg_mem_name(o42a_dbg_header(((char *) header) + enclosing));
		fputc(':', dbg_env->output);
	}

	fputs(header->name, dbg_env->output);
}

void o42a_dbg_mem_name(
		const char *const prefix,
		const void *const ptr) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}
	dbg_print(prefix);
	if (!ptr) {
		fputs("NULL\n", dbg_env->output);
		return;
	}
	fprintf(dbg_env->output, "<%#lx> ", (long) ptr);

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_mem_name(header);
	fprintf(dbg_env->output, ": %s\n", header->type_info->name);
}

union ptr_and_func {
	const void *ptr;
	void (*const func) ();
};

static void dbg_func_name(void (*const func)()) {

	o42a_dbg_env_t *const env = dbg_env;

	if (!func) {
		fputs("NULL", env->output);
		return;
	}

	const uint32_t old_command = env->command;

	env->command = O42A_DBG_CMD_REPORT;
	func();
	env->command = old_command;
}

void o42a_dbg_func_name(const char *const prefix, void (*const func)()) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}
	dbg_print(prefix);
	dbg_func_name(func);
	fputc('\n', dbg_env->output);
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
		fputs(field_info->name, dbg_env->output);

		const void *field_ptr = ((char *) data) + field_info->offset;

		fprintf(dbg_env->output, " <%#lx>", (long) field_ptr);
		dbg_field_value(field_ptr, field_info, depth, indent);
		fputc('\n', dbg_env->output);
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
			fputs(": int8 = 0", dbg_env->output);
		} else {
			fprintf(dbg_env->output, ": int8 = %i (%#x)", val, val);
		}

		break;
	}
	case O42A_TYPE_INT16: {

		const int val = *((int16_t*) data);

		if (!val) {
			fputs(": int16 = 0", dbg_env->output);
		} else {
			fprintf(dbg_env->output, ": int16 = %i (%#x)", val, val);
		}

		break;
	}
	case O42A_TYPE_INT32: {

		const long val = *((int32_t*) data);

		if (!val) {
			fputs(": int32 = 0", dbg_env->output);
		} else {
			fprintf(dbg_env->output, ": int32 = %li (%#lx)", val, val);
		}

		break;
	}
	case O42A_TYPE_INT64: {

		const long long val = *((int64_t*) data);

		if (!val) {
			fputs(": int64 = 0", dbg_env->output);
		} else {
			fprintf(dbg_env->output, ": int64 = %lli (%#llx)", val, val);
		}

		break;
	}
	case O42A_TYPE_FP32:
		fprintf(dbg_env->output, ": fp32 = %f", (double) (*((float*) data)));
		break;
	case O42A_TYPE_FP64:
		fprintf(dbg_env->output, ": fp64 = %f", *((double*) data));
		break;
	case O42A_TYPE_CODE_PTR: {

		void *func_ptr = *((void**) data);

		fputs(": code -> ", dbg_env->output);
		if (!func_ptr) {
			fputs("NULL", dbg_env->output);
			break;
		}
		fprintf(dbg_env->output, "<%#lx>", (long) func_ptr);

		break;
	}
	case O42A_TYPE_FUNC_PTR: {

		union ptr_and_func func_ptr = {.ptr = *((void**) data)};

		fputs(": function -> ", dbg_env->output);
		dbg_func_name(func_ptr.func);

		break;
	}
	case O42A_TYPE_REL_PTR: {

		const o42a_rptr_t val = *((o42a_rptr_t*) data);

		fprintf(dbg_env->output, ": rptr = %+li", (long) val);

		break;
	}
	case O42A_TYPE_PTR:
		fputs(": * -> ", dbg_env->output);

		void *data_ptr = *((void**) data);

		if (!data_ptr) {
			fputs("NULL", dbg_env->output);
			break;
		}

		fprintf(dbg_env->output, "<%#lx>", (long) data_ptr);

		break;
	case O42A_TYPE_DATA_PTR: {

		const o42a_dbg_type_info_t *const type_info = field_info->type_info;

		if (type_info) {
			fprintf(dbg_env->output, ": *%s -> ", type_info->name);
		} else {
			fputs(": * -> ", dbg_env->output);
		}

		void *data_ptr = *((void**) data);

		if (!data_ptr) {
			fputs("NULL", dbg_env->output);
			break;
		}

		fprintf(dbg_env->output, "<%#lx> ", (long) data_ptr);

		const o42a_dbg_header_t *const data_header =
				o42a_dbg_header(data_ptr);

		if (type_info && data_header->type_info != type_info) {
			fputs("(!) ", dbg_env->output);
		}

		dbg_mem_name(data_header);

		break;
	}
	case O42A_TYPE_STRUCT: {

		const o42a_dbg_type_info_t *const type_info = field_info->type_info;

		if (!depth) {
			fprintf(dbg_env->output, ": %s = ...\n", type_info->name);
			break;
		}

		const o42a_dbg_header_t *const data_header = o42a_dbg_header(data);

		if (data_header->type_info != type_info) {
			fprintf(
					dbg_env->output,
					"Incorrect pointer to %s, but %s expected",
					data_header->type_info->name,
					type_info->name);
			exit(EXIT_FAILURE);
		}

		fprintf(dbg_env->output, ": %s = {\n", type_info->name);
		dbg_struct(data_header, depth - 1, indent + 1);
		dbg_indent(indent);
		fputc('}', dbg_env->output);

		break;
	}
	default:
		fprintf(dbg_env->output, ": <unknown:%x>", field_info->data_type);
		break;
	}
}

void o42a_dbg_dump_mem(
		const char *const prefix,
		const void *const ptr,
		const uint32_t depth) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}
	dbg_print(prefix);
	fprintf(dbg_env->output, "<%#lx> ", (long) ptr);

	const o42a_dbg_header_t *const header = o42a_dbg_header(ptr);

	dbg_mem_name(header);
	fprintf(dbg_env->output, ": %s = {\n", header->type_info->name);
	dbg_struct(header, depth, 1);
	dbg_indent(0);
	fputs("}\n", dbg_env->output);
}

static void dbg_exit(o42a_dbg_env_t *const env, o42a_bool_t print) {

	o42a_dbg_stack_frame_t *const stack_frame = env->stack_frame;

	assert(stack_frame && "Empty stack frame");

	o42a_dbg_stack_frame_t *const prev = stack_frame->prev;

	if (print) {
		dbg_indent(0);
		if (!prev) {
			fprintf(env->output, ">> %s >>\n", stack_frame->name);
		} else {
			fprintf(
					env->output,
					">> %s >> %s\n",
					stack_frame->name,
					prev->name);
		}
	}

	env->stack_frame = prev;
}

o42a_bool_t o42a_dbg_enter(o42a_dbg_stack_frame_t *const stack_frame) {

	o42a_dbg_env_t *const env = dbg_env;

	stack_frame->prev = env->stack_frame;
	env->stack_frame = stack_frame;

	if (env->command == O42A_DBG_CMD_REPORT) {
		fputs(env->stack_frame->name, env->output);
		dbg_exit(env, O42A_FALSE);
		return O42A_FALSE;
	}
	if (!dbg_env->options.silent_calls) {
		dbg_indent(0);
		fprintf(env->output, "<< %s <<\n", env->stack_frame->name);
	}

	return O42A_TRUE;
}

void o42a_dbg_exit() {
	dbg_exit(dbg_env, !dbg_env->options.silent_calls);
}

void o42a_dbg_do(
		o42a_dbg_stack_frame_t *const stack_frame,
		const char *const comment) {
	if (dbg_env->options.debug_blocks_omitted) {
		return;
	}

	o42a_dbg_env_t *const env = dbg_env;
	o42a_dbg_stack_frame_t *const prev = env->stack_frame;

	stack_frame->name = prev->name;
	stack_frame->prev = prev;
	prev->line = stack_frame->line;
	prev->comment = comment;
	if (stack_frame->file) {
		dbg_printf(
				"((( /* %s */ (%s:%lu)\n",
				comment,
				stack_frame->file,
				(unsigned long) stack_frame->line);
	} else {
		dbg_printf("((( /* %s */\n", comment);
	}
	env->stack_frame = stack_frame;
	++env->indent;
}

void o42a_dbg_done(const uint32_t line) {
	if (dbg_env->options.debug_blocks_omitted) {
		return;
	}

	o42a_dbg_env_t *const env = dbg_env;

	--env->indent;
	o42a_dbg_stack_frame_t *const prev = env->stack_frame->prev;
	if (env->stack_frame->file) {
		dbg_printf(
				"))) /* %s */ (%s:%lu)\n",
				prev->comment,
				prev->file,
				(unsigned long) line);
	} else {
		dbg_printf("))) /* %s */\n", prev->comment);
	}
	prev->line = line;
	prev->comment = NULL;
	env->stack_frame = prev;
}

void o42a_dbg_set_line(uint32_t line) {
	dbg_env->stack_frame->line = line;
}

static inline void dbg_print_stack_frame(
		FILE *const out,
		o42a_dbg_stack_frame_t *const frame) {
	fputs(frame->name, out);
	if (frame->file) {
		fputs(" (", out);
		fputs(frame->file, out);
		if (frame->line) {
			fprintf(out, ":%lu", (long) frame->line);
		}
		fputc(')', out);
	}
	if (frame->comment) {
		fputs(" /* ", out);
		fputs(frame->comment, out);
		fputs(" */", out);
	}
}

void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t *const frame) {
	dbg_print_stack_frame(dbg_env->output, frame);
}

void o42a_dbg_print_stack_trace(o42a_dbg_stack_frame_t *frame) {

	FILE *const out = dbg_env->output;

	while (frame) {
		fputs("  ", out);
		dbg_print_stack_frame(out, frame);
		fputc('\n', out);
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

typedef struct stack_dump_data_frame {

	const char *name;

	const char *comment;

	const char *file;

	uint32_t line;

} stack_dump_data_frame_t;

typedef struct stack_dump_data {

	size_t size;

	stack_dump_data_frame_t frames[];

} stack_dump_data_t;

o42a_dbg_stack_dump_t o42a_dbg_stack_dump(size_t skip_frames) {

	const o42a_dbg_stack_frame_t *frame = dbg_env->stack_frame;
	o42a_dbg_stack_dump_t dump = {
		.stack_frame = NULL,
	};

	// Calculate the number of stack frames.
	size_t num_frames = 0;

	while (frame) {
		if (skip_frames) {
			--skip_frames;
		} else {
			if (!num_frames) {
				dump.stack_frame = frame;
			}
			++num_frames;
		}
		frame = frame->prev;
	}

	// Calculate the dump data size.
	dump.size =
			sizeof(stack_dump_data_t)
			+ sizeof(stack_dump_data_frame_t) * num_frames;

	return dump;
}

void o42a_dbg_fill_stack_dump(
		const o42a_dbg_stack_dump_t *const dump,
		void *data) {

	stack_dump_data_t *const dump_data = data;
	size_t frame_num = 0;
	const o42a_dbg_stack_frame_t *frame = dump->stack_frame;

	while (frame) {

		stack_dump_data_frame_t *const data_frame =
				dump_data->frames + frame_num;

		data_frame->name = frame->name;
		data_frame->comment = frame->comment;
		data_frame->file = frame->file;
		data_frame->line = frame->line;
		++frame_num;
		frame = frame->prev;
	}

	dump_data->size = frame_num;
}

static inline void dbg_print_stack_dump_frame(
		FILE *const out,
		const stack_dump_data_frame_t *const frame) {
	fputs(frame->name, out);
	if (frame->file) {
		fputs(" (", out);
		fputs(frame->file, out);
		if (frame->line) {
			fprintf(out, ":%lu", (long) frame->line);
		}
		fputc(')', out);
	}
	if (frame->comment) {
		fputs(" /* ", out);
		fputs(frame->comment, out);
		fputs(" */", out);
	}
}

void o42a_dbg_print_stack_dump(void *data) {
	if (dbg_env->options.no_debug_messages) {
		return;
	}

	FILE *const out = dbg_env->output;
	const stack_dump_data_t *const dump_data = data;
	size_t num_frames = dump_data->size;

	for (size_t i = 0; i < num_frames; ++i) {
		dbg_indent(1);
		dbg_print_stack_dump_frame(out, dump_data->frames + i);
		fputc('\n', out);
	}
}
