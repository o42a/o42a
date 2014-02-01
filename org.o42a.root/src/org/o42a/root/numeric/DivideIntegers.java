/*
    Root Object Definition
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
package org.o42a.root.numeric;

import static org.o42a.codegen.code.op.NumOp.DIV_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.object.Obj;
import org.o42a.root.Root;


@SourcePath(relativeTo = Root.class, value = "integers/divide.o42a")
public final class DivideIntegers extends BinaryInteger {

	public DivideIntegers(Obj owner, AnnotatedSources sources) {
		super(owner, sources, "left operand", "right operand");
	}

	@Override
	protected long calculate(long left, long right) {
		return left / right;
	}

	@Override
	protected Int64op write(Code code, Int64op left, Int64op right) {
		return left.div(DIV_ID, code, right);
	}

}
