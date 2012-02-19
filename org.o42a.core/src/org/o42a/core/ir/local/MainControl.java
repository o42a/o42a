/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValOp;


abstract class MainControl extends Control {

	private final CodeBuilder builder;
	private final Block code;
	private final CodePos exit;
	private final CodePos falseDir;
	private final ValOp result;
	private int seq;

	MainControl(
			CodeBuilder builder,
			Block code,
			CodePos exit,
			CodePos falseDir,
			ValOp result) {
		this.code = code;
		this.builder = builder;
		this.exit = exit;
		this.falseDir = falseDir;
		this.result = result;
	}

	@Override
	public final Block code() {
		return this.code;
	}

	@Override
	public AllocationCode allocation() {
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

	final ValOp mainResult() {
		return this.result;
	}

	@Override
	final MainControl main() {
		return this;
	}

	@Override
	final BracesControl braces() {
		return null;
	}

	@Override
	final CodePos exitDir(BracesControl braces) {
		throw new UnsupportedOperationException(
				"Main control does not support loops");
	}

	@Override
	final CodePos repeatDir(BracesControl braces) {
		throw new UnsupportedOperationException(
				"Main control does not support loops");
	}

	final String anonymousName() {
		return Integer.toString(++this.seq);
	}

}
