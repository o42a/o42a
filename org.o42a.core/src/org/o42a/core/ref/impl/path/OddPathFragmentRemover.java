/*
    Compiler Core
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
import org.o42a.util.ArrayUtil;


public class OddPathFragmentRemover implements PathWalker {

	private final Step[] steps;
	private final Scope[] entries;
	private final int[] removeUpto;
	private int index;

	public OddPathFragmentRemover(Path path) {
		this.steps = path.getSteps();
		this.entries = new Scope[this.steps.length];
		this.removeUpto = new int[this.steps.length];
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		return true;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		return skip();
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return skip();
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {

		final Scope enclosingScope = enclosing.getScope();

		for (int i = 0; i < this.index; ++i) {
			if (this.entries[i] == enclosingScope) {
				this.removeUpto[i] = this.index;
				break;
			}
		}

		return skip();
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return enter(container.getScope());
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		return enter(array.getScope());
	}

	@Override
	public boolean fieldDep(Obj object, Step step, Field<?> dependency) {
		return skip();
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		return skip();
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {
		return skip();
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	public Step[] removeOddFragments() {

		Step[] result = this.steps;
		int delta = 0;
		int i = 0;

		while (i < this.removeUpto.length) {

			final int removeUpto = this.removeUpto[i];

			if (removeUpto <= 0) {
				++i;
				continue;
			}

			final int nextIdx = removeUpto + 1;

			result = ArrayUtil.remove(result, i - delta, nextIdx - delta);
			delta += nextIdx - i;
			i = nextIdx;
		}

		return result;
	}

	private boolean skip() {
		++this.index;
		return true;
	}

	private boolean enter(Scope entry) {
		this.entries[this.index++] = entry;
		return true;
	}

}
