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
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;


public class PathTracker implements PathWalker {

	protected final PathResolver initialResolver;
	private final PathWalker walker;
	private boolean aborted;

	public PathTracker(PathResolver resolver, PathWalker walker) {
		this.initialResolver = resolver;
		this.walker = walker;
	}

	public PathResolver nextResolver() {
		return this.initialResolver;
	}

	public final boolean isAborted() {
		return this.aborted;
	}

	@Override
	public final boolean root(Path path, Scope root) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean start(Path path, Scope start) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean module(Step fragment, Obj module) {
		return walk(this.walker.module(fragment, module));
	}

	@Override
	public boolean up(
			Container enclosed,
			Step fragment,
			Container enclosing) {
		return walk(this.walker.up(enclosed, fragment, enclosing));
	}

	@Override
	public boolean member(
			Container container,
			Step fragment,
			Member member) {
		return walk(this.walker.member(container, fragment, member));
	}

	@Override
	public boolean arrayElement(Obj array, Step fragment, ArrayElement element) {
		return walk(this.walker.arrayElement(array, fragment, element));
	}

	@Override
	public boolean fieldDep(
			Obj object,
			Step fragment,
			Field<?> dependency) {
		return walk(this.walker.fieldDep(object, fragment, dependency));
	}

	@Override
	public boolean refDep(Obj object, Step fragment, Ref dependency) {
		return walk(this.walker.refDep(object, fragment, dependency));
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			Step fragment,
			Obj result) {
		return walk(this.walker.materialize(artifact, fragment, result));
	}

	@Override
	public void abortedAt(Scope last, Step brokenFragment) {
		this.walker.abortedAt(last, brokenFragment);
	}

	@Override
	public boolean done(Container result) {
		return walk(this.walker.done(result));
	}

	boolean walk(boolean succeed) {
		this.aborted = !succeed;
		return succeed;
	}

}
