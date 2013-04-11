/*
    Compiler Commons
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.common.ref.cmp;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class NotEqualsOperator extends ComparisonOperator {

	NotEqualsOperator() {
		super(ClauseId.EQUALS);
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public boolean result(Value<?> value) {
		return value.getKnowledge().isFalse();
	}

	@Override
	public ValOp writeComparison(ValDirs dirs, RefOp cmp) {

		final Block code = dirs.code();
		final Block notEqual = code.addBlock("not_equal");

		cmp.writeCond(
				dirs.getBuilder().dirs(code, notEqual.head()));

		code.go(dirs.falseDir());
		if (notEqual.exists()) {
			notEqual.go(code.tail());
		}

		return dirs.getBuilder().voidVal(code);
	}

	@Override
	public ValOp inlineComparison(ValDirs dirs, HostOp host, InlineValue cmp) {

		final Block code = dirs.code();
		final Block notEqual = code.addBlock("not_equal");

		cmp.writeCond(
				dirs.getBuilder().dirs(code, notEqual.head()),
				host);

		code.go(dirs.falseDir());
		if (notEqual.exists()) {
			notEqual.go(code.tail());
		}

		return dirs.getBuilder().voidVal(code);
	}

	@Override
	public ValOp write(ValDirs dirs, ValOp comparisonVal) {
		return dirs.getBuilder().voidVal(dirs.code());
	}

}
