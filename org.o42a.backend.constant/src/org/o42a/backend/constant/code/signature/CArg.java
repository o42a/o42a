/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code.signature;

import org.o42a.codegen.code.Arg;
import org.o42a.codegen.code.SignatureBuilder;
import org.o42a.codegen.code.op.Op;


abstract class CArg<O extends Op> {

	private final Arg<O> underlying;

	public CArg(Arg<O> underlying) {
		this.underlying = underlying;
	}

	public final String getName() {
		return this.underlying.getName();
	}

	public abstract Arg<O> add(SignatureBuilder builder);

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

}
