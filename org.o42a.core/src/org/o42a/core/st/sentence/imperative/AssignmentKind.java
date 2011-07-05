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
package org.o42a.core.st.sentence.imperative;

import static org.o42a.core.ir.local.StOp.noStOp;

import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;


enum AssignmentKind {

	ASSIGNMENT_ERROR() {

		@Override
		public StOp op(LocalBuilder builder, AssignmentStatement assignment) {
			return noStOp(builder, assignment);
		}

	},

	VARIABLE_ASSIGNMENT() {

		@Override
		public StOp op(LocalBuilder builder, AssignmentStatement assignment) {
			return new VariableAssignmentOp(builder, assignment);
		}

	},

	ARRAY_ASSIGNMENT() {

		@Override
		public StOp op(LocalBuilder builder, AssignmentStatement assignment) {
			return new ArrayAssignmentOp(builder, assignment);
		}

	};

	public final boolean isError() {
		return this == ASSIGNMENT_ERROR;
	}

	public abstract StOp op(
			LocalBuilder builder,
			AssignmentStatement assignment);

}
