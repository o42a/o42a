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

import java.util.ArrayList;

import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public class LLVMGenerator extends Generator {

	public static LLVMGenerator newGenerator(String toolName, String id) {
		return new LLVMGenerator(toolName, id);
	}

	private final ArrayList<String> commandLine = new ArrayList<String>();

	private LLVMModule module;

	private LLVMGenerator(String toolName, String id) {
		super(id);
		this.commandLine.add(toolName);
	}

	public void addArg(String arg) {
		this.commandLine.add(arg);
	}

	public void addValue(String name, String value) {
		if (value == null) {
			addArg(name);
		} else {
			this.commandLine.add(name + "=" + value);
		}
	}

	public void addArgs(String... args) {
		this.commandLine.ensureCapacity(this.commandLine.size() + args.length);
		for (String arg : args) {
			this.commandLine.add(arg);
		}
	}

	public void addArgs(String args) {

		boolean escaped = false;
		char quote = 0;

		final StringBuilder arg = new StringBuilder();

		for (int i = 0, len = args.length(); i < len; ++i) {

			final char c = args.charAt(i);

			if (escaped) {
				switch (c) {
				case 'n':
					arg.append('\n');
					break;
				case 'r':
					arg.append('\r');
					break;
				case 't':
					arg.append('\t');
					break;
				default:
					arg.append(c);
				}
				escaped = false;
				continue;
			}
			if (c == '\\') {
				escaped = true;
				continue;
			}
			if (quote != 0) {
				if (c == quote) {
					quote = 0;
					continue;
				}
				arg.append(c);
				continue;
			}
			switch (c) {
			case '\'':
			case '\"':
				quote = c;
				continue;
			case ' ':
			case '\r':
			case '\n':
			case '\t':
				if (arg.length() != 0) {
					this.commandLine.add(arg.toString());
					arg.setLength(0);
				}
				continue;
			default:
				arg.append(c);
			}
		}

		if (arg.length() != 0) {
			this.commandLine.add(arg.toString());
		}
	}

	@Override
	public void write() {

		final LLVMModule module = module();

		try {
			super.write();
			module.write();
		} finally {
			module.destroy();
		}
	}

	@Override
	public DataAllocator dataAllocator() {
		return module().dataAllocator();
	}

	@Override
	public DataWriter dataWriter() {
		return module().dataWriter();
	}

	@Override
	public CodeBackend codeBackend() {
		return module().codeBackend();
	}

	private LLVMModule module() {
		if (this.module != null) {
			return this.module;
		}
		return this.module = new LLVMModule(
				this,
				this.commandLine.toArray(new String[this.commandLine.size()]));
	}

}
