/*
    Compiler Command-Line Interface
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
package org.o42a.cl;

import static org.o42a.backend.llvm.LLVMGenerator.newGenerator;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;

import java.io.IOException;
import java.net.*;

import org.o42a.backend.llvm.LLVMGenerator;
import org.o42a.codegen.Generator;
import org.o42a.common.source.URLContext;
import org.o42a.compiler.Compiler;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.ArrayUtil;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


public class CL {

	private static final int COMPILE_ERROR = 1;
	private static final int COMPILATION_FAILED = 2;
	private static final int INVALID_INPUT = 3;

	private final Generator generator;

	private CL(Generator generator) {
		this.generator = generator;
	}

	public void compile(String source) throws IOException {

		final Compiler compiler = compiler();
		final CompilerIntrinsics intrinsics = intrinsics(compiler);
		final URL src = url(source);
		final CompilerContext rootContext =
				intrinsics.getRoot().getContext();
		final Log logger = new Log();
		final String fileName = fileName(src);
		final CompilerContext context =
				new URLContext(rootContext, fileName, src, fileName, logger);

		final Module module = new Module(context, moduleName(src.getPath()));

		intrinsics.setMainModule(module);
		intrinsics.resolveAll();

		assert context.fullResolution().isComplete() :
			"Full resolution is incomplete";

		logger.abortOnError();

		intrinsics.generateAll(this.generator);

		this.generator.write();
	}

	public static void main(String[] args) {

		final String[] llvmArgs = ArrayUtil.prepend("o42ac", args);
		final LLVMGenerator generator = newGenerator(null, llvmArgs);

		try {

			final String source = generator.getInputFilename();

			if (source == null) {
				System.err.println("Input file not specified");
				System.exit(INVALID_INPUT);
				return;
			}

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

	private static URL url(String path) {

		final URI currentDir;
		final URI sourceURI;

		try {
			currentDir = new URI(
					"file",
					System.getProperty("user.dir") + "/",
					null);
			sourceURI = new URI(path);
		} catch (URISyntaxException e) {
			System.err.println("Wrong file path: " + path);
			System.exit(INVALID_INPUT);
			return null;
		}

		try {
			return currentDir.resolve(sourceURI).toURL();
		} catch (MalformedURLException e) {
			System.err.println("Wrong file path: " + path);
			System.exit(INVALID_INPUT);
			return null;
		}
	}

	private static String fileName(URL url) {

		final String path = url.getPath();
		final int slashIdx = path.lastIndexOf('/');

		if (slashIdx < 0) {
			return path;
		}

		return path.substring(slashIdx + 1);
	}

	private static String moduleName(String source) {

		final int slashIdx =
			source.lastIndexOf(System.getProperty("file.separator"));
		final String fileName;

		if (slashIdx >= 0) {
			fileName = source.substring(slashIdx + 1);
		} else {
			fileName = source;
		}

		final int dotIdx = fileName.lastIndexOf('.');

		if (dotIdx > 0) {
			return fileName.substring(0, dotIdx);
		}

		return fileName;
	}

	private static final class Log implements Logger {

		private boolean hasErrors;
		private boolean abortOnError;

		@Override
		public void log(LogRecord record) {
			DEFAULT_LOGGER.log(record);
			if (record.getSeverity().isError()) {
				if (this.abortOnError) {
					System.exit(COMPILE_ERROR);
				}
				this.hasErrors = true;
			}
		}

		public void abortOnError() {
			if (this.hasErrors) {
				System.exit(COMPILE_ERROR);
			}
			this.abortOnError = true;
		}
	}

}
