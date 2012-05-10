/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012 Ruslan Lopatin

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


public abstract class SystemStore {

	private static final SystemStore[] ALLOC_STORES;

	static {

		final AllocClass[] allocClasses = AllocClass.values();

		ALLOC_STORES = new SystemStore[allocClasses.length];
		for (int i = 0; i < allocClasses.length; ++i) {
			ALLOC_STORES[i] = new AllocSystemStore(allocClasses[i]);
		}
	}

	public static SystemStore allocSystemStore(AllocClass allocClass) {
		return ALLOC_STORES[allocClass.ordinal()];
	}

	private final AllocClass allocClass;

	public SystemStore(AllocClass allocClass) {
		this.allocClass = allocClass;
	}

	public final AllocClass getAllocClass() {
		return this.allocClass;
	}

	@Override
	public String toString() {
		return String.valueOf(this.allocClass);
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
