/*
    Compiler
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.NewObjectOp;
import org.o42a.core.ir.op.ValOp;


final class ValuePartOp extends NewObjectOp {

	ValuePartOp(HostOp host, ValuePartRef ref) {
		super(host, ref);
	}

	public final boolean isOverridden() {
		return getPartRef().isOverridden();
	}

	public ObjectOp object(Code code, CodePos exit) {
		return getRef().op(host()).target(code, exit).toObject(code, exit);
	}

	@Override
	public void writeCondition(Code code, CodePos exit) {
		part().writeCondition(code, exit, this);
	}

	@Override
	public void writeValue(Code code, CodePos exit, ValOp result) {
		part().writeValue(code, exit, result, this);
	}

	private final ValuePartRef getPartRef() {
		return (ValuePartRef) getRef();
	}

	private final ValuePart part() {
		return getPartRef().getValuePart();
	}

}
