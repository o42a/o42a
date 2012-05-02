/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


final class AccessorResolver implements PathWalker {

	private boolean owner = true;
	private boolean declaration = true;
	private boolean enclosed = true;
	private boolean inheritant = true;

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
	public boolean root(BoundPath path, Scope root) {
		return updateContainer(root.getContainer(), root.getContainer());
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		return updateContainer(module, module);
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return updateContainer(
				scope.getContainer(),
				scope.getContainer());
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		updateDeclaration(enclosed, enclosing);
		updateInheritant(enclosed, enclosing);
		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		this.owner = false;
		updateDeclaration(container, member.substance(dummyUser()));
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		this.owner = false;
		updateDeclaration(linkObject, link.getTarget());
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		this.owner = false;
		updateDeclaration(array, element.getTarget());
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		this.owner = false;
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public boolean object(Step step, Obj object) {
		this.owner = false;
		this.declaration &=
				object.getContext().declarationsVisibleFrom(
						object.getContext());
		this.enclosed = false;
		this.inheritant = false;
		return true;
	}

	@Override
	public void pathTrimmed(BoundPath path, Scope root) {
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
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

		if (oldObject == null) {
			return this.inheritant = false;
		}

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
