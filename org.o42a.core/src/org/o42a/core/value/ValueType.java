/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayValueType;
import org.o42a.core.object.directive.Directive;
import org.o42a.core.object.directive.impl.DirectiveValueType;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.object.macro.Macro;
import org.o42a.core.object.macro.impl.MacroValueType;
import org.o42a.core.ref.path.Path;
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
	public static final SingleValueType<Macro> MACRO =
			MacroValueType.INSTANCE;

	public static final SingleValueType<java.lang.Void> NONE =
			NoneValueType.INSTANCE;

	private final String systemId;

	public ValueType(String systemId) {
		this.systemId = systemId;
	}

	public final String getSystemId() {
		return this.systemId;
	}

	public final boolean isVoid() {
		return is(VOID);
	}

	public final boolean isNone() {
		return is(NONE);
	}

	public final boolean isMacro() {
		return is(MACRO);
	}

	public final boolean isLink() {
		return toLinkType() != null;
	}

	public final boolean isArray() {
		return toArrayType() != null;
	}

	public abstract boolean isVariable();

	public abstract boolean isStateless();

	public abstract boolean isRuntimeConstructed();

	public final boolean is(ValueType<?> valueType) {
		return this == valueType;
	}

	public abstract Obj typeObject(Intrinsics intrinsics);

	public abstract Path path(Intrinsics intrinsics);

	public final StaticTypeRef typeRef(LocationInfo location, Scope scope) {
		return typeRef(location, scope, null);
	}

	public StaticTypeRef typeRef(
			LocationInfo location,
			Scope scope,
			ValueStructFinder valueStructFinder) {
		return path(location.getContext().getIntrinsics())
				.bind(location, scope)
				.staticTypeRef(scope.distribute(),
				valueStructFinder);
	}

	public abstract LinkValueType toLinkType();

	public abstract ArrayValueType toArrayType();

	@Override
	public String toString() {
		return getSystemId();
	}

}
