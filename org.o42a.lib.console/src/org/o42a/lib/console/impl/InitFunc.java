/*
    Console Module
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.lib.console.impl;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.util.string.ID;


public class InitFunc extends Func<InitFunc> {

	public static final Signature INIT = new Signature();

	private InitFunc(FuncCaller<InitFunc> caller) {
		super(caller);
	}

	public void init(Code code) {
		invoke(null, code, INIT.result());
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<InitFunc> {

		private Return<Void> result;

		private Signature() {
			super(ID.id("InitF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		@Override
		public InitFunc op(FuncCaller<InitFunc> caller) {
			return new InitFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
		}

	}

}
