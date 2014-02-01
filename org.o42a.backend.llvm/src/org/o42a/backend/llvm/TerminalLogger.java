/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm;

import static org.o42a.backend.llvm.data.LLVMModule.stderrColumns;

import org.o42a.util.log.ConsoleLogger;
import org.o42a.util.log.LogRecord;


public class TerminalLogger extends ConsoleLogger {

	private boolean autoColumns;

	public TerminalLogger() {
		super(0);
		this.autoColumns = true;
	}

	public TerminalLogger(int columns) {
		super(columns);
	}

	public boolean isAutoColumns() {
		return this.autoColumns;
	}

	public void setAutoColumns(boolean autoColumns) {
		this.autoColumns = autoColumns;
	}

	@Override
	public int getColumns() {
		return super.getColumns();
	}

	@Override
	public void setColumns(int columns) {
		this.autoColumns = false;
		super.setColumns(columns);
	}

	@Override
	public void log(LogRecord record) {
		if (isAutoColumns()) {

			final int stderrColumns = stderrColumns();

			if (stderrColumns > 0) {
				super.setColumns(stderrColumns);
			}
		}

		super.log(record);
	}

	@Override
	public String toString() {
		if (!isAutoColumns()) {
			return super.toString();
		}
		return getClass().getSimpleName() + "[cols: auto]";
	}

}
