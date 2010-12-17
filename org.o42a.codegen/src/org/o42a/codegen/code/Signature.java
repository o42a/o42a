/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.backend.*;
import org.o42a.codegen.data.SignatureDataBase;


public abstract class Signature<F extends Func> extends SignatureDataBase<F> {

	private SignatureAllocation<F> allocation;
	private final String result;
	private final String name;
	private final String args;

	public Signature(String result, String name, String args) {
		this.result = result;
		this.name = name;
		this.args = args;
	}

	public final String getResult() {
		return this.result;
	}

	public final String getName() {
		return this.name;
	}

	public final String getArgs() {
		return this.args;
	}

	public final SignatureAllocation<F> getAllocation() {
		return this.allocation;
	}

	public abstract F op(FuncCaller caller);

	@Override
	public String toString() {
		return this.result + ' ' + this.name + '(' + this.args + ')';
	}

	protected abstract void write(SignatureWriter<F> writer);

	@Override
	protected final Signature<F> allocate(CodeBackend backend) {

		final SignatureAllocation<F> allocation = getAllocation();

		if (allocation != null) {
			return this;
		}

		final SignatureWriter<F> writer = backend.addSignature(this);

		write(writer);

		this.allocation = writer.done();

		return this;
	}

}
