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
package org.o42a.core.ref.impl;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.ResolutionWalker;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public final class ResolutionRootFinder
		implements ResolutionWalker, PathWalker {

	public static Scope resolutionRoot(TypeRef typeRef) {

		final Scope scope = typeRef.getRescoper().rescope(typeRef.getScope());
		final ResolutionRootFinder finder = new ResolutionRootFinder(scope);
		final Resolver resolver = scope.walkingResolver(dummyUser(), finder);

		typeRef.getRef().resolve(resolver);

		return finder.getRoot();
	}

	private Container root;

	private ResolutionRootFinder(Scope scope) {
		this.root = scope.getContainer();
	}

	public final Scope getRoot() {
		return this.root.getScope();
	}

	@Override
	public PathWalker path(LocationInfo location, Path path) {
		if (path.isAbsolute()) {
			this.root = this.root.getContext().getRoot();
			return null;
		}
		return this;
	}

	@Override
	public boolean newObject(ScopeInfo location, Obj object) {

		final Resolver ancestorResolver =
				this.root.getScope().walkingResolver(dummyUser(), this);
		final TypeRef ancestor = object.type().getAncestor();

		if (ancestor == null) {
			return false;
		}

		final Resolution ancestorResolution =
				ancestor.getRef().resolve(ancestorResolver);

		return ancestorResolution != null;
	}

	@Override
	public boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {
		return false;
	}

	@Override
	public boolean staticArtifact(LocationInfo location, Artifact<?> artifact) {
		this.root = this.root.getContext().getRoot();
		return false;
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
		this.root = enclosing;
		return true;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {

		final Container substance = member.substance(dummyUser());

		if (substance.getScope() != this.root.getScope()) {
			// Member access - root already reached.
			return false;
		}

		final Member oldMember = this.root.toMember();

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
		this.root = object.getScope().getEnclosingScope().toLocal();
		return false;
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		// Materialized object is not a root.
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
