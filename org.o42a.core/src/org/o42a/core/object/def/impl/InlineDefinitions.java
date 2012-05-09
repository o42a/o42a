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

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public class InlineDefinitions extends InlineValue {

	private final ValueStruct<?, ?> valueStruct;
	private final InlineEval claim;
	private final InlineEval proposition;

	public InlineDefinitions(
			ValueStruct<?, ?> valueStruct,
			InlineEval claim,
			InlineEval proposition) {
		super(null);
		this.valueStruct = valueStruct;
		this.claim = claim;
		this.proposition = proposition;
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {

		final DefDirs defDirs = dirs.nested().value(this.valueStruct).def();

		this.claim.write(defDirs, host);
		this.proposition.write(defDirs, host);
		defDirs.done();
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final DefDirs defDirs = dirs.nested().def();

		this.claim.write(defDirs, host);
		this.proposition.write(defDirs, host);
		defDirs.done();

		return defDirs.result();
	}

	@Override
	public String toString() {
		if (this.proposition == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		out.append(this.claim).append("! ");
		out.append(this.proposition);
		out.append(')');

		return out.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
