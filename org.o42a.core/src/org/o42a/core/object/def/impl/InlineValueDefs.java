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
package org.o42a.core.object.def.impl;

import static org.o42a.util.func.Cancellation.cancelAll;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.InlineValue;


public class InlineValueDefs extends InlineValue {

	private final InlineValue[] inlines;

	public InlineValueDefs(InlineValue[] inlines) {
		super(inlines[0].getValueStruct());
		this.inlines = inlines;
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final ValOp result = dirs.value();
		final Block code = dirs.code();
		final Block[] blocks = new Block[this.inlines.length];

		for (int i = 0; i < blocks.length; ++i) {
			blocks[i] = code.addBlock("val_" + i);
		}
		code.go(blocks[0].head());

		Code firstValueInset = null;
		ValOp firstValue = null;
		int numValues = 0;

		for (int i = 0; i < this.inlines.length; ++i) {

			final int nextIdx = i + 1;
			final Block block = blocks[i];
			final CodePos nextDir =
					nextIdx >= this.inlines.length
					? dirs.unknownDir() : blocks[nextIdx].head();

			final ValDirs defDirs = dirs.getBuilder()
					.splitWhenUnknown(block, dirs.falseDir(), nextDir)
					.value(result);
			final ValOp value = this.inlines[i].writeValue(defDirs, host);

			if (block.exists()) {
				if (numValues == 0) {
					firstValueInset = block.inset("fst_val");
					firstValue = value;
					numValues = 1;
				} else {
					if (numValues == 1) {
						result.store(firstValueInset, firstValue);
						numValues = 2;
					}
					result.store(block, value);
				}
			}

			defDirs.done();

			if (block.exists()) {
				block.go(code.tail());
			}
		}

		if (numValues == 1) {
			return firstValue;
		}

		return result;
	}

	@Override
	public void cancel() {
		cancelAll(this.inlines);
	}

	@Override
	public String toString() {
		if (this.inlines == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		out.append(this.inlines[0]);
		for (int i = 1; i < this.inlines.length; ++i) {
			out.append(". ").append(this.inlines[i]);
		}
		out.append(')');

		return out.toString();
	}

}
