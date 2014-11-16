/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code.rec;

import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.STATIC_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.UNKNOWN_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;
import static org.o42a.codegen.data.AllocPlace.staticAllocPlace;
import static org.o42a.codegen.data.AllocPlace.unknownAllocPlace;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.op.InstrBE;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.AllocPlace;


public abstract class RecStore {

	private static final AllocRecStore[] ALLOC_STORES =
			new AllocRecStore[AllocClass.values().length];

	static {
		ALLOC_STORES[UNKNOWN_ALLOC_CLASS.ordinal()] =
				new AllocRecStore(unknownAllocPlace());
		ALLOC_STORES[STATIC_ALLOC_CLASS.ordinal()] =
				new AllocRecStore(staticAllocPlace());
		ALLOC_STORES[CONSTANT_ALLOC_CLASS.ordinal()] =
				new AllocRecStore(constantAllocPlace());
	}

	public static RecStore autoRecStore(Code code) {
		return new AllocRecStore(code.getClosestAllocator().getAllocPlace());
	}

	public static RecStore allocRecStore(AllocPlace allocPlace) {

		final AllocRecStore store =
				ALLOC_STORES[allocPlace.getAllocClass().ordinal()];

		if (store != null) {
			return store;
		}

		return new AllocRecStore(allocPlace);
	}

	private final AllocPlace allocPlace;

	public RecStore(AllocPlace allocPlace) {
		this.allocPlace = allocPlace;
	}

	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	public abstract <O extends Op> void store(
			InstrBE instr,
			RecCOp<?, O, ?> rec,
			OpBE<O> value);

	public abstract <O extends Op> void load(
			RecCOp<?, O, ?> rec,
			OpBE<O> value);

	@Override
	public String toString() {
		return String.valueOf(this.allocPlace);
	}

	protected abstract Usable<SimpleUsage> init(
			RecCOp<?, ?, ?> rec,
			Usable<SimpleUsage> allUses);

	protected static final Usable<SimpleUsage> init(
			RecStore store,
			RecCOp<?, ?, ?> rec,
			Usable<SimpleUsage> allUses) {
		return store.init(rec, allUses);
	}

}
