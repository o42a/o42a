/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public class LLVMGenerator extends Generator {

	public static LLVMGenerator newGenerator(
			String id,
			String... args) {
		return new LLVMGenerator(new LLVMModule(id, args));
	}

	private final LLVMModule module;

	private LLVMGenerator(LLVMModule module) {
		super(module.getId());
		this.module = module;
		module.init(this);
		setDebug(module.isDebug());
	}

	public final String getInputFilename() {
		return this.module.getInputFilename();
	}

	@Override
	public DataAllocator dataAllocator() {
		return this.module.dataAllocator();
	}

	@Override
	public DataWriter dataWriter() {
		return this.module.dataWriter();
	}

	@Override
	public CodeBackend codeBackend() {
		return this.module.codeBackend();
	}

	@Override
	public void close() {
		this.module.destroy();
	}

	@Override
	protected void writeData() {
		super.writeData();
		this.module.write();
	}
}
