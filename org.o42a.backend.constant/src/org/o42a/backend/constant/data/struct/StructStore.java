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
package org.o42a.backend.constant.data.struct;

import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.STATIC_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.UNKNOWN_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;
import static org.o42a.codegen.data.AllocPlace.staticAllocPlace;
import static org.o42a.codegen.data.AllocPlace.unknownAllocPlace;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.code.op.SystemStore;
import org.o42a.backend.constant.code.rec.RecStore;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.*;


public abstract class StructStore {

	private static final AllocStructStore[] ALLOC_STORES =
			new AllocStructStore[AllocClass.values().length];

	static {
		ALLOC_STORES[UNKNOWN_ALLOC_CLASS.ordinal()] =
				new AllocStructStore(unknownAllocPlace());
		ALLOC_STORES[STATIC_ALLOC_CLASS.ordinal()] =
				new AllocStructStore(staticAllocPlace());
		ALLOC_STORES[CONSTANT_ALLOC_CLASS.ordinal()] =
				new AllocStructStore(constantAllocPlace());
	}

	public static StructStore autoStructStore(Code code) {
		return new AutoStructStore(code.getAllocator().getAllocPlace());
	}

	public static StructStore allocStructStore(AllocPlace allocPlace) {

		final AllocClass allocClass = allocPlace.getAllocClass();
		final AllocStructStore store = ALLOC_STORES[allocClass.ordinal()];

		if (store != null) {
			return store;
		}
		if (allocClass.isAuto()) {
			return new AutoStructStore(allocPlace);
		}

		return new AllocStructStore(allocPlace);
	}

	private final AllocPlace allocPlace;

	StructStore(AllocPlace allocPlace) {
		this.allocPlace = allocPlace;
	}

	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	public abstract RecStore fieldStore(CStruct<?> struct, Rec<?, ?> field);

	public abstract SystemStore systemStore(
			CStruct<?> struct,
			SystemData field);

	public abstract StructStore subStore(CStruct<?> struct, Type<?> field);

	@Override
	public String toString() {
		return String.valueOf(this.allocPlace);
	}

	protected abstract Usable<SimpleUsage> init(
			CStruct<?> struct,
			Usable<SimpleUsage> allUses);

}
