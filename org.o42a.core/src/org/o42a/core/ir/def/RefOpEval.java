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
package org.o42a.core.ir.def;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;


public class RefOpEval extends RefEval {

	public RefOpEval(CodeBuilder builder, Ref ref) {
		super(builder, ref);
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
		getRef().op(host).writeCond(dirs);
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final DefDirs defDirs = dirs.falseWhenUnknown();
		final ValOp value = getRef().op(host).writeValue(defDirs.valDirs());

		defDirs.done();
		dirs.returnValue(value);
	}

}
