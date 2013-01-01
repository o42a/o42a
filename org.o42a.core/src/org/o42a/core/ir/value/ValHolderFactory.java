/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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


public interface ValHolderFactory {

	ValHolderFactory NO_VAL_HOLDER = new ValHolderFactory() {

		@Override
		public ValHolder createValHolder(ValOp value) {
			return ValHolder.NO_VAL_HOLDER;
		}

	};

	ValHolderFactory TEMP_VAL_HOLDER = new ValHolderFactory() {

		@Override
		public ValHolder createValHolder(ValOp value) {
			return value.getDesc().tempValHolder(value);
		}

	};

	ValHolderFactory VAL_TRAP = new ValHolderFactory() {

		@Override
		public ValHolder createValHolder(ValOp value) {
			return value.getDesc().valTrap(value);
		}

	};

	ValHolder createValHolder(ValOp value);

}
