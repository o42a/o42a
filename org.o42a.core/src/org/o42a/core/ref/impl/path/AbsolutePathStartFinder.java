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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;


public final class AbsolutePathStartFinder implements PathWalker {

	private int index;
	private Obj startObject;
	private int startIndex;
	private boolean unreachable;

	public final Obj getStartObject() {
		return this.startObject;
	}

	public final int getStartIndex() {
		return this.startIndex;
	}

	@Override
	public boolean root(Path path, Scope root) {
		this.startObject = root.toObject();
		return false;
	}

	@Override
	public boolean start(Path path, Scope start) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		this.startObject = module;
		this.startIndex = ++this.index;
		return true;
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		return set(enclosing.toObject());
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {

		final Field<?> field = member.toField(dummyUser());

		if (field == null) {
			return unreachable();
		}

		return set(field.toObject());
	}

	@Override
	public boolean arrayElement(Obj array, PathFragment fragment, ArrayElement element) {
		return unreachable();
	}

	@Override
	public boolean fieldDep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		return unreachable();
	}

	@Override
	public boolean refDep(Obj object, PathFragment fragment, Ref dependency) {
		return unreachable();
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		return set(result);
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return false;
	}

	private final boolean set(Obj object) {
		if (object == null || this.unreachable) {
			++this.index;
			return true;
		}
		this.startObject = object;
		this.startIndex = ++this.index;
		return true;
	}

	private final boolean unreachable() {
		this.unreachable = true;
		++this.index;
		return true;
	}

}
