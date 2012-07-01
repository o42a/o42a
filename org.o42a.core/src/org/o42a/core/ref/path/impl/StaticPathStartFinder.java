/*
    Compiler Core
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
package org.o42a.core.ref.path.impl;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


public final class StaticPathStartFinder implements PathWalker {

	private int index;
	private Scope startObjectScope;
	private Obj startObject;
	private int startIndex;

	public final Scope getStartObjectScope() {
		return this.startObjectScope;
	}

	public final Obj getStartObject() {
		return this.startObject;
	}

	public final int getStartIndex() {
		return this.startIndex;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		this.startObjectScope = root.getEnclosingScope();
		this.startObject = root.toObject();
		return true;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		this.startObjectScope = start.getEnclosingScope();
		this.startObject = start.toObject();
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		return set(module.getScope().getEnclosingScope(), module);
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return set(scope.getEnclosingScope(), scope.toObject());
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return set(enclosed.getScope(), enclosing.toObject());
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

		final MemberField field = member.toField();

		if (field == null) {
			return abort();// Not a field - not an object.
		}

		return set(
				container.getScope(),
				field.substance(dummyUser()).toObject());
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return abort();// Dereferenced object can not be used as static start.
	}

	@Override
	public boolean arrayIndex(
			Scope start,
			Step step,
			Ref array,
			Ref index,
			ArrayElement element) {
		return abort();// Can not use an array element as static start.
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		return abort();// Dependency can not be a part of static path.
	}

	@Override
	public boolean object(Step step, Obj object) {
		return set(object.getScope().getEnclosingScope(), object);
	}

	@Override
	public void pathTrimmed(BoundPath path, Scope root) {
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return false;
	}

	private final boolean set(Scope objectScope, Obj object) {
		if (object == null) {
			++this.index;
			return true;
		}
		this.startObjectScope = objectScope;
		this.startObject = object;
		if (object.getConstructionMode().isRuntime()) {
			return false;
		}
		this.startIndex = ++this.index;
		return true;
	}

	private final boolean abort() {
		++this.index;
		return false;
	}

}
