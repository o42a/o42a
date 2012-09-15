/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.source.CompilerContext;


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

	@Override
	public LocalOp toLocal() {
		return null;
	}

	@Override
	public HostOp field(CodeDirs dirs, MemberKey memberKey) {
		return materialize(dirs, tempObjHolder(dirs.getAllocator()))
				.field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return target(dirs).materialize(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return target(dirs).dereference(dirs, holder);
	}

	public abstract HostOp target(CodeDirs dirs);

	protected final HostValueOp targetValueOp() {
		return new PathTargetValueOp(this);
	}

	private static final class PathTargetValueOp implements HostValueOp {

		private final PathOp path;

		PathTargetValueOp(PathOp path) {
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
			return this.path.target(dirs).value();
		}

	}

	private static final class HostPathOp extends PathOp {

		HostPathOp(BoundPath path, HostOp pathStart, HostOp host) {
			super(path, pathStart, host);
		}

		@Override
		public HostValueOp value() {
			return host().value();
		}

		@Override
		public LocalOp toLocal() {
			return host().toLocal();
		}

		@Override
		public HostOp field(CodeDirs dirs, MemberKey memberKey) {
			return host().field(dirs, memberKey);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
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
	}

}
