/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ResolutionWalker;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.LocationInfo;


final class AccessorResolver implements ResolutionWalker, PathWalker {

	private boolean owner = true;
	private boolean declaration = true;
	private boolean enclosed = true;
	private boolean inheritant = true;

	private boolean ownerBeforeRoot;
	private boolean declarationBeforeRoot;
	private boolean enclosedBeforeRoot;
	private boolean inheritantBeforeRoot;

	public AccessorResolver() {
	}

	public final Accessor getAccessor() {
		if (this.owner) {
			return Accessor.OWNER;
		}
		if (this.declaration) {
			return Accessor.DECLARATION;
		}
		if (this.inheritant) {
			return Accessor.INHERITANT;
		}
		if (this.enclosed) {
			return Accessor.ENCLOSED;
		}
		return Accessor.PUBLIC;
	}

	@Override
	public PathWalker path(LocationInfo location, Path path) {
		return this;
	}

	@Override
	public boolean newObject(ScopeInfo location, Obj object) {
		this.owner = false;
		this.declaration &=
				object.getContext().declarationsVisibleFrom(
						location.getContext());
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {
		updateContainer(artifact.getContainer(), part.getContainer());
		return true;
	}

	@Override
	public boolean staticArtifact(LocationInfo location, Artifact<?> artifact) {
		return updateContainer(
				artifact.getContainer(),
				artifact.getContainer());
	}

	@Override
	public boolean root(Path path, Scope root) {
		this.ownerBeforeRoot = this.owner;
		this.declarationBeforeRoot = this.declaration;
		this.enclosedBeforeRoot = this.enclosed;
		return updateContainer(root.getContainer(), root.getContainer());
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		this.owner = this.ownerBeforeRoot;
		this.declaration = this.declarationBeforeRoot;
		this.enclosed = this.enclosedBeforeRoot;
		this.inheritant = this.inheritantBeforeRoot;
		return updateContainer(module, module);
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		updateDeclaration(enclosed, enclosing);
		updateInheritant(enclosed, enclosing);
		return true;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {
		this.owner = false;
		updateDeclaration(container, member.substance(dummyUser()));
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean arrayItem(Obj array, PathFragment fragment, ArrayItem item) {
		this.owner = false;
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean fieldDep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		this.owner = false;
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean refDep(Obj object, PathFragment fragment, Ref dependency) {
		this.owner = false;
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		if (artifact.toObject() == result) {
			return true;
		}
		updateDeclaration(artifact.getContainer(), result);
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private boolean updateDeclaration(Container current, Container container) {
		if (!this.declaration) {
			return false;
		}
		if (this.owner) {
			if (current == container) {
				return true;
			}
			this.owner = false;
		}
		return this.declaration =
				container.getContext().declarationsVisibleFrom(
						container.getContext());
	}

	private boolean updateEnclosed(Container current, Container container) {
		if (!this.enclosed) {
			return false;
		}

		final Scope oldScope = current.getScope();
		final Scope newScope = container.getScope();

		if (oldScope != newScope) {
			return this.enclosed = newScope.contains(oldScope);
		}

		final Member oldMember = current.toMember();
		final Member newMember = container.toMember();

		if (oldMember != null) {
			if (newMember == null) {
				return true;
			}
			return this.enclosed =
					oldMember.getKey().startsWith(newMember.getKey());
		}
		if (newMember != null) {
			return false;
		}
		return true;
	}

	private boolean updateInheritant(Container current, Container container) {
		if (!this.inheritant) {
			return false;
		}

		final Obj newObject = container.toObject();

		if (newObject == null) {
			return this.inheritant = false;
		}

		final Obj oldObject = current.toObject();

		return this.inheritant =
				oldObject.type().derivedFrom(newObject.type());
	}

	private boolean updateContainer(Container current, Container container) {
		updateDeclaration(current, container);
		updateEnclosed(current, container);
		updateInheritant(current, container);
		return true;
	}

}
