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
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.ir.object.vmt.VmtIRChain.Op;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.MemberKey;


public class EagerLinkFldOp
		extends ConstructedRefFldOp<StatefulOp, StatefulType, ObjectRefFn> {

	EagerLinkFldOp(ObjOp host, EagerLinkFld fld, OpMeans<StatefulOp> ptr) {
		super(host, fld, ptr);
	}

	private EagerLinkFldOp(EagerLinkFldOp proto, OpPresets presets) {
		super(proto, presets);
	}

	@Override
	public EagerLinkFldOp setPresets(OpPresets presets) {
		if (presets.is(getPresets())) {
			return this;
		}
		return new EagerLinkFldOp(this, presets);
	}

	@Override
	public final EagerLinkFld fld() {
		return (EagerLinkFld) super.fld();
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
