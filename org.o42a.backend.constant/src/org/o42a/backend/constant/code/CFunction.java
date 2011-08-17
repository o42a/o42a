/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.backend.CodeCallback;


public class CFunction<F extends Func<F>> extends CCode<Function<F>> {

	private final CodeCallback callback;

	CFunction(
			ConstBackend backend,
			Function<F> function,
			CodeCallback callback,
			Function<F> underlying) {
		super(backend, null, function, underlying);
		this.callback = callback;
	}

	public final CodeCallback getCallback() {
		return this.callback;
	}

}
