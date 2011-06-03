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

import static org.o42a.core.ir.op.Val.VOID_VAL;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Intrinsics;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.op.Val;
import org.o42a.core.ref.type.StaticTypeRef;


final class VoidValueType extends ValueType<Void> {

	VoidValueType() {
		super("void", Void.class);
	}

	@Override
	public Obj wrapper(Intrinsics intrinsics) {
		return intrinsics.getVoid();
	}

	@Override
	public StaticTypeRef typeRef(LocationInfo location, Scope scope) {
		return voidRef(location, scope.distribute()).toStaticTypeRef();
	}

	@Override
	protected Val val(Generator generator, Void value) {
		return VOID_VAL;
	}

	@Override
	protected CodeId constId(Generator generator) {
		return generator.id("CONST").sub("VOID");
	}

}
