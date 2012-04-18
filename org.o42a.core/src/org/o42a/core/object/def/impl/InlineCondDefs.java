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

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineCond;
import org.o42a.core.object.def.CondDef;
import org.o42a.core.object.def.CondDefs;
import org.o42a.util.fn.Cancelable;


public class InlineCondDefs extends InlineCond {

	private final CondDefs defs;
	private final InlineCond[] inlines;

	public InlineCondDefs(CondDefs defs, InlineCond[] inlines) {
		super(null);
		this.defs = defs;
		this.inlines = inlines;
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {

		final Block code = dirs.code();
		final CondDef[] defs = this.defs.get();
		final Block[] blocks = new Block[defs.length];

		for (int i = 0; i < blocks.length; ++i) {
			blocks[i] = code.addBlock("cond_" + i);
		}
		code.go(blocks[0].head());
		for (int i = 0; i < defs.length; ++i) {

			final int next = i + 1;
			final boolean last = next >= defs.length;
			final Block block = blocks[i];

			if (!defs[i].hasPrerequisite()) {

				final CodeDirs defDirs = dirs.getBuilder().falseWhenUnknown(
						block,
						dirs.falseDir());

				this.inlines[i].writeCond(defDirs, host);
				block.go(last ? code.tail() : blocks[next].head());
				continue;
			}

			final CodeDirs defDirs = dirs.getBuilder().splitWhenUnknown(
					block,
					dirs.falseDir(),
					last ? dirs.unknownDir() : blocks[next].head());

			this.inlines[i].writeCond(defDirs, host);

			final int nextRequired = nextRequired(defs, i);

			block.go(nextRequired < 0 ? code.tail() : blocks[i].head());
		}
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

	@Override
	protected Cancelable cancelable() {
		return null;
	}

	private final int nextRequired(CondDef[] defs, int index) {
		for (int i = index + 1; i < this.inlines.length; ++i) {

			final CondDef def = defs[i];

			if (def.hasPrerequisite()) {
				continue;
			}
			return i;
		}

		return -1;
	}

}
