/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.operator;

import static org.o42a.core.value.Value.voidValue;

import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


final class EqualsOperator extends ComparisonOperator {

	EqualsOperator() {
		super(ClauseId.EQUALS);
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return ValueStruct.VOID;
	}

	@Override
	public boolean result(Value<?> value) {
		return !value.getKnowledge().isFalse();
	}

	@Override
	public ValOp write(ValDirs dirs, ValOp comparisonVal) {
		return voidValue().op(dirs.getBuilder(), dirs.code());
	}

}
