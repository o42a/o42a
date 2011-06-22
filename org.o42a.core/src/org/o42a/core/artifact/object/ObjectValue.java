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

import static org.o42a.util.use.Usable.simpleUsable;

import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.util.use.*;


public final class ObjectValue extends AbstractUser {

	private final Obj object;
	private Usable<?> usable;
	private Value<?> value;

	ObjectValue(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	@Override
	public final UseFlag getUseBy(UseCase useCase) {
		return usable().getUseBy(useCase);
	}

	public final Value<?> getValue() {
		if (this.value == null) {
			this.value = getObject().calculateValue(
					getObject().getScope().dummyResolver());
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

	final Usable<?> usable() {
		if (this.usable != null) {
			return this.usable;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf == null) {
			return this.usable = simpleUsable("ObjectValue", getObject());
		}

		return this.usable = cloneOf.value(dummyUser()).usable();
	}

}
