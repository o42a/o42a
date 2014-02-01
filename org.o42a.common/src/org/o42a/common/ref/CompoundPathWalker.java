/*
    Compiler Commons
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
package org.o42a.common.ref;

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


public class CompoundPathWalker implements PathWalker {

	private final PathWalker[] walkers;

	public CompoundPathWalker(PathWalker... walkers) {
		this.walkers = walkers;
	}

	public final PathWalker[] getWalkers() {
		return this.walkers;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.root(path, root) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.start(path, start) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean module(Step step, Obj module) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.module(step, module) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.staticScope(step, scope) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed =
					walker.up(enclosed, step, enclosing, reversePath)
					& proceed;
		}

		return proceed;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.member(container, step, member) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.dereference(linkObject, step, link) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean local(Scope scope, Local local) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.local(scope, local) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean dep(Obj object, Dep dep) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.dep(object, dep) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean object(Step step, Obj object) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.object(step, object) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.pathTrimmed(path, root) & proceed;
		}

		return proceed;
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		for (PathWalker walker : getWalkers()) {
			walker.abortedAt(last, brokenStep);
		}
	}

	@Override
	public boolean done(Container result) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.done(result) & proceed;
		}

		return proceed;
	}

}
