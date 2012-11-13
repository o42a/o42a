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

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.link.LinkValueStruct;


public abstract class SingleValueStruct<T>
		extends ValueStruct<SingleValueStruct<T>, T> {

	public SingleValueStruct(SingleValueType<T> valueType) {
		super(valueType);
	}

	@Override
	public SingleValueType<T> getValueType() {
		return (SingleValueType<T>) super.getValueType();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public final TypeParameters getParameters() {
		return null;
	}

	public final boolean is(ValueStruct<?, ?> valueStruct) {
		return this == valueStruct;
	}

	@Override
	public TypeRelation.Kind relationTo(ValueStruct<?, ?> other) {
		if (getValueType() == other.getValueType()) {
			return TypeRelation.Kind.SAME;
		}
		return TypeRelation.Kind.INCOMPATIBLE;
	}

	@Override
	public boolean convertibleFrom(ValueStruct<?, ?> other) {
		return assignableFrom(other);
	}

	@Override
	public final ScopeInfo toScoped() {
		return null;
	}

	@Override
	public final LinkValueStruct toLinkStruct() {
		return null;
	}

	@Override
	public final ArrayValueStruct toArrayStruct() {
		return null;
	}

	@Override
	public final SingleValueStruct<T> prefixWith(PrefixPath prefix) {
		return this;
	}

	@Override
	public final SingleValueStruct<T> upgradeScope(Scope toScope) {
		return this;
	}

	@Override
	public final SingleValueStruct<T> rebuildIn(Scope scope) {
		return this;
	}

	@Override
	public SingleValueStruct<T> reproduce(Reproducer reproducer) {
		return this;
	}

	@Override
	public void resolveAll(FullResolver resolver) {
	}

	@Override
	public String toString() {

		final SingleValueType<T> valueType = getValueType();

		if (valueType == null) {
			return super.toString();
		}

		return valueType.toString();
	}

}
