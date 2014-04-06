/*
    Compiler Core
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
package org.o42a.core.ref.path.impl;

import java.util.ArrayList;

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
import org.o42a.util.ArrayUtil;


public class OddPathFragmentRemover implements PathWalker {

	private static final Entry NO_ENTRY = new Entry(null);

	private final ArrayList<Entry> entries;

	public OddPathFragmentRemover(int length) {
		this.entries = new ArrayList<>(length);
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
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {

		final Scope enclosingScope = enclosing.getScope();
		final int size = this.entries.size();

		for (Entry entry : this.entries) {
			if (entry.checkRemove(enclosingScope, size)) {
				break;
			}
		}

		this.entries.add(NO_ENTRY);

		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return enter(container.getScope());
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return enter(linkObject.getScope());
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return skip();
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		return skip();
	}

	@Override
	public boolean object(Step step, Obj object) {
		return enter(object.getScope().getEnclosingScope());
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		this.entries.clear();
		return true;
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	public Step[] removeOddFragments(Step[] steps) {

		final int size = this.entries.size();
		Step[] result = steps;
		int delta = 0;
		int i = 0;

		while (i < size) {

			final int removeUpto = this.entries.get(i).removeUpTo;

			if (removeUpto <= 0) {
				// No odd fragment starts at this step.
				++i;
				continue;
			}
			if (removeUpto >= steps.length) {
				// The odd fragment includes non-existing steps.
				// This is possible for template.
				++i;
				continue;
			}

			final int nextIdx = removeUpto + 1;

			// Remove the odd fragment.
			result = ArrayUtil.remove(result, i - delta, nextIdx - delta);
			// Increase the delta between the original path step index
			// and modified one.
			delta += nextIdx - i;
			// Continue from the first not removed step.
			i = nextIdx;
		}

		return result;
	}

	private boolean skip() {
		this.entries.add(NO_ENTRY);
		return true;
	}

	private boolean enter(Scope start) {
		this.entries.add(new Entry(start));
		return true;
	}

	private static final class Entry {

		private final Scope start;
		private int removeUpTo;

		Entry(Scope start) {
			this.start = start;
		}

		public boolean checkRemove(Scope scope, int index) {
			if (this.start != scope) {
				return false;
			}
			this.removeUpTo = index;
			return true;
		}

		@Override
		public String toString() {
			if (this.start == null) {
				return "-";
			}
			if (this.removeUpTo == 0) {
				return this.start.toString();
			}
			return this.start + "..." + this.removeUpTo;
		}

	}

}
