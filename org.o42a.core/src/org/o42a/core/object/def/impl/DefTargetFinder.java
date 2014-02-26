/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.object.def.impl;

import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.staticPath;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class DefTargetFinder implements PathWalker, PathModifier {

	public static BoundPath defTarget(BoundPath path, Scope scope) {
		return path.modifyPath(new DefTargetFinder(scope));
	}

	private final Scope scope;
	private BoundPath originalPath;
	private BoundPath path;

	private DefTargetFinder(Scope scope) {
		this.scope = scope;
	}

	public final BoundPath getTarget() {
		return this.path;
	}

	public final Scope getScope() {
		return this.scope;
	}

	@Override
	public BoundPath modifyPath(BoundPath path) {

		final DefTargetFinder finder = new DefTargetFinder(getScope());

		path.walk(
				pathResolver(path.getOrigin().getScope(), dummyRefUser()),
				finder);

		return finder.getTarget();
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		this.originalPath = path;
		this.path = path.getPath().bind(path, getScope());
		return false;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		this.originalPath = path;
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		return staticScope(step, module.getScope());
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		this.path =
				staticPath(getScope(), scope)
				.bind(this.originalPath, getScope());
		return true;
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		if (this.path != null) {
			return append(step);
		}
		return initialUp(enclosing.getScope());
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return appendIfExist(step);
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return appendIfExist(step);
	}

	@Override
	public boolean local(Scope scope, Local local) {
		return appendIfExist(local);
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		if (this.path != null) {
			return append(dep);
		}
		if (!initialUp(dep.enclosingScope(object.getScope()))) {
			return false;
		}
		this.path = this.path.append(dep.ref().getPath().getPath());
		return true;
	}

	@Override
	public boolean object(Step step, Obj object) {
		return appendIfExist(step);
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		this.path = null;
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private boolean appendIfExist(Step step) {
		if (this.path == null) {
			return false;
		}
		return append(step);
	}

	private boolean append(Step step) {
		this.path = this.path.append(step);
		return true;
	}

	private boolean initialUp(Scope enclosing) {
		assert this.path == null :
			"Not initial up";
		if (!enclosing.is(getScope())) {
			this.path = null;
			return false;
		}
		if (this.originalPath.isStatic()) {
			this.path = SELF_PATH.bindStatically(
					this.originalPath,
					getScope());
		} else {
			this.path = SELF_PATH.bind(this.originalPath, getScope());
		}
		return true;
	}

}
