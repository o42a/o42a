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


final class PathWalkTracker implements PathWalker {

	private final PathWalker walker;
	private boolean aborted;

	PathWalkTracker(PathWalker walker) {
		this.walker = walker;
	}

	public final boolean isAborted() {
		return this.aborted;
	}

	@Override
	public boolean root(Path path, Scope root) {
		return walk(this.walker.root(path, root));
	}

	@Override
	public boolean start(Path path, Scope start) {
		return walk(this.walker.start(path, start));
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		return walk(this.walker.module(fragment, module));
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		return walk(this.walker.up(enclosed, fragment, enclosing));
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {
		return walk(this.walker.member(container, fragment, member));
	}

	@Override
	public boolean dep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		return walk(this.walker.dep(object, fragment, dependency));
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		return walk(this.walker.materialize(artifact, fragment, result));
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
		this.walker.abortedAt(last, brokenFragment);
	}

	@Override
	public boolean done(Container result) {
		return walk(this.walker.done(result));
	}

	private final boolean walk(boolean succeed) {
		return this.aborted = !succeed;
	}

}
