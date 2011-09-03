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
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;


public class DummyPathWalker implements PathWalker {

	@Override
	public boolean root(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		return true;
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		return true;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {
		return true;
	}

	@Override
	public boolean fieldDep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		return true;
	}

	@Override
	public boolean refDep(Obj object, PathFragment fragment, Ref dependency) {
		return true;
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		return true;
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

}
