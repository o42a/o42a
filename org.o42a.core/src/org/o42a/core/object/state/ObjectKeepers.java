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
package org.o42a.core.object.state;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.Chain;
import org.o42a.util.collect.ReadonlyIterable;
import org.o42a.util.collect.ReadonlyIterator;
import org.o42a.util.string.ID;


public abstract class ObjectKeepers {

	private final Obj object;
	private final Chain<Keeper> declaredKeepers =
			new Chain<>(Keeper::getNext, Keeper::setNext);

	public ObjectKeepers(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Iterable<Keeper> declaredKeepers() {
		return this.declaredKeepers;
	}

	public final ReadonlyIterable<Keeper> allKeepers() {
		return new AllKeepers(this);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "Keepers[" + this.object + ']';
	}

	protected final Keeper declareKeeper(
			LocationInfo location,
			Ref value,
			ID id) {

		final Keeper keeper = new Keeper(getObject(), location, value, id);

		this.declaredKeepers.add(keeper);

		return keeper;
	}

	protected abstract void keeperResolved(Keeper keeper);

	private static final class AllKeepers implements ReadonlyIterable<Keeper> {

		private final ObjectKeepers keepers;

		AllKeepers(ObjectKeepers keepers) {
			this.keepers = keepers;
		}

		@Override
		public ReadonlyIterator<Keeper> iterator() {
			return new AllKeepersIterator(this.keepers);
		}

		@Override
		public String toString() {
			if (this.keepers == null) {
				return super.toString();
			}
			return "AllKeepers[" + this.keepers.getObject() + ']';
		}

	}

	private static final class AllKeepersIterator
			extends ReadonlyIterator<Keeper> {

		private final Iterator<Scope> ascendants;
		private Keeper nextKeeper;

		AllKeepersIterator(ObjectKeepers keepers) {
			this.ascendants =
					keepers.getObject()
					.type()
					.allAscendants()
					.keySet()
					.iterator();
			nextKeeper();
		}

		@Override
		public boolean hasNext() {
			return this.nextKeeper != null;
		}

		@Override
		public Keeper next() {

			final Keeper nextKeeper = this.nextKeeper;

			if (nextKeeper == null) {
				throw new NoSuchElementException();
			}
			nextKeeper();

			return nextKeeper;
		}

		private void nextKeeper() {
			if (this.nextKeeper != null) {
				this.nextKeeper = this.nextKeeper.getNext();
				if (this.nextKeeper != null) {
					return;
				}
			}
			nextAscendantWithKeepers();
		}

		private void nextAscendantWithKeepers() {
			while (this.ascendants.hasNext()) {

				final ObjectKeepers nextKeepers =
						this.ascendants.next().toObject().keepers();

				this.nextKeeper = nextKeepers.declaredKeepers.getFirst();
				if (this.nextKeeper != null) {
					return;
				}
			}
		}

	}

}
