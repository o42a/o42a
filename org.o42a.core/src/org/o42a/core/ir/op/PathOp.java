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
package org.o42a.core.ir.op;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.ID;


public abstract class PathOp implements HostOp {

	public static PathOp hostPathOp(
			BoundPath path,
			HostOp pathStart,
			HostOp host) {
		return new HostPathOp(path, pathStart, host);
	}

	private final BoundPath path;
	private final HostOp pathStart;
	private final HostOp host;

	public PathOp(BoundPath path, HostOp pathStart, HostOp host) {
		this.path = path;
		this.pathStart = pathStart;
		this.host = host;
	}

	public PathOp(PathOp start) {
		this.path = start.getPath();
		this.pathStart = start.pathStart();
		this.host = start;
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final HostOp pathStart() {
		return this.pathStart;
	}

	public final HostOp host() {
		return this.host;
	}

	@Override
	public final Generator getGenerator() {
		return this.host.getGenerator();
	}

	@Override
	public final CodeBuilder getBuilder() {
		return host().getBuilder();
	}

	@Override
	public final CompilerContext getContext() {
		return host().getContext();
	}

	public abstract HostOp pathTarget(CodeDirs dirs);

	protected final HostTargetOp pathTargetOp() {
		return new PathTargetOp(this);
	}

	protected final HostValueOp pathValueOp() {
		return new PathValueOp(this);
	}

	protected abstract TargetStoreOp allocateStore(ID id, Code code);

	private static final class PathTargetOp implements HostTargetOp {

		private final PathOp path;

		PathTargetOp(PathOp path) {
			this.path = path;
		}

		@Override
		public TargetOp op(CodeDirs dirs) {
			return target(dirs).op(dirs);
		}

		@Override
		public FldOp<?> field(CodeDirs dirs, MemberKey memberKey) {
			return target(dirs).field(dirs, memberKey);
		}

		@Override
		public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
			return target(dirs).materialize(dirs, holder);
		}

		@Override
		public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
			return target(dirs).dereference(dirs, holder);
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {
			return this.path.allocateStore(id, code);
		}

		@Override
		public String toString() {
			if (this.path == null) {
				return super.toString();
			}
			return this.path.toString();
		}

		private HostTargetOp target(CodeDirs dirs) {
			return this.path.pathTarget(dirs).target();
		}

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

	private static final class HostPathOp extends PathOp {

		HostPathOp(BoundPath path, HostOp pathStart, HostOp host) {
			super(path, pathStart, host);
		}

		@Override
		public HostTargetOp target() {
			return host().target();
		}

		@Override
		public HostValueOp value() {
			return host().value();
		}

		@Override
		public HostOp pathTarget(CodeDirs dirs) {
			return host();
		}

		@Override
		public String toString() {

			final HostOp host = host();

			if (host == null) {
				return super.toString();
			}

			return host.toString();
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return host().target().allocateStore(id, code);
		}

	}

}
