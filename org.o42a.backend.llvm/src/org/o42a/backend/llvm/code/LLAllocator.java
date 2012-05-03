/*
    Compiler LLVM Back-end
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.backend.llvm.code;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.backend.AllocatorWriter;


public abstract class LLAllocator extends LLBlock implements AllocatorWriter {

	public LLAllocator(
			LLVMModule module,
			LLFunction<?> function,
			Allocator allocator) {
		super(module, function, allocator);
	}

	@Override
	public AllocationWriter init(AllocationCode code) {

		final LLAllocation allocation = new LLAllocation(this, code);

		addInset(allocation);

		return allocation;
	}

}
