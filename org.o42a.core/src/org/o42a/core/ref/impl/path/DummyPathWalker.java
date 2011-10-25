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
package org.o42a.core.ref.impl.path;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


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
	public boolean skip(Step step, Scope scope) {
		return true;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return true;
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return true;
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		return true;
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		return true;
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {
		return true;
	}

	@Override
	public boolean object(Step step, Obj object) {
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
