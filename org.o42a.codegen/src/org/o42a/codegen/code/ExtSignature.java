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

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.util.string.ID;


public class ExtSignature<R, F extends Fn<F>> extends Signature<F> {

	private final ArrayList<Consumer<SignatureBuilder>> args;
	private final Function<SignatureBuilder, Return<R>> ret;
	private final Function<FuncCaller<F>, F> createOp;
	private Return<R> result;

	ExtSignature(
			ID id,
			ArrayList<Consumer<SignatureBuilder>> args,
			Function<SignatureBuilder, Return<R>> ret,
			Function<FuncCaller<F>, F> createOp) {
		super(id);
		this.args = args;
		this.ret = ret;
		this.createOp = createOp;
	}

	public final Return<R> result() {
		return this.result;
	}

	@Override
	public F op(FuncCaller<F> caller) {
		return this.createOp.apply(caller);
	}

	@Override
	protected void build(SignatureBuilder builder) {
		this.result = this.ret.apply(builder);
		for (Consumer<SignatureBuilder> arg : this.args) {
			arg.accept(builder);
		}
	}

}
