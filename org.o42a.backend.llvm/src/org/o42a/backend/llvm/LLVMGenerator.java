/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.AbstractGenerator;
import org.o42a.codegen.Analyzer;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public class LLVMGenerator extends AbstractGenerator {

	public static LLVMGenerator newGenerator(
			String id,
			Analyzer analyzer,
			String... args) {
		return new LLVMGenerator(new LLVMModule(id, args), analyzer);
	}

	private final LLVMModule module;

	private LLVMGenerator(LLVMModule module, Analyzer analyzer) {
		super(analyzer);
		this.module = module;
		module.init(this);
		setDebug(module.isDebug());
	}

	public final String getInputFilename() {
		return this.module.getInputFilename();
	}

	public final String getInputEncoding() {
		return this.module.getInputEncoding();
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
	public void write() {
		super.write();
		this.module.write();
	}

	@Override
	public void close() {
		this.module.destroy();
	}

}
