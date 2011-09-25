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
package org.o42a.core.ref.impl.path;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;


public class PathUpscoper implements PathWalker {

	private final Scope toScope;
	private int cut;

	public PathUpscoper(Scope toScope) {
		this.toScope = toScope;
	}

	public final boolean succeed() {
		return this.cut > 0;
	}

	public final int getCut() {
		return this.cut;
	}

	@Override
	public boolean root(Path path, Scope root) {
		return false;
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		return false;
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		++this.cut;
		if (enclosing.getScope() != this.toScope) {
			// Target scope not reached yet.
			return true;
		}

		final Member member = enclosing.toMember();

		if (member == null) {
			// Target scope reached
			return false;
		}

		// Not a compound member in the same scope.
		return member.getKey().getMemberId().getEnclosingId() == null;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {
		return fail();
	}

	@Override
	public boolean arrayItem(Obj array, PathFragment fragment, ArrayItem item) {
		return fail();
	}

	@Override
	public boolean fieldDep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		return fail();
	}

	@Override
	public boolean refDep(Obj object, PathFragment fragment, Ref dependency) {
		return fail();
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		return fail();
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private final boolean fail() {
		this.cut = -1;
		return false;
	}

}
