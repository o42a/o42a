/*
    Compiler Command-Line Interface
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.cl.CL.COMPILE_ERROR;

import org.o42a.backend.llvm.TerminalLogger;
import org.o42a.intrinsic.CompileErrors;
import org.o42a.util.log.LogRecord;


final class CLLogger extends TerminalLogger implements CompileErrors {

	private boolean hasErrors;
	private boolean abortOnError;

	@Override
	public void log(LogRecord record) {
		super.log(record);
		if (record.getSeverity().isError()) {
			if (this.abortOnError) {
				System.exit(COMPILE_ERROR);
			}
			this.hasErrors = true;
		}
	}

	@Override
	public boolean hasCompileErrors() {
		return this.hasErrors;
	}

	public void abortOnError() {
		if (this.hasErrors) {
			System.exit(COMPILE_ERROR);
		}
		this.abortOnError = true;
	}

}
