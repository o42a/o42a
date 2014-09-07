/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public class ExtFunc<R> extends Func<ExtFunc<R>> {

	ExtFunc(FuncCaller<ExtFunc<R>> caller) {
		super(caller);
	}

	public final SimpleSignature<R> getSimpleSignature() {
		return (SimpleSignature<R>) getSignature();
	}

	public final R call(
			ID id,
			Code code,
			Op... args) {
		return invoke(id, code, getSimpleSignature().result(), args);
	}

}
