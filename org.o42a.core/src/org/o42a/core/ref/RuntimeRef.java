/*
    Compiler Core
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
package org.o42a.core.ref;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Result;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.common.NewObjectEx;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class RuntimeRef extends NewObjectEx {

	RuntimeRef(LocationSpec location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new RuntimeRef(this, reproducer.distribute());
	}

	@Override
	protected Obj createObject() {
		return new RuntimeObject(this, distribute());
	}

	private static final class RuntimeObject extends Result {

		RuntimeObject(LocationSpec location, Distributor enclosing) {
			super(location, enclosing, ValueType.VOID);
		}

		@Override
		protected Value<?> calculateValue(Scope scope) {
			return ValueType.VOID.runtimeValue();
		}

	}

}
