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
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.impl.DummyPathWalker;


public interface PathWalker {

	PathWalker DUMMY_PATH_WALKER = DummyPathWalker.INSTANCE;

	boolean root(BoundPath path, Scope root);

	boolean start(BoundPath path, Scope start);

	boolean module(Step step, Obj module);

	boolean staticScope(Step step, Scope scope);

	boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath);

	boolean member(Container container, Step step, Member member);

	boolean dereference(Obj linkObject, Step step, Link link);

	boolean arrayIndex(
			Scope start,
			Step step,
			Ref array,
			Ref index,
			ArrayElement element);

	boolean dep(Obj object, Step step, Ref dependency);

	boolean object(Step step, Obj object);

	/**
	 * Informs walker about path trimming.
	 *
	 * <p>This can happen when some PathFragment is absolute. In this case
	 * the beginning of the Path gets trimmed and step index starts over from
	 * zero.</p>
	 *
	 * <p>This only happens at path rebuild phase. So, this method won't be
	 * called for ordinary path walkers, as they applied to already rebuild
	 * paths.</p>
	 *
	 * @param path rebuilding path.
	 * @param root root scope.
	 */
	void pathTrimmed(BoundPath path, Scope root);

	void abortedAt(Scope last, Step brokenStep);

	boolean done(Container result);

}
