/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;


public abstract class PathOp implements HostOp {

	private final HostOp host;

	public PathOp(HostOp host) {
		this.host = host;
	}

	public final HostOp host() {
		return this.host;
	}

	@Override
	public final CodeBuilder getBuilder() {
		return host().getBuilder();
	}

	public abstract HostOp pathTarget(CodeDirs dirs);

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {
		return pathTarget(dirs).field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return pathTarget(dirs).materialize(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return pathTarget(dirs).dereference(dirs, holder);
	}

	protected final HostValueOp pathValueOp() {
		return new PathValueOp(this);
	}

	private static final class PathValueOp implements HostValueOp {

		private final PathOp path;

		PathValueOp(PathOp path) {
			this.path = path;
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			targetValue(dirs).writeCond(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return targetValue(dirs.dirs()).writeValue(dirs);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			targetValue(dirs).assign(dirs, value);
		}

		@Override
		public String toString() {
			if (this.path == null) {
				return super.toString();
			}
			return this.path.toString();
		}

		private HostValueOp targetValue(CodeDirs dirs) {
			return this.path.pathTarget(dirs).value();
		}

	}

}
