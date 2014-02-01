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
package org.o42a.backend.constant.code.op;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.AllocPlace;


public abstract class SystemStore {

	private static final AllocSystemStore[] ALLOC_STORES =
			new AllocSystemStore[AllocClass.values().length];

	static {
		ALLOC_STORES[AllocClass.UNKNOWN_ALLOC_CLASS.ordinal()] =
				new AllocSystemStore(AllocPlace.unknownAllocPlace());
		ALLOC_STORES[AllocClass.STATIC_ALLOC_CLASS.ordinal()] =
				new AllocSystemStore(AllocPlace.staticAllocPlace());
		ALLOC_STORES[AllocClass.CONSTANT_ALLOC_CLASS.ordinal()] =
				new AllocSystemStore(AllocPlace.constantAllocPlace());
	}

	public static SystemStore allocSystemStore(AllocPlace allocPlace) {

		final AllocSystemStore store =
				ALLOC_STORES[allocPlace.getAllocClass().ordinal()];

		if (store != null) {
			return store;
		}

		return new AllocSystemStore(allocPlace);
	}

	private final AllocPlace allocPlace;

	public SystemStore(AllocPlace allocPlace) {
		this.allocPlace = allocPlace;
	}

	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	@Override
	public String toString() {
		return String.valueOf(this.allocPlace);
	}

	protected abstract Usable<SimpleUsage> init(
			SystemCOp op,
			Usable<SimpleUsage> allUses);

	protected static final Usable<SimpleUsage> init(
			SystemStore store,
			SystemCOp op,
			Usable<SimpleUsage> allUses) {
		return store.init(op, allUses);
	}

}
