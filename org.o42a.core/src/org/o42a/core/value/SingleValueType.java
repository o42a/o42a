/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.impl.Constant;
import org.o42a.core.value.link.LinkValueType;


public abstract class SingleValueType<T>
		extends ValueType<SingleValueStruct<T>, T> {

	public SingleValueType(
			String systemId,
			Class<? extends T> valueClass) {
		super(systemId, valueClass);
	}

	@Override
	public boolean isStateful() {
		return false;
	}

	@Override
	public final boolean isVariable() {
		return false;
	}

	@Override
	public boolean convertibleFrom(ValueType<?, ?> other) {
		return is(other);
	}

	public abstract SingleValueStruct<T> struct();

	public final Value<T> constantValue(T value) {
		return struct().compilerValue(value);
	}

	public final Value<T> runtimeValue() {
		return struct().runtimeValue();
	}

	public final Value<T> falseValue() {
		return struct().falseValue();
	}

	public final Value<T> compilerValue(T value) {
		return struct().compilerValue(value);
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

	public final Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope) {
		return struct().noValueDefinitions(location, scope);
	}

	@Override
	public final LinkValueType toLinkType() {
		return null;
	}

	@Override
	public final ArrayValueType toArrayType() {
		return null;
	}

	@Override
	protected final ValueKnowledge valueKnowledge(T value) {
		return ValueKnowledge.KNOWN_VALUE;
	}

	@Override
	protected Value<T> prefixValueWith(Value<T> value, PrefixPath prefix) {
		return value;
	}

	@Override
	protected void resolveAll(Value<T> value, FullResolver resolver) {
	}

}
