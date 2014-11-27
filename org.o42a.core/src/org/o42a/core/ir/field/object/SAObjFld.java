/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.object;

import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld.StatelessOp;
import org.o42a.core.ir.field.RefFld.StatelessType;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.member.field.Field;


public class SAObjFld extends AbstractObjFld<StatelessOp, StatelessType> {

	public SAObjFld(ObjectIRBody bodyIR, Field field, boolean dummy) {
		super(bodyIR, field, dummy);
	}

	@Override
	public FldKind getKind() {
		return FldKind.STATELESS;
	}

	@Override
	protected StatelessType getType() {
		throw new UnsupportedOperationException("Field is stateless");
	}

	@Override
	protected FunctionBuilder<ObjectConstructorFn> constructorBuilder() {
		return new SAObjFldConstructorBuilder(this);
	}

}
