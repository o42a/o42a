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
#include "llvm/Support/raw_ostream.h"

#ifndef O42AC_LLVM_DEBUG_H
#define O42AC_LLVM_DEBUG_H


#ifdef O42AC_NDEBUG


#define ODEBUG_WITH_TYPE(TYPE, X) do { } while (0)
#define ODEBUG(X)
#define OTRACE(X)
#define ODUMP(X)
#define ODDUMP(X)
#define OCODE(BLOCK, X)


#else


#define ODEBUG_WITH_TYPE(TYPE, X) \
	do { \
		if (::o42ac::debugEnabled(TYPE)) { X; } \
	} while (0)
#define ODEBUG(X) ODEBUG_WITH_TYPE("debug", llvm::errs() << X)
#define OTRACE(X) ODEBUG_WITH_TYPE("trace", llvm::errs() << "trace: " << X)
#define ODUMP(X) \
	ODEBUG_WITH_TYPE("trace", llvm::errs() << ">>>>>> " << *(X) << "\n")
#define ODDUMP(X) \
	ODEBUG_WITH_TYPE("debug", llvm::errs() << ">>>>>> " << *(X) << "\n")
#define OCODE(BLOCK, X) ODEBUG_WITH_TYPE( \
		"trace", \
		llvm::errs() << "[" << (BLOCK)->getName() << "] " << X)


namespace o42ac {

bool debugEnabled(const char*);

}

#endif /* O42AC_NDEBUG */

#endif /* O42AC_LLVM_DEBUG_H */
