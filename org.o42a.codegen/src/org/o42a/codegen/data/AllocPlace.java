/*
    Compiler Code Generator
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.codegen.data;

import static org.o42a.codegen.data.AllocClass.*;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;


public final class AllocPlace {

	private static final AllocPlace UNKNOWN_ALLOC_PLACE =
			new AllocPlace(UNKNOWN_ALLOC_CLASS, null);
	private static final AllocPlace STATIC_ALLOC_PLACE =
			new AllocPlace(STATIC_ALLOC_CLASS, null);
	private static final AllocPlace CONSTANT_ALLOC_PLACE =
			new AllocPlace(CONSTANT_ALLOC_CLASS, null);

	public static AllocPlace defaultAllocPlace() {
		return UNKNOWN_ALLOC_PLACE;
	}

	public static AllocPlace unknownAllocPlace() {
		return UNKNOWN_ALLOC_PLACE;
	}

	public static AllocPlace autoAllocPlace(Allocator allocator) {
		return new AllocPlace(AUTO_ALLOC_CLASS, allocator);
	}

	public static AllocPlace staticAllocPlace() {
		return STATIC_ALLOC_PLACE;
	}

	public static AllocPlace constantAllocPlace() {
		return CONSTANT_ALLOC_PLACE;
	}

	private final AllocClass allocClass;
	private final Allocator allocator;

	private AllocPlace(AllocClass allocClass, Allocator allocator) {
		this.allocClass = allocClass;
		this.allocator = allocator;
	}

	public final AllocClass getAllocClass() {
		return this.allocClass;
	}

	public final Allocator getAllocator() {
		return this.allocator;
	}

	public final boolean isStatic() {
		return getAllocClass().isStatic();
	}

	public final boolean isAuto() {
		return getAllocClass().isAuto();
	}

	public final boolean ensureAccessibleFrom(Code code) {
		assert accessibleFrom(code) :
			this + " is not accessible from " + code;
		return true;
	}

	public boolean accessibleFrom(Code code) {

		final Allocator placeAllocator = getAllocator();

		if (placeAllocator == null) {
			return true;
		}

		final Allocator codeAllocator = code.getClosestAllocator();

		if (codeAllocator == placeAllocator) {
			return true;
		}

		final Code enclosing = codeAllocator.getEnclosing();

		if (enclosing == null) {
			return false;
		}

		return accessibleFrom(enclosing);
	}

	@Override
	public String toString() {
		if (this.allocClass == null) {
			return super.toString();
		}
		if (this.allocator == null) {
			return this.allocClass.toString();
		}
		return this.allocClass + "@" + this.allocator;
	}

}
