/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.value.directive.impl;

import static org.o42a.core.ir.value.type.ValueIRDesc.VOID_VALUE_IR_DESC;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.directive.Directive;


public final class DirectiveValueType extends SingleValueType<Directive> {

	public static final DirectiveValueType INSTANCE = new DirectiveValueType();

	private DirectiveValueType() {
		super("directive", Directive.class);
	}

	@Override
	public Obj typeObject(Intrinsics intrinsics) {
		return intrinsics.getDirective();
	}

	@Override
	public Path path(Intrinsics intrinsics) {
		return Path.ROOT_PATH.append(
				typeObject(intrinsics).getScope().toField().getKey());
	}

	@Override
	public ValueIRDesc irDesc() {
		return VOID_VALUE_IR_DESC;
	}

	@Override
	protected ValueTypeIR<Directive> createIR(Generator generator) {
		return new DirectiveValueTypeIR(generator, this);
	}

}
