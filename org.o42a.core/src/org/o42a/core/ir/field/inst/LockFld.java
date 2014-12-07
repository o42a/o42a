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
package org.o42a.core.ir.field.inst;

import static org.o42a.codegen.data.Content.noContent;
import static org.o42a.core.ir.field.inst.InstFldKind.INST_LOCK;
import static org.o42a.core.ir.field.inst.ObjectIRLock.OBJECT_IR_LOCK;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Content;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;


public class LockFld extends InstFld<ObjectIRLock.Op, ObjectIRLock> {

	public LockFld(ObjectIRBody bodyIR) {
		super(bodyIR);
	}

	@Override
	public InstFldKind getInstFldKind() {
		return INST_LOCK;
	}

	@Override
	public LockFldOp op(Code code, ObjOp host) {
		return new LockFldOp(
				host,
				this,
				code.means(c -> host.ptr().field(c, getTypeInstance())));
	}

	@Override
	public LockFld derive(ObjectIRBody inheritantBodyIR) {
		return new LockFld(inheritantBodyIR);
	}

	@Override
	protected ObjectIRLock getType() {
		return OBJECT_IR_LOCK;
	}

	@Override
	protected Content<ObjectIRLock> content() {
		return noContent();
	}

}
