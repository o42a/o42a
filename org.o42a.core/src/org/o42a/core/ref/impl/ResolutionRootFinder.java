/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public final class ResolutionRootFinder implements PathWalker {

	public static Scope resolutionRoot(TypeRef typeRef) {

		final Scope scope = typeRef.getScope();
		final ResolutionRootFinder finder = new ResolutionRootFinder(scope);
		final Resolver resolver = scope.walkingResolver(finder);

		if (typeRef.getRef().resolve(resolver).isError()) {
			return null;
		}

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
	public boolean root(BoundPath path, Scope root) {
		this.root = root.toObject();
		return false;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		if (!path.isStatic()) {
			return true;
		}
		this.root = this.root.getLocation().getContext().getRoot();
		return false;
	}

	@Override
	public boolean module(Step step, Obj module) {
		throw new IllegalStateException();
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		this.root = this.root.getLocation().getContext().getRoot();
		return false;
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		this.root = enclosing;
		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

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
		if (oldMember.getMemberKey().startsWith(member.getMemberKey())) {
			// Enclosing member access - go up.
			return up(container, step, substance, null);
		}

		return false;
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		this.root = linkObject;
		return false;
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return local.ref().resolve(scope.walkingResolver(this)).isResolved();
	}

	@Override
	public boolean dep(Obj object, Dep dep) {

		final Scope enclosingScope = dep.enclosingScope(object.getScope());

		this.root = enclosingScope.getContainer();

		return dep.ref()
				.resolve(enclosingScope.walkingResolver(this))
				.isResolved();
	}

	@Override
	public boolean object(Step step, Obj object) {

		final Resolver ancestorResolver =
				this.root.getScope().walkingResolver(this);
		final TypeRef ancestor = object.type().getAncestor();

		if (ancestor == null) {
			return false;
		}

		return ancestor.getRef().resolve(ancestorResolver).isResolved();
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

}
