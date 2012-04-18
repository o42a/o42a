/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


public class InlineControl extends MainControl {

	private final ValDirs dirs;
	private final DefDirs defDirs;
	private Block returnCode;
	private ValOp finalResult;
	private Code singleResultInset;
	private byte results;

	public InlineControl(ValDirs dirs) {
		super(
				dirs.getBuilder(),
				dirs.code(),
				dirs.unknownDir(),
				dirs.falseDir());
		this.dirs = dirs;
		this.defDirs = null;
	}

	public InlineControl(DefDirs dirs) {
		super(
				dirs.getBuilder(),
				dirs.code(),
				dirs.unknownDir(),
				dirs.falseDir());
		this.dirs = dirs.valDirs();
		this.defDirs = dirs;
	}

	public final ValOp finalResult() {
		return this.finalResult != null ? this.finalResult : result();
	}

	@Override
	public void end() {
		super.end();
		code().go(exit());
		if (this.returnCode != null) {
			if (this.defDirs != null) {
				this.defDirs.returnValue(this.returnCode, finalResult());
			} else {
				this.returnCode.go(code().tail());
			}
		}
	}

	@Override
	final ValOp mainResult() {
		return this.dirs.value();
	}

	@Override
	void storeResult(Block code, ValOp value) {
		if (this.results == 0) {
			this.singleResultInset = code.inset("sgl_res");
			this.finalResult = value;
			this.results = 1;
			return;
		}
		if (this.results == 1) {
			result().store(this.singleResultInset, this.finalResult);
			this.singleResultInset = null;
			this.finalResult = result();
			this.results = 2;
		}
		result().store(code, value);
	}

	@Override
	CodePos returnDir() {
		if (this.returnCode == null) {
			this.returnCode = code().addBlock("return");
		}
		return this.returnCode.head();
	}

}
