/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.object.impl;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class OverrideRequirement implements PathWalker {

	public static boolean abstractsAllowedIn(Obj object) {
		if (object.isAbstract() || object.isPrototype()) {
			return true;
		}
		if (object.toClause() != null) {
			return true;
		}
		return object.isWrapper() || object.getDereferencedLink() != null;
	}

	private final Obj object;
	private Container top;
	private byte abstractsOverrideRequired;

	public OverrideRequirement(Obj object) {
		this.object = object;
	}

	public boolean overrideRequired(MemberField field) {
		assert field.getMemberOwner().toObject() == this.object :
			"The field " + field + " belongs to wrong object";
		if (!field.isAbstract()) {
			return false;
		}

		final TypeRef ancestor = this.object.type().getAncestor();

		if (ancestor == null
				|| ancestor.getType().member(field.getMemberKey()) == null) {
			// The field does not belong to ancestor.
			return true;
		}

		return isAbstractsOverrideRequired();
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		if (this.top != null) {
			return requireAbstractsOverride();
		}
		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		if (this.top == null) {
			if (!setTop(container)) {
				return false;
			}
		}
		if (member.toField() != null) {
			return true;
		}
		if (member.toClause() != null) {
			this.abstractsOverrideRequired = -1;
			return false;
		}
		return requireAbstractsOverride();
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		if (this.top == null) {
			if (!setTop(linkObject)) {
				return false;
			}
		}
		return requireAbstractsOverride();
	}

	@Override
	public boolean local(Scope scope, Local local) {
		return local.originalRef()
				.resolve(scope.walkingResolver(this))
				.isResolved();
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		if (this.top != null) {
			return requireAbstractsOverride();
		}

		final Scope enclosingScope = dep.enclosingScope(object.getScope());
		final Container enclosing = enclosingScope.getContainer();

		if (!setTop(enclosing)) {
			return false;
		}

		final Ref dependency = dep.ref();

		if (dependency.isStatic()) {
			return requireAbstractsOverride();
		}

		return dependency.resolve(enclosingScope.walkingResolver(this))
				.isResolved();
	}

	@Override
	public boolean object(Step step, Obj object) {
		if (this.top == null) {
			if (!setTop(object.getScope().getEnclosingScope().getContainer())) {
				return false;
			}
		}
		return requireAbstractsOverride();
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return true;
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private boolean setTop(Container top) {
		this.top = top;

		final Obj topObject = top.toObject();

		if (abstractsAllowedIn(topObject)) {
			return true;
		}

		final OverrideRequirement topOverrideRequirement =
				new OverrideRequirement(topObject);

		if (!topOverrideRequirement.isAbstractsOverrideRequired()) {
			return true;
		}

		return requireAbstractsOverride();
	}

	private boolean requireAbstractsOverride() {
		this.abstractsOverrideRequired = 1;
		return false;
	}

	private boolean isAbstractsOverrideRequired() {
		if (this.abstractsOverrideRequired != 0) {
			return this.abstractsOverrideRequired > 0;
		}
		if (abstractsAllowedIn(this.object)) {
			this.abstractsOverrideRequired = -1;
			return false;
		}

		final TypeRef ancestor = this.object.type().getAncestor();

		if (ancestor == null || ancestor.isStatic()) {
			this.abstractsOverrideRequired = 1;
			return true;
		}

		final Resolution resolution = ancestor.getRef().resolve(
				ancestor.getScope().walkingResolver(this));

		resolution.isResolved();

		return this.abstractsOverrideRequired > 0;
	}

}
