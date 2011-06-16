/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ir.value;

import org.o42a.codegen.code.Code;


public enum ValStoreMode {

	TEMP_VAL_STORE() {

		@Override
		void store(Code code, ValOp target, Val value) {

			final ValueTypeIR<?> ir =
				target.getValueType().ir(code.getGenerator());

			ir.store(code, target, value);
		}

		@Override
		void store(Code code, ValOp target, ValOp value) {

			final ValueTypeIR<?> ir =
				target.getValueType().ir(code.getGenerator());

			ir.store(code, target, value);
		}

	},

	INITIAL_VAL_STORE() {

		@Override
		void store(Code code, ValOp target, Val value) {

			final ValueTypeIR<?> ir =
				target.getValueType().ir(code.getGenerator());

			ir.initialize(code, target, value);
		}

		@Override
		void store(Code code, ValOp target, ValOp value) {

			final ValueTypeIR<?> ir =
				target.getValueType().ir(code.getGenerator());

			ir.initialize(code, target, value);
		}

	},

	ASSIGNMENT_VAL_STORE() {

		@Override
		void store(Code code, ValOp target, Val value) {

			final ValueTypeIR<?> ir =
				target.getValueType().ir(code.getGenerator());

			ir.assign(code, target, value);
		}

		@Override
		void store(Code code, ValOp target, ValOp value) {

			final ValueTypeIR<?> ir =
				target.getValueType().ir(code.getGenerator());

			ir.assign(code, target, value);
		}

	};

	abstract void store(Code code, ValOp target, Val value);

	abstract void store(Code code, ValOp target, ValOp value);

}
