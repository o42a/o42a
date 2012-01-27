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
package org.o42a.core.ref.impl.path;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;


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
	public boolean skip(Step step, Scope scope) {
		return walk(walker().skip(step, scope));
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return walk(walker().staticScope(step, scope));
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
		return walk(walker().up(enclosed, step, enclosing));
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return walk(walker().member(container, step, member));
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		return walk(walker().arrayElement(array, step, element));
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		return walk(walker().refDep(object, step, dependency));
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
