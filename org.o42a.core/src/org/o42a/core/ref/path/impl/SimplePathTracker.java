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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class SimplePathTracker extends PathTracker {

	public SimplePathTracker(
			BoundPath path,
			PathResolver resolver,
			PathWalker walker) {
		super(path, resolver, walker);
	}

	@Override
	public boolean module(Step step, Obj module) {
		return walk(walker().module(step, module));
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return walk(walker().staticScope(step, scope));
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return walk(walker().up(enclosed, step, enclosing, reversePath));
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return walk(walker().member(container, step, member));
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return walk(walker().dereference(linkObject, step, link));
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return walk(walker().local(step, scope, local));
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		return walk(walker().dep(object, dep));
	}

	@Override
	public boolean object(Step step, Obj object) {
		return walk(walker().object(step, object));
	}

	@Override
	public boolean done(Container result) {
		return walk(walker().done(result));
	}

}
