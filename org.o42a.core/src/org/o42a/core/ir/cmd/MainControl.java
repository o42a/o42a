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
package org.o42a.core.ir.cmd;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


final class MainControl extends Control {

	private final DefDirs dirs;
	private final Block continuation;

	private int seq;
	private Block returnCode;
	private ValOp finalResult;
	private Code singleResultInset;
	private byte results;

	MainControl(DefDirs dirs, Block continuation) {
		this.dirs = dirs;
		this.continuation = continuation;
	}

	@Override
	public final LocalsCode locals() {
		return builder().locals();
	}

	@Override
	public final Block code() {
		return this.dirs.code();
	}

	@Override
	public Code allocation() {
		throw new UnsupportedOperationException(
				"Main control does not support stack allocations");
	}

	@Override
	public final CodePos exit() {
		return this.continuation.head();
	}

	@Override
	public final CodePos falseDir() {
		return this.dirs.falseDir();
	}

	public final ValOp finalResult() {
		return this.finalResult != null ? this.finalResult : result();
	}

	@Override
	public void end() {
		if (this.continuation.exists()) {
			this.continuation.go(code().tail());
		}
		if (this.returnCode != null) {
			this.dirs.returnValue(this.returnCode, finalResult());
		}
	}

	final CodeBuilder builder() {
		return this.dirs.getBuilder();
	}

	final ValOp mainResult() {
		return this.dirs.value();
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
	CodePos returnDir() {
		if (this.returnCode == null) {
			this.returnCode = code().addBlock("return");
		}
		return this.returnCode.head();
	}

	final ID anonymousName() {
		return ID.id(Integer.toString(++this.seq));
	}

	void storeResult(Block code, ValOp value) {
		if (this.results == 0 && valueAccessibleBy(code)) {
			this.singleResultInset = code.inset("sgl_res");
			this.finalResult = value;
			this.results = 1;
			return;
		}
		if (this.results == 1) {
			if (this.singleResultInset != null) {
				result().store(this.singleResultInset, this.finalResult);
				this.singleResultInset = null;
			}
			this.results = 2;
			this.finalResult = result();
		}
		result().store(code, value);
	}

	private boolean valueAccessibleBy(Code code) {
		if (!this.dirs.value().isStackAllocated()) {
			return true;
		}
		return code.getAllocator() == this.dirs.code().getAllocator();
	}

}
