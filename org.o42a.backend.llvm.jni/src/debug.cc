/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010-2014 Ruslan Lopatin

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
#include "o42ac/llvm/debug.h"

#include <string.h>

#include "llvm/Support/CommandLine.h"


using namespace llvm;

namespace o42ac {

static cl::list<std::string> DebugMode(
		"debug-o42ac",
		cl::ValueOptional,
		cl::CommaSeparated,
		cl::desc("Compiler debug mode"),
		cl::value_desc("mode"));

static bool debugInitialized;

static bool traceModeEnabled;
static bool debugModeEnabled;

bool debugEnabled(const char *const mode) {
	if (!debugInitialized) {
		debugInitialized = true;
		if (!DebugMode.getNumOccurrences()) {
			return false;
		}

		const size_t size = DebugMode.size();

		for (size_t i = 0; i < size; ++i) {

			const std::string opt = DebugMode[i];

			if (opt == "all") {
				traceModeEnabled = true;
				debugModeEnabled = true;
			} else if (opt == "trace") {
				traceModeEnabled = true;
			} else if (opt == "debug") {
				debugModeEnabled = true;
			}
		}
	}

	if (!strcmp(mode, "trace")) {
		return traceModeEnabled;
	}
	if (!strcmp(mode, "debug")) {
		return debugModeEnabled;
	}

	return false;
}

}
