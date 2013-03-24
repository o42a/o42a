/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.ir.local;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


abstract class MainControl extends Control {

	private final CodeBuilder builder;
	private final Block code;
	private final CodePos exit;
	private final CodePos falseDir;
	private int seq;

	MainControl(
			CodeBuilder builder,
			Block code,
			CodePos exit,
			CodePos falseDir) {
		this.code = code;
		this.builder = builder;
		this.exit = exit;
		this.falseDir = falseDir;
	}

	@Override
	public final LocalsCode locals() {
		return this.builder.locals();
	}

	@Override
	public final Block code() {
		return this.code;
	}

	@Override
	public Code allocation() {
		throw new UnsupportedOperationException(
				"Main control does not support stack allocations");
	}

	@Override
	public final CodePos exit() {
		return this.exit;
	}

	@Override
	public final CodePos falseDir() {
		return this.falseDir;
	}

	@Override
	public void end() {
	}

	final CodeBuilder builder() {
		return this.builder;
	}

	abstract ValOp mainResult();

	@Override
	final MainControl main() {
		return this;
	}

	@Override
	final BracesControl braces() {
		return null;
	}

	final ID anonymousName() {
		return ID.id(Integer.toString(++this.seq));
	}

	abstract void storeResult(Block code, ValOp value);

}
