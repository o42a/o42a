/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import static org.o42a.codegen.data.AllocPlace.autoAllocPlace;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public abstract class Allocator extends Block {

	static final Disposal NO_DISPOSAL = new NoDisposal();

	private final AllocPlace allocPlace = autoAllocPlace(this);
	private Code allocation;
	private Disposal lastDisposal = NO_DISPOSAL;
	private Disposal disposal = NO_DISPOSAL;
	private HashMap<Class<?>, Object> data;

	Allocator(Block enclosing, ID name) {
		super(enclosing, name);
	}

	Allocator(Generator generator, ID id) {
		super(generator, id);
	}

	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	public final void addDisposal(Disposal disposal) {
		assert disposal != null :
			"Disposal not specified";
		if (this.disposal == NO_DISPOSAL) {
			this.disposal = disposal;
		} else {
			this.disposal = new CombinedDisposal(this.disposal, disposal);
		}
	}

	public final void addLastDisposal(Disposal disposal) {
		assert disposal != null :
			"Disposal not specified";
		if (this.lastDisposal == NO_DISPOSAL) {
			this.lastDisposal = disposal;
		} else {
			this.lastDisposal =
					new CombinedDisposal(disposal, this.lastDisposal);
		}
	}

	@Override
	public final Allocator getAllocator() {
		return this;
	}

	public abstract Allocator getEnclosingAllocator();

	public final Code allocation() {
		if (this.allocation != null) {
			return this.allocation;
		}
		return this.allocation = inset("alloc");
	}

	public final <T> T get(Class<? extends T> klass) {

		final T found = find(klass);

		if (found != null) {
			return found;
		}

		final Allocator enclosing = getEnclosingAllocator();

		if (enclosing == null) {
			return null;
		}

		return enclosing.get(klass);
	}

	public final <T> void put(Class<? extends T> klass, T value) {
		if (this.data == null) {
			this.data = new HashMap<>(1);
		}
		this.data.put(klass, value);
	}

	protected abstract Disposal disposal();

	final void dispose(Block code) {
		this.disposal.dispose(code);
		this.lastDisposal.dispose(code);
		disposal().dispose(code);
	}

	private <T> T find(Class<? extends T> klass) {
		if (this.data == null) {
			return null;
		}
		return klass.cast(this.data.get(klass));
	}

	private static final class NoDisposal implements Disposal {

		@Override
		public void dispose(Code code) {
		}

		@Override
		public String toString() {
			return "_";
		}

	}

	private static final class CombinedDisposal implements Disposal {

		private final Disposal first;
		private final Disposal second;

		CombinedDisposal(Disposal first, Disposal second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public void dispose(Code code) {
			this.first.dispose(code);
			this.second.dispose(code);
		}

		@Override
		public String toString() {
			if (this.second == null) {
				return super.toString();
			}
			return this.first + ", " + this.second;
		}

	}

}
