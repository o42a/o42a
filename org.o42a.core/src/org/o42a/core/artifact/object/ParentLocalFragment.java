/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.util.use.UserInfo;


final class ParentLocalFragment extends PathFragment {

	private final Obj object;

	ParentLocalFragment(Obj object) {
		this.object = object;
	}

	@Override
	public Container resolve(
			LocationInfo location,
			UserInfo user,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = start.toObject();

		object.assertDerivedFrom(this.object);

		final Container result =
			object.getScope().getEnclosingContainer();

		walker.up(object, this, result);

		return result;
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {
		return new OpaqueLocalOp(start);
	}

	@Override
	public PathFragment combineWithMember(MemberKey memberKey) {
		return this.object.addDep(memberKey);
	}

	@Override
	public PathFragment combineWithLocalOwner(Obj owner) {
		return this.object.addEnclosingOwnerDep(owner);
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		return reproducedPath(scope.getScope().getEnclosingScopePath());
	}

	@Override
	public int hashCode() {
		return this.object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final ParentLocalFragment other = (ParentLocalFragment) obj;

		return this.object == other.object;
	}

	@Override
	public String toString() {
		return "ParentLocal[" + this.object + ']';
	}

	private static final class OpaqueLocalOp implements HostOp {

		private final HostOp host;

		OpaqueLocalOp(HostOp host) {
			this.host = host;
		}

		@Override
		public Generator getGenerator() {
			return this.host.getGenerator();
		}

		@Override
		public CodeBuilder getBuilder() {
			return this.host.getBuilder();
		}

		@Override
		public CompilerContext getContext() {
			return this.host.getContext();
		}

		@Override
		public ObjectOp toObject(CodeDirs dirs) {
			return null;
		}

		@Override
		public LocalOp toLocal() {
			return null;
		}

		@Override
		public HostOp field(CodeDirs dirs, MemberKey memberKey) {
			throw new UnsupportedOperationException(
					"Can not retrieve a field of " + this);
		}

		@Override
		public ObjectOp materialize(CodeDirs dirs) {
			throw new UnsupportedOperationException(
					"Can not materialize " + this);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "OpaqueParentLocal[" + this.host + ']';
		}

	}

}
