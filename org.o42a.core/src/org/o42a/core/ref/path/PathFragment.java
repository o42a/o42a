/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.member.MemberKey;


public abstract class PathFragment {

	static final MaterializeFragment MATERIALIZE = new MaterializeFragment();

	public boolean isAbsolute() {
		return false;
	}

	public String getName() {
		return null;
	}

	public abstract Container resolve(
			LocationInfo location,
			Path path,
			int index,
			Scope start,
			PathWalker walker);

	public abstract Reproduction reproduce(LocationInfo location, Scope scope);

	public PathFragment combineWithMember(MemberKey memberKey) {
		return null;
	}

	public PathFragment combineWithLocalOwner(Obj owner) {
		return null;
	}

	public final Path toPath() {
		if (isAbsolute()) {
			return new AbsolutePath(this);
		}
		return new Path(this);
	}

	public abstract HostOp write(Code code, CodePos exit, HostOp start);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected PathFragment rebuild(PathFragment prev) {
		return null;
	}

	protected final Reproduction reproduced(Path path) {
		return new Reproduction(path, false, false);
	}

	protected final Reproduction reproduced(PathFragment fragment) {
		return new Reproduction(fragment.toPath(), false, false);
	}

	protected final Reproduction unchanged() {
		return new Reproduction(toPath(), true, false);
	}

	protected final Reproduction outOfClause(Path path) {
		return new Reproduction(path, false, true);
	}

	public static final class Reproduction {

		private final Path path;
		private final boolean unchanged;
		private final boolean outOfClause;

		private Reproduction(
				Path path,
				boolean unchanged,
				boolean outOfClause) {
			this.path = path;
			this.unchanged = unchanged;
			this.outOfClause = outOfClause;
		}

		public final boolean isUnchanged() {
			return this.unchanged;
		}

		public final boolean isOutOfClause() {
			return this.outOfClause;
		}

		public final Path getPath() {
			return this.path;
		}

		@Override
		public String toString() {
			return "" + this.path;
		}

	}

}
