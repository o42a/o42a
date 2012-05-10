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
package org.o42a.backend.constant.data.struct;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.op.SystemStore;
import org.o42a.backend.constant.code.rec.RecStore;
import org.o42a.codegen.data.*;


public abstract class StructStore {

	private static final StructStore[] ALLOC_STORES;

	static {

		final AllocClass[] allocClasses = AllocClass.values();

		ALLOC_STORES = new StructStore[allocClasses.length];
		for (int i = 0; i < allocClasses.length; ++i) {
			ALLOC_STORES[i] = new AllocStructStore(allocClasses[i]);
		}
	}

	public static StructStore autoStructStore() {
		return new AutoStructStore();
	}

	public static StructStore allocStructStore(AllocClass allocClass) {
		return ALLOC_STORES[allocClass.ordinal()];
	}

	private final AllocClass allocClass;

	public StructStore(AllocClass allocClass) {
		this.allocClass = allocClass;
	}

	public final AllocClass getAllocClass() {
		return this.allocClass;
	}

	public abstract RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field);

	public abstract SystemStore systemStore(
			CStruct<?> struct,
			SystemData field);

	public abstract StructStore subStore(CStruct<?> struct, Type<?> field);

	@Override
	public String toString() {
		return String.valueOf(this.allocClass);
	}

	protected abstract Usable<SimpleUsage> init(
			CStruct<?> struct,
			Usable<SimpleUsage> allUses);

}
