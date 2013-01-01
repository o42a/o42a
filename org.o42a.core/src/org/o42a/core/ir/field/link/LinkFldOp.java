/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.op.CodeDirs;


public class LinkFldOp extends RefFldOp<LinkFld.Op, ObjectRefFunc> {

	private final LinkFld.Op ptr;

	LinkFldOp(LinkFld fld, ObjOp host, LinkFld.Op ptr) {
		super(fld, host);
		this.ptr = ptr;
	}

	@Override
	public final LinkFld fld() {
		return (LinkFld) super.fld();
	}

	@Override
	public final LinkFld.Op ptr() {
		return this.ptr;
	}

	@Override
	public HostValueOp value() {
		return new LinkFldValueOp(this);
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
	protected ObjectOp findTarget(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();
		final DataOp constructed = ptr().construct(code, host());

		constructed.isNull(null, code).go(code, dirs.falseDir());

		return holder.set(code, createObject(code, constructed));
	}

	private static final class LinkFldValueOp
			extends AbstractLinkFldValueOp<LinkFldOp> {

		LinkFldValueOp(LinkFldOp fld) {
			super(fld);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
