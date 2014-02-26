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
package org.o42a.core.object.value;

import org.o42a.analysis.use.Usable;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;


public abstract class ObjectValueBase {

	private final Obj object;
	private final ObjectValueDefs valueDefs;

	public ObjectValueBase(Obj object) {
		this.object = object;

		final ObjectValue objectValue = (ObjectValue) this;

		this.valueDefs = new ObjectValueDefs(objectValue);
	}

	public final Obj getObject() {
		return this.object;
	}

	public final ObjectValueDefs valueDefs() {
		return this.valueDefs;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectValue[" + this.object + ']';
	}

	protected abstract Usable<ValueUsage> uses();

}