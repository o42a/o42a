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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.MaterializerFragment;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public abstract class PathFragment {

	public static final MaterializerFragment MATERIALIZE =
			MaterializerFragment.INSTANCE;

	public boolean isAbsolute() {
		return false;
	}

	public String getName() {
		return null;
	}

	public boolean isMaterializer() {
		return false;
	}

	public abstract PathFragment materialize();

	public abstract Container resolve(
			PathResolver resolver,
			Path path,
			int index,
			Scope start,
			PathWalker walker);

	public abstract PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope);

	public PathFragment combineWithMember(MemberKey memberKey) {
		return null;
	}

	public PathFragment combineWithLocalOwner(Obj owner) {
		return null;
	}

	public PathFragment combineWithRef(Ref ref) {
		return null;
	}

	public final Path toPath() {
		if (isAbsolute()) {
			return new AbsolutePath(this);
		}
		return new Path(this);
	}

	public abstract HostOp write(CodeDirs dirs, HostOp start);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected PathFragment rebuild(PathFragment prev) {
		return null;
	}

}
