/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public final class DummyPathWalker implements PathWalker {

	public static final DummyPathWalker INSTANCE = new DummyPathWalker();

	private DummyPathWalker() {
	}

	@Override
	public boolean root(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		return true;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return true;
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return true;
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return true;
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return true;
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		return true;
	}

	@Override
	public boolean object(Step step, Obj object) {
		return true;
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

}
