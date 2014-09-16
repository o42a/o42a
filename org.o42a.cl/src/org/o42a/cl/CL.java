/*
    Compiler Command-Line Interface
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
package org.o42a.cl;

import static org.o42a.backend.llvm.LLVMGenerator.newGenerator;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;

import java.io.FileNotFoundException;

import org.o42a.analysis.Analyzer;
import org.o42a.backend.constant.ConstGenerator;
import org.o42a.backend.llvm.LLVMGenerator;
import org.o42a.codegen.Generator;
import org.o42a.common.source.FileSourceTree;
import org.o42a.compiler.Compiler;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.ArrayUtil;


public class CL {

	public static final int COMPILE_ERROR = 1;
	public static final int COMPILATION_FAILED = 2;
	public static final int INVALID_INPUT = 3;

	private final Generator generator;

	private CL(Generator generator) {
		this.generator = generator;
	}

	public void compile(FileSourceTree sourceTree) {

		final Compiler compiler = compiler();
		final CLLogger logger = new CLLogger();
		final CompilerIntrinsics intrinsics = intrinsics(compiler, logger);
		final CompilerContext rootContext = intrinsics.getRoot().getContext();
		final CompilerContext context = sourceTree.context(rootContext);
		final Module module = new Module(context, null);

		intrinsics.setMainModule(module);
		intrinsics.resolveAll(this.generator.getAnalyzer(), logger);

		assert context.fullResolution().isComplete() :
			"Full resolution is incomplete";

		logger.abortOnError();

		intrinsics.generateAll(this.generator);

		this.generator.write();
	}

	public static void main(String[] args) {

		final String[] llvmArgs = ArrayUtil.prepend("o42ac", args);
		final Analyzer analyzer = new Analyzer("compiler");
		final LLVMGenerator llvmGenerator =
				newGenerator(null, analyzer, llvmArgs);
		final FileSourceTree source = createSource(llvmGenerator);
		final Generator generator = new ConstGenerator(llvmGenerator);

		try {

			final CL instance = new CL(generator);

			try {
				instance.compile(source);
			} catch (Throwable e) {
				e.printStackTrace();
				System.exit(COMPILATION_FAILED);
				return;
			}
		} finally {
			generator.close();
		}
	}

	private static FileSourceTree createSource(LLVMGenerator llvmGenerator) {
		try {
			return new FileSourceTree(llvmGenerator.createSource());
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.exit(INVALID_INPUT);
			return null;
		}
	}

}
