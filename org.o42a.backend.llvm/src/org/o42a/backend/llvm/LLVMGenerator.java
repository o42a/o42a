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

import java.io.File;
import java.io.FileNotFoundException;

import org.o42a.analysis.Analyzer;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.AbstractGenerator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.io.FileSource;
import org.o42a.util.io.SourceFileName;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public class LLVMGenerator extends AbstractGenerator {

	private static final ID MODULE_ID = ID.id("module");

	public static LLVMGenerator newGenerator(
			String id,
			Analyzer analyzer,
			String... args) {
		return new LLVMGenerator(id, new LLVMModule(args), analyzer);
	}

	private final String id;
	private final LLVMModule module;

	private LLVMGenerator(String id, LLVMModule module, Analyzer analyzer) {
		super(analyzer);
		this.id = id;
		this.module = module;
		module.init(this);
	}

	public FileSource createSource() throws FileNotFoundException {

		final String path = getInputFilename();

		if (path == null) {
			throw new FileNotFoundException("Input file not specified");
		}

		final FileSource source = sourceByPath(path);

		source.setEncoding(getInputEncoding());
		this.module.createModule(moduleId(source));

		return source;
	}

	public final String getInputFilename() {
		return this.module.getInputFilename();
	}

	public final String getInputEncoding() {
		return this.module.getInputEncoding();
	}

	@Override
	public DataAllocator dataAllocator() {
		return initializedModule().dataAllocator();
	}

	@Override
	public DataWriter dataWriter() {
		return initializedModule().dataWriter();
	}

	@Override
	public CodeBackend codeBackend() {
		return initializedModule().codeBackend();
	}

	@Override
	public void write() {
		super.write();
		initializedModule().write();
	}

	@Override
	public void close() {
		this.module.destroy();
	}

	private LLVMModule initializedModule() {
		if (this.module.getNativePtr() == 0L) {
			this.module.createModule(
					this.id != null ? ID.id(this.id) : MODULE_ID);
		}
		return this.module;
	}

	private static FileSource sourceByPath(
			String path)
	throws FileNotFoundException {

		final File file = new File(path);

		if (!file.isFile()) {
			throw new FileNotFoundException("No such file: " + path);
		}

		return new FileSource(file.getParentFile(), file.getName());
	}

	private ID moduleId(FileSource source) {
		if (this.id != null) {
			return ID.id(this.id);
		}

		final Name name =
				new SourceFileName(source.getName()).getFieldName();

		if (name != null) {
			return name.toID();
		}

		return MODULE_ID;
	}

}
