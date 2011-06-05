/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;


final class ValuePartOp extends ConstructorOp {

	ValuePartOp(HostOp host, ValuePartRef ref) {
		super(host, ref);
	}

	public final boolean isOverridden() {
		return getPartRef().isOverridden();
	}

	public ObjectOp object(CodeDirs dirs) {
		return getRef().op(host()).target(dirs).toObject(dirs);
	}

	@Override
	public void writeLogicalValue(CodeDirs dirs) {
		part().writeLogicalValue(dirs, this);
	}

	@Override
	public ValOp writeValue(ValDirs dirs) {
		return part().writeValue(dirs, this);
	}

	private final ValuePartRef getPartRef() {
		return (ValuePartRef) getRef();
	}

	private final ValuePart part() {
		return getPartRef().getValuePart();
	}

}
