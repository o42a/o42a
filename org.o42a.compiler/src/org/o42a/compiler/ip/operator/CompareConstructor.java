/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.core.Distributor;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class CompareConstructor extends ComparisonConstructor {

	private byte error;

	public CompareConstructor(BinaryNode node, Distributor distributor) {
		super(node, distributor);
	}

	protected CompareConstructor(
			CompareConstructor prototype,
			Reproducer reproducer) {
		super(prototype, reproducer);
		prototype.checkError();
		this.error = prototype.error;
	}

	@Override
	public abstract CompareConstructor reproduce(Reproducer reproducer);

	@Override
	protected final boolean result(Value<?> value) {
		if (checkError()) {
			return false;
		}

		final Long compareResult =
			ValueType.INTEGER.cast(value).getDefiniteValue();

		return compare(compareResult);
	}

	protected abstract boolean compare(long compareResult);

	@Override
	protected final void write(
			CodeDirs dirs,
			ValOp result,
			ValOp comparisonVal) {

		final Code code = dirs.code();

		if (checkError()) {
			dirs.goWhenFalse(code);
			return;
		}

		final RecOp<Int64op> comparisonPtr =
			comparisonVal.rawValue(code.id("cmp_ptr"), code);
		final Int64op comparisonValue =
			comparisonPtr.load(code.id("cmp_value"), code);

		write(dirs, comparisonValue);
		result.storeVoid(code);
	}

	protected abstract void write(CodeDirs dirs, Int64op comparisonValue);

	private boolean checkError() {
		if (this.error != 0) {
			return this.error > 0;
		}

		final Resolution resolution = getPhrase().getResolution();

		if (resolution.isError()) {
			this.error = 1;
			return true;
		}

		final ValueType<?> valueType = getPhrase().getValueType();

		if (valueType != ValueType.INTEGER) {
			getLogger().error(
					"comparison_not_integer",
					this,
					"Comparison expected to return integer value");
			this.error = 1;
			return true;
		}

		this.error = -1;

		return false;
	}
}
