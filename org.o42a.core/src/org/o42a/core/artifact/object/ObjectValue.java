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
package org.o42a.core.artifact.object;

import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.util.use.User;


public final class ObjectValue {

	private final Obj object;
	private Value<?> value;

	private ObjectValue(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Value<?> getValue() {
		if (this.value == null) {
			this.value = getObject().calculateValue(
					getObject().getScope().newResolver());
		}
		return this.value;
	}

	public Value<?> value(Resolver resolver) {
		getObject().assertCompatible(resolver.getScope());

		final Value<?> result;

		if (resolver == getObject().getScope()) {
			result = getValue();
		} else {
			result = getObject().calculateValue(resolver);
		}

		return result;
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ValueOf[" + this.object + ']';
	}

	static final class UseableObjectValue extends ObjectUsable<ObjectValue> {

		private final ObjectValue value;

		UseableObjectValue(Obj object) {
			super(object);
			this.value = new ObjectValue(object);
		}

		@Override
		protected ObjectValue createUsed(User user) {
			return this.value;
		}

		final ObjectValue getValue() {
			return this.value;
		}

	}
}
