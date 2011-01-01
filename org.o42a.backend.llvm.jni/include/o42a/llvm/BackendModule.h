/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010 Ruslan Lopatin

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
#ifndef O42A_BACKEND_MODULE_H
#define O42A_BACKEND_MODULE_H

#include "llvm/Module.h"

using namespace llvm;

namespace llvm {

class Constant;
class Function;
class FunctionPassManager;
class TargetData;

}

namespace o42ac {

class BackendModule : public Module {

	mutable TargetData *targetData;
	mutable FunctionPassManager *functionPassManager;

	explicit BackendModule(StringRef ModuleID, LLVMContext &context);

	~BackendModule();

public:

	static BackendModule *createBackend(StringRef &ModuleID);

	const TargetData &getTargetData() const;

	bool validateFunction(Function*);

	bool writeCode();

	void destroyBackend();

};

} /* o42a */

#endif
