/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.impl.*;


public abstract class ValueType<S extends ValueStruct<?, ?>> {

	public static final SingleValueType<Void> VOID =
			VoidValueType.INSTANCE;
	public static final SingleValueType<Long> INTEGER =
			IntegerValueType.INSTANCE;
	public static final SingleValueType<Double> FLOAT =
			FloatValueType.INSTANCE;
	public static final SingleValueType<String> STRING =
			StringValueType.INSTANCE;
	public static final SingleValueType<Directive> DIRECTIVE =
			DirectiveValueType.INSTANCE;

	public static final SingleValueType<java.lang.Void> NONE =
			NoneValueType.INSTANCE;

	private final String systemId;

	ValueType(String systemId) {
		this.systemId = systemId;
	}

	public final String getSystemId() {
		return this.systemId;
	}

	public final boolean isVoid() {
		return this == VOID;
	}

	public final boolean isNone() {
		return this == NONE;
	}

	public abstract Obj wrapper(Intrinsics intrinsics);

	public StaticTypeRef typeRef(LocationInfo location, Scope scope) {

		final Distributor distributor = scope.distribute();
		@SuppressWarnings("unchecked")
		final Field<Obj> wrapperField =
				(Field<Obj>) wrapper(location.getContext().getIntrinsics())
				.getScope().toField();

		return ROOT_PATH.append(wrapperField.getKey())
				.target(location, distributor)
				.toStaticTypeRef();
	}

	@Override
	public String toString() {
		return getSystemId();
	}

}
