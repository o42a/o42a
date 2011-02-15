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
package org.o42a.core.ref;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;


final class ResolutionRootFinder implements PathWalker {

	private Container container;
	private Path root = Path.SELF_PATH;

	ResolutionRootFinder(Ref ref) {
		this.container = ref.getContainer();
	}

	public final Path getRoot() {
		return this.root;
	}

	@Override
	public boolean root(Path path, Scope root) {
		throw new IllegalStateException();
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		throw new IllegalStateException();
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		this.container = enclosing;
		this.root = this.root.append(fragment);
		return true;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {

		final Container substance = member.getSubstance();

		if (substance.getScope() != this.container.getScope()) {
			// Member access - root already reached.
			return false;
		}

		final Member oldMember = this.container.toMember();

		if (oldMember == null) {
			// Member access - root already reached.
			return false;
		}

		if (oldMember.getKey().startsWith(member.getKey())) {
			// Enclosing member access - go up.
			return up(container, fragment, substance);
		}

		return false;
	}

	@Override
	public boolean dep(Obj object, PathFragment fragment, Field<?> dependency) {
		// Treat the enclosing local scope as resolution root.
		this.root = this.root.append(object.getScope().getEnclosingScopePath());
		this.container = object.getScope().getEnclosingScope().toLocal();
		return false;
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		// Materialized object is not root.
		return false;
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

}
