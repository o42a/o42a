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

import java.util.ArrayList;

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
import org.o42a.util.ArrayUtil;


public class OddPathFragmentRemover implements PathWalker {

	private static final Entry NO_ENTRY = new Entry(null);

	private final BoundPath path;
	private final ArrayList<Entry> entries;

	public OddPathFragmentRemover(BoundPath path) {
		this.path = path;
		this.entries = new ArrayList<Entry>(path.getRawSteps().length);
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
		return skip(step);
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return skip(step);
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
		handlePathTrimming(step);

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
		return enter(step, container.getScope());
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		return enter(step, array.getScope());
	}

	@Override
	public boolean fieldDep(Obj object, Step step, Field<?> dependency) {
		return skip(step);
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		return skip(step);
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {
		return skip(step);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	public Step[] removeOddFragments() {

		final int size = this.entries.size();
		Step[] result = this.path.getSteps();
		int delta = 0;
		int i = 0;

		while (i < size) {

			final int removeUpto = this.entries.get(i).removeUpTo;

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

	private boolean skip(Step step) {
		handlePathTrimming(step);
		this.entries.add(NO_ENTRY);
		return true;
	}

	private boolean enter(Step step, Scope start) {
		handlePathTrimming(step);
		this.entries.add(new Entry(start));
		return true;
	}

	private void handlePathTrimming(Step step) {
		if (this.path.getSteps()[0] == step) {
			// This can happen when some PathFragment is absolute.
			// In this case the beginning of the Path gets trimmed
			// and step index starts over from zero.
			this.entries.clear();
		}
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
