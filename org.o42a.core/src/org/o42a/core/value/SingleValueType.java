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
package org.o42a.core.value;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.impl.Constant;


public abstract class SingleValueType<T>
		extends ValueType<SingleValueStruct<T>> {

	public SingleValueType(String systemId) {
		super(systemId);
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	public abstract SingleValueStruct<T> struct();

	public final Value<T> constantValue(T value) {
		return struct().constantValue(value);
	}

	public final Value<T> runtimeValue() {
		return struct().runtimeValue();
	}

	public final Value<T> falseValue() {
		return struct().falseValue();
	}

	public final Value<T> unknownValue() {
		return struct().unknownValue();
	}

	public final Ref constantRef(
			LocationInfo location,
			Distributor distributor,
			T value) {

		final Constant<T> constant =
				new Constant<T>(location, distributor, this, value);
		final BoundPath path = constant.toPath().bindStatically(
				location,
				distributor.getScope());

		return path.target(distributor);
	}

	public final ValueDef constantDef(
			Obj source,
			LocationInfo location,
			T value) {
		return struct().constantDef(source, location, value);
	}

	public final Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope) {
		return struct().noValueDefinitions(location, scope);
	}

	public final T cast(Object value) {
		return struct().cast(value);
	}

	public final Value<T> cast(Value<?> value) {
		return struct().cast(value);
	}

}
