/*
    Modules Commons
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.common.resolution;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


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
	public boolean up(Container enclosed, Step step, Container enclosing) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.up(enclosed, step, enclosing) & proceed;
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
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.arrayElement(array, step, element) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean fieldDep(Obj object, Step step, Field<?> dependency) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.fieldDep(object, step, dependency) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.refDep(object, step, dependency) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.materialize(artifact, step, result) & proceed;
		}

		return proceed;
	}

	@Override
	public boolean newObject(Step step, Obj object) {

		boolean proceed = true;

		for (PathWalker walker : getWalkers()) {
			proceed = walker.newObject(step, object) & proceed;
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
