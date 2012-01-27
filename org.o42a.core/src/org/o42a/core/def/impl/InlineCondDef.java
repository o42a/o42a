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
package org.o42a.core.def.impl;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.InlineCond;


public class InlineCondDef extends InlineCond {

	private final InlineCond prerequisite;
	private final InlineCond precondition;
	private final InlineCond def;

	public InlineCondDef(
			InlineCond prerequisite,
			InlineCond precondition,
			InlineCond def) {
		this.prerequisite = prerequisite;
		this.precondition = precondition;
		this.def = def;
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
		if (this.prerequisite != null) {
			this.prerequisite.writeCond(dirs.unknownWhenFalse(), host);
		}
		this.precondition.writeCond(dirs, host);
		this.def.writeCond(dirs, host);
	}

	@Override
	public void cancel() {
		if (this.prerequisite != null) {
			this.prerequisite.cancel();
		}
		this.precondition.cancel();
		this.def.cancel();
	}

	@Override
	public String toString() {
		if (this.def == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		if (this.prerequisite != null) {
			out.append(this.prerequisite).append("? ");
		}
		out.append(this.precondition);
		out.append(", ");
		out.append(this.def);
		out.append(')');

		return out.toString();
	}

}
