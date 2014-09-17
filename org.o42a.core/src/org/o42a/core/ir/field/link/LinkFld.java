/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.link;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld.StatelessOp;
import org.o42a.core.ir.field.RefFld.StatelessType;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


public class LinkFld extends AbstractLinkFld<StatelessOp, StatelessType> {

	public LinkFld(
			ObjectIRBody bodyIR,
			Field field,
			boolean dummy,
			Obj target,
			Obj targetAscendant) {
		super(bodyIR, field, dummy, target, targetAscendant);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.LINK;
	}

	@Override
	public StatelessType getInstance() {
		throw new UnsupportedOperationException("Field is stateless");
	}

	@Override
	protected StatelessType getType() {
		throw new UnsupportedOperationException("Field is stateless");
	}

	@Override
	protected FuncPtr<ObjectRefFn> constructorStub() {
		throw new IllegalStateException(
				"The link constructor can not be a stub");
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp result = construct(builder, dirs);

		result.toData(null, code).returnValue(code);
	}

	@Override
	protected LinkFldOp op(Code code, ObjOp host, StatelessOp ptr) {
		return new LinkFldOp(host, this, ptr);
	}

}
