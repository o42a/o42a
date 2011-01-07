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
package org.o42a.core.ref.path;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;


public interface PathWalker {

	boolean root(Path path, Scope root);

	boolean start(Path path, Scope start);

	boolean module(PathFragment fragment, String moduleId, Obj module);

	boolean up(Container enclosed, PathFragment fragment, Container enclosing);

	boolean member(
			Container container,
			PathFragment fragment,
			Member member);

	boolean dep(Obj object, PathFragment fragment, Field<?> dependency);

	boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result);

	void abortedAt(Scope last, PathFragment brokenFragment);

	boolean done(Container result);

}
