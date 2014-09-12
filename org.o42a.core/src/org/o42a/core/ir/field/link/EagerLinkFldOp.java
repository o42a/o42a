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
package org.o42a.core.ir.field.link;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.VmtIRChain.Op;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.HostValueOp;
import org.o42a.core.member.MemberKey;


public class EagerLinkFldOp
		extends RefFldOp<StatefulOp, StatefulType, ObjectRefFn> {

	private final StatefulOp ptr;

	EagerLinkFldOp(EagerLinkFld fld, ObjOp host, StatefulOp ptr) {
		super(fld, host);
		this.ptr = ptr;
	}

	@Override
	public final EagerLinkFld fld() {
		return (EagerLinkFld) super.fld();
	}

	@Override
	public final StatefulOp ptr() {
		return this.ptr;
	}

	@Override
	public HostValueOp value() {
		return new EagerLinkFldValueOp(this);
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {
		throw new UnsupportedOperationException(
				"Eager link field has no fields");
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs, holder);
	}

	@Override
	public ObjectOp target(CodeDirs dirs, ObjHolder holder) {
		return super.target(dirs, holder.toVolatile());
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException(
				"Eager link field can not be materialized");
	}

	@Override
	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {
		return loadOrConstructTarget(dirs, holder, false);
	}

	@Override
	protected DataOp construct(Code code, ObjectRefFn constructor, Op vmtc) {
		return constructor.call(code, host(), vmtc);
	}

	private static final class EagerLinkFldValueOp
			extends AbstractLinkFldValueOp<EagerLinkFldOp> {

		EagerLinkFldValueOp(EagerLinkFldOp fld) {
			super(fld);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
