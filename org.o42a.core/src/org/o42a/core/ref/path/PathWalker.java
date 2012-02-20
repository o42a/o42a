/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.impl.DummyPathWalker;


public interface PathWalker {

	PathWalker DUMMY_PATH_WALKER = DummyPathWalker.INSTANCE;

	boolean root(BoundPath path, Scope root);

	boolean start(BoundPath path, Scope start);

	boolean module(Step step, Obj module);

	boolean skip(Step step, Scope scope);

	boolean staticScope(Step step, Scope scope);

	boolean up(Container enclosed, Step step, Container enclosing);

	boolean member(Container container, Step step, Member member);

	boolean arrayElement(Obj array, Step step, ArrayElement element);

	boolean refDep(Obj object, Step step, Ref dependency);

	boolean object(Step step, Obj object);

	void abortedAt(Scope last, Step brokenStep);

	boolean done(Container result);

}
