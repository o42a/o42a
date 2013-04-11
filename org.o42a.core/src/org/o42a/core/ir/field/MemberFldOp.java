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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


public abstract class MemberFldOp extends FldOp {

	public MemberFldOp(MemberFld fld, ObjOp host) {
		super(host, fld);
	}

	@Override
	public ID getId() {
		if (!isOmitted()) {
			return super.getId();
		}
		return fld().getField().getId();
	}

	@Override
	public MemberFld fld() {
		return (MemberFld) super.fld();
	}

	protected final HostValueOp objectFldValueOp() {
		return new ObjectFldValueOp(this);
	}

	private static final class ObjectFldValueOp implements HostValueOp {

		private final MemberFldOp fld;

		ObjectFldValueOp(MemberFldOp fld) {
			this.fld = fld;
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			object(dirs).value().writeCond(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return object(dirs.dirs()).value().writeValue(dirs);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			object(dirs).value().assign(dirs, value);
		}

		@Override
		public String toString() {
			if (this.fld == null) {
				return super.toString();
			}
			return this.fld.toString();
		}

		private final ObjectOp object(CodeDirs dirs) {
			return this.fld.materialize(
					dirs,
					tempObjHolder(dirs.getAllocator()));
		}

	}

}
