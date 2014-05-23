/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.value.Statefulness;


public abstract class ConstructedObject extends Obj {

	private final ObjectConstructor constructor;

	public ConstructedObject(
			ObjectConstructor constructor,
			Distributor enclosing) {
		super(constructor, enclosing);
		this.constructor = constructor;
	}

	public final ObjectConstructor getConstructor() {
		return this.constructor;
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected final Nesting createNesting() {
		return getConstructor().getNesting();
	}

	@Override
	protected Statefulness determineStatefulness() {
		return super.determineStatefulness()
				.setStateful(this.constructor.isStateful())
				.setEager(this.constructor.isEager());
	}

}
