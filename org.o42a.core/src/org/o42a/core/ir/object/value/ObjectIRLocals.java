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
package org.o42a.core.ir.object.value;

import java.util.ArrayList;

import org.o42a.core.ir.object.ObjectValueIR;


public final class ObjectIRLocals {

	private final ObjectValueIR valueIR;

	public ObjectIRLocals(ObjectValueIR valueIR) {
		this.valueIR = valueIR;
	}

	private ArrayList<LocalIRFunc> locals;
	private boolean filled;

	public final ObjectValueIR getValueIR() {
		return this.valueIR;
	}

	public void addLocal(LocalIRFunc local) {
		if (this.locals == null) {
			this.locals = new ArrayList<LocalIRFunc>();
			this.locals.add(local);
			this.valueIR.getObjectIR().allocate();
		} else {
			this.locals.add(local);
		}
		if (this.filled) {
			local.build();
		}
	}

	public void build() {
		if (this.locals == null) {
			return;
		}
		for (LocalIRFunc local : this.locals) {
			local.build();
		}
	}

}
