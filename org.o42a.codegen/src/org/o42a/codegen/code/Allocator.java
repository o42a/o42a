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

import static org.o42a.codegen.code.InternalDisposal.NO_DISPOSAL;
import static org.o42a.codegen.data.AllocPlace.autoAllocPlace;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public abstract class Allocator extends Block {

	private static final ID ALLOC_ID = ID.id("__alloc__");
	private static final ID DISPOSE_ID = ID.id("__dispose__");

	private final AllocPlace allocPlace = autoAllocPlace(this);
	private Code allocation;
	private InternalDisposal lastDisposal = NO_DISPOSAL;
	private InternalDisposal disposal = NO_DISPOSAL;
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
		assert !getFunction().isComplete() :
			"Can not add disposals to already built function";

		final DedupDisposal dedupDisposal = new DedupDisposal(this, disposal);

		if (this.disposal == NO_DISPOSAL) {
			this.disposal = dedupDisposal;
		} else {
			this.disposal = new CombinedDisposal(this.disposal, dedupDisposal);
		}
	}

	public final void addLastDisposal(Disposal disposal) {
		assert disposal != null :
			"Disposal not specified";
		assert !getFunction().isDone() :
			"Can not add disposals to already built function";

		final DedupDisposal dedupDisposal = new DedupDisposal(this, disposal);

		if (this.lastDisposal == NO_DISPOSAL) {
			this.lastDisposal = dedupDisposal;
		} else {
			this.lastDisposal =
					new CombinedDisposal(dedupDisposal, this.lastDisposal);
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
		return this.allocation = inset(ALLOC_ID);
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

	abstract InternalDisposal disposal();

	final void dispose(Code code) {
		getFunction().addCompleteListener(new Disposer(this, code));
		afterDispose(code);
	}

	private <T> T find(Class<? extends T> klass) {
		if (this.data == null) {
			return null;
		}
		return klass.cast(this.data.get(klass));
	}

	private void disposeIn(Code code) {
		this.disposal.dispose(code);
		this.lastDisposal.dispose(code);
		disposal().dispose(code);
	}

	private void afterDispose(Code code) {
		this.disposal.afterDispose(code);
		this.lastDisposal.afterDispose(code);
		disposal().afterDispose(code);
	}

	private static final class Disposer
			implements FunctionCompleteListener {

		private final Allocator allocator;
		private final Code dispose;

		Disposer(Allocator allocator, Code code) {
			this.allocator = allocator;
			this.dispose = code.inset(DISPOSE_ID);
		}

		@Override
		public void functionComplete(Function<?> function) {
			this.allocator.disposeIn(this.dispose);
		}

	}

}
