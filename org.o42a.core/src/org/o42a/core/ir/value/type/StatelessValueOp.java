/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.value.type;

import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;


public abstract class StatelessValueOp extends ValueOp {

	public StatelessValueOp(ValueIR valueIR, ObjectOp object) {
		super(valueIR, object);
		assert valueIR.getValueType().getDefaultStatefulness().isStateless() :
			valueIR.getValueType() + " is stateful";
	}

	@Override
	public final StateOp state(CodeDirs dirs) {
		throw new UnsupportedOperationException(
				"Value of type " + getValueType() + " has no state");
	}

}
