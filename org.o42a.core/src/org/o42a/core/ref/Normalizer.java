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
package org.o42a.core.ref;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Scope;
import org.o42a.util.collect.Chain;
import org.o42a.util.fn.Cancelable;


public final class Normalizer {

	private final RootNormalizer root;
	private final Chain<Normal> cancelables =
			new Chain<>(Normal::getNext, Normal::setNext);
	private boolean cancelled;

	Normalizer(RootNormalizer root) {
		this.root = root;
	}

	private Normalizer(Normalizer parent) {
		this.root = parent.getRoot();
		if (parent.isCancelled()) {
			this.cancelled = true;
		} else {
			new NestedNormalizer(parent, this);
		}
	}

	public final RootNormalizer getRoot() {
		return this.root;
	}

	public final Analyzer getAnalyzer() {
		return getRoot().getAnalyzer();
	}

	public final Scope getNormalizedScope() {
		return getRoot().getNormalizedScope();
	}

	public final boolean isCancelled() {
		return this.cancelled;
	}

	public final Normalizer createNested() {
		return new Normalizer(this);
	}

	public final void cancelAll() {
		if (this.cancelled) {
			return;
		}
		this.cancelled = true;

		Normal normal = this.cancelables.getFirst();

		while (normal != null) {
			normal.cancelable().cancel();

			final Normal next = normal.getNext();

			normal.setNext(null);
			normal = next;
		}

		this.cancelables.clear();
	}

	@Override
	public String toString() {
		if (this.root == null) {
			return super.toString();
		}
		return "Normalizer[to " + getNormalizedScope()
				+ " by " + getAnalyzer() + ']';
	}

	final void addNormal(Normal normal) {
		if (this.cancelled) {
			normal.cancelable().cancel();
		} else {
			this.cancelables.add(normal);
		}
	}

	private static final class NestedNormalizer
			extends Normal
			implements Cancelable {

		private final Normalizer nested;

		NestedNormalizer(Normalizer parent, Normalizer nested) {
			super(parent);
			this.nested = nested;
		}

		@Override
		public void cancel() {
			this.nested.cancelAll();
		}

		@Override
		public String toString() {
			if (this.nested == null) {
				return super.toString();
			}
			return "Nested" + this.nested;
		}

		@Override
		protected Cancelable cancelable() {
			return this;
		}

	}

}
