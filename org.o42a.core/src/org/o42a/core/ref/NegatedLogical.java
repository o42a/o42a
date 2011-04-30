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
package org.o42a.core.ref;

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;

import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class NegatedLogical extends Logical {

	NegatedLogical(Logical original) {
		super(original, original.getScope());
	}

	@Override
	public LogicalValue getConstantValue() {
		return negate().getConstantValue().negate();
	}

	@Override
	public LogicalValue logicalValue(Scope scope) {
		assertCompatible(scope);
		return negate().logicalValue(scope).negate();
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Logical reproduced = negate().reproduce(reproducer);

		if (reproduced == null) {
			return null;
		}

		return reproduced.negate();
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final Code code = dirs.code();
		final Code isFalse = code.addBlock("is_false");
		final CodeDirs negatedDirs =
			falseWhenUnknown(code, isFalse.head())
			.begin("not", "Logical NOT: " + this);

		negate().write(negatedDirs, host);
		negatedDirs.end();
		dirs.goWhenFalse(code);

		if (isFalse.exists()) {
			isFalse.go(code.tail());
		}
	}

	@Override
	public String toString() {
		return "--" + negate();
	}

	@Override
	protected void fullyResolve() {
		negate().resolveAll();
	}

}
