/*
    Compiler LLVM Back-end Tests
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
package org.o42a.backend.llvm.test;

import static org.o42a.backend.llvm.LLVMGenerator.newGenerator;

import java.util.ArrayList;

import org.o42a.analysis.Analyzer;
import org.o42a.backend.constant.ConstGenerator;
import org.o42a.backend.llvm.LLVMGenerator;
import org.o42a.codegen.Generator;
import org.o42a.compiler.test.CompilerTestCase;


public class GeneratorTestCase extends CompilerTestCase {

	private Generator generator;

	@Override
	protected void compile(String line, String... lines) {
		super.compile(line, lines);
		try {
			generateCode(this.generator);
			this.generator.write();
		} finally {
			this.generator.close();
		}
	}

	@Override
	protected Analyzer createAnalyzer() {

		final Analyzer analyzer = super.createAnalyzer();
		final LLVMGenerator llvmGenerator = newGenerator(
				"test",
				analyzer,
				parseArgs(System.getProperty("llvm.args", "")));

		this.generator = new ConstGenerator(llvmGenerator);

		return analyzer;
	}

	private String[] parseArgs(String args) {

		final ArrayList<String> commandLine = new ArrayList<String>();

		commandLine.add("test");

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
					commandLine.add(arg.toString());
					arg.setLength(0);
				}
				continue;
			default:
				arg.append(c);
			}
		}

		if (arg.length() != 0) {
			commandLine.add(arg.toString());
		}

		return commandLine.toArray(new String[commandLine.size()]);
	}

}
