/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;
import static org.o42a.codegen.data.AllocPlace.staticAllocPlace;
import static org.o42a.codegen.data.AllocPlace.unknownAllocPlace;

import org.o42a.codegen.code.Code;


public enum AllocClass {

	UNKNOWN_ALLOC_CLASS() {

		@Override
		public AllocPlace allocPlace(Code code) {
			return unknownAllocPlace();
		}

	},

	AUTO_ALLOC_CLASS() {

		@Override
		public AllocPlace allocPlace(Code code) {
			return code.getClosestAllocator().getAllocPlace();
		}

	},

	STATIC_ALLOC_CLASS() {

		@Override
		public AllocPlace allocPlace(Code code) {
			return staticAllocPlace();
		}

	},

	CONSTANT_ALLOC_CLASS() {

		@Override
		public AllocPlace allocPlace(Code code) {
			return constantAllocPlace();
		}

	};

	public final boolean isStatic() {
		return ordinal() >= STATIC_ALLOC_CLASS.ordinal();
	}

	public final boolean isAuto() {
		return this == AUTO_ALLOC_CLASS;
	}

	public abstract AllocPlace allocPlace(Code code);

}
