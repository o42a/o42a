/*
    Compiler Core
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
package org.o42a.core.ir.value.impl;

import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;


final class DefaultValueOp extends ValueOp {

	DefaultValueOp(ValueIR<?> valueIR, ObjectOp object) {
		super(valueIR, object);
	}

	@Override
	public ValOp writeValue(ValDirs dirs, ObjectOp body) {

		final ValDirs dubDirs = dirs.begin(
				"Value of "
				+ (body != null ? body + " by " + this : toString()));
		final ValOp result =
				object().objectType(dubDirs.code()).writeValue(dubDirs, body);

		dubDirs.done();

		return result;
	}

	@Override
	public void writeRequirement(CodeDirs dirs, ObjectOp body) {

		final CodeDirs subDirs;

		if (body != null) {
			subDirs = dirs.begin("obj_req", "Requirement of " + body);
		} else {
			subDirs = dirs.begin("obj_req", "Requirement");
		}

		object().objectType(subDirs.code()).writeRequirement(subDirs, body);

		subDirs.end();
	}

	@Override
	public void writeCondition(CodeDirs dirs, ObjOp body) {

		final CodeDirs subDirs;

		if (body != null) {
			subDirs = dirs.begin("obj_cond", "Condition of " + body);
		} else {
			subDirs = dirs.begin("obj_cond", "Condition");
		}

		object().objectType(subDirs.code()).writeCondition(subDirs, body);

		subDirs.end();
	}

	@Override
	protected ValOp writeClaim(ValDirs dirs, ObjectOp body) {

		final ValDirs subDirs = dirs.begin(
				"Claim of "
				+ (body != null ? body + " by " + this : toString()));
		final ValOp result =
				object()
				.objectType(subDirs.code())
				.writeClaim(subDirs, body);

		subDirs.done();

		return result;
	}

	@Override
	protected ValOp writeProposition(ValDirs dirs, ObjectOp body) {

		final ValDirs subDirs = dirs.begin(
				"Proposition of "
				+ (body != null ? body + " by " + this : toString()));
		final ValOp result =
				object()
				.objectType(subDirs.code())
				.writeProposition(subDirs, body);

		subDirs.done();

		return result;
	}

}
