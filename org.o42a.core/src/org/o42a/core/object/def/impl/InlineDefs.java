/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.util.fn.Cancelable;


public class InlineDefs extends InlineEval {

	private final InlineEval[] defs;

	public InlineDefs(InlineEval[] defs) {
		super(null);
		this.defs = defs;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {
		for (InlineEval def : this.defs) {
			def.write(dirs, host);
			if (!dirs.code().exists()) {
				break;
			}
		}
	}

	@Override
	public String toString() {
		if (this.defs == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		out.append(this.defs[0]);
		for (int i = 1; i < this.defs.length; ++i) {
			out.append(". ").append(this.defs[i]);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
