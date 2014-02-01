/*
    Compiler Core
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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.util.string.ID;


public final class ValUseFunc extends Func<ValUseFunc> {

	public static final Signature VAL_USE = new Signature();

	private ValUseFunc(FuncCaller<ValUseFunc> caller) {
		super(caller);
	}

	public final void call(Code code, ValType.Op val) {
		invoke(null, code, VAL_USE.result(), val);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ValUseFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> val;

		private Signature() {
			super(ID.id("ValUseF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> val() {
			return this.val;
		}

		@Override
		public ValUseFunc op(FuncCaller<ValUseFunc> caller) {
			return new ValUseFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.val = builder.addPtr("val", VAL_TYPE);
		}

	}

}
