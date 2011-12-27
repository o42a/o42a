/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.def.impl;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.def.InlineValue;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


public class InlineValueDefs extends InlineValue {

	private final InlineValue[] inlines;

	public InlineValueDefs(InlineValue[] inlines) {
		super(inlines[0].getValueStruct());
		this.inlines = inlines;
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final ValOp result = dirs.value();
		final Code code = dirs.code();
		final Code[] blocks = new Code[this.inlines.length];

		for (int i = 0; i < blocks.length; ++i) {
			blocks[i] = code.addBlock("val_" + i);
		}
		code.go(blocks[0].head());
		for (int i = 0; i < this.inlines.length; ++i) {

			final int nextIdx = i + 1;
			final Code block = blocks[i];
			final CodePos nextDir =
					nextIdx >= this.inlines.length
					? dirs.unknownDir() : blocks[nextIdx].head();

			final ValDirs defDirs = dirs.getBuilder()
					.splitWhenUnknown(block, dirs.falseDir(), nextDir)
					.value(block.id("val"), result);

			result.store(block, this.inlines[i].writeValue(defDirs, host));

			defDirs.done();

			block.go(code.tail());
		}

		return result;
	}

}
