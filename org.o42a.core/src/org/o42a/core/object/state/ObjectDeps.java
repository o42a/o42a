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
package org.o42a.core.object.state;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public abstract class ObjectDeps {

	private final Obj object;

	public ObjectDeps(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "Deps[" + this.object + ']';
	}

	protected final Dep newDep(Ref ref, Name name, ID id) {
		return new Dep(getObject(), ref, name, id);
	}

	protected final void reuseDep(Dep dep) {
		dep.reuseDep();
	}

	protected abstract void depResolved(Dep dep);

}
