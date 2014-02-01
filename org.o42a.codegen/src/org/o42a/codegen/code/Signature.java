/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureAllocation;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;


public abstract class Signature<F extends Func<F>> {

	private static final Arg<?>[] NO_ARGS = new Arg<?>[0];

	private final ID id;
	private Generator generator;
	private SignatureAllocation<F> allocation;
	private Return<?> ret;
	private Arg<?>[] args = NO_ARGS;

	public Signature(ID id) {
		this.id = id;
	}

	public boolean isDebuggable() {
		return true;
	}

	public Return<?> returns(Generator generator) {
		allocate(generator);
		return this.ret;
	}

	public final ID getId() {
		return this.id;
	}

	public final Arg<?>[] args(Generator generator) {
		allocate(generator);
		return this.args;
	}

	public final SignatureAllocation<F> allocation(Generator generator) {
		allocate(generator);
		return this.allocation;
	}

	public abstract F op(FuncCaller<F> caller);

	@Override
	public String toString() {
		return toString(getId().toString());
	}

	public String toString(String name) {

		final StringBuilder out = new StringBuilder();

		if (this.ret != null) {
			out.append(this.ret.typeName()).append(' ');
		} else {
			out.append("? ");
		}
		out.append(name);
		out.append('(');
		for (int i = 0; i < this.args.length; ++i) {
			if (i != 0) {
				out.append(", ");
			}
			out.append(this.args[i]);
		}
		out.append(')');

		return out.toString();
	}

	protected abstract void build(SignatureBuilder builder);

	final Signature<F> allocate(Generator generator) {
		if (this.allocation != null) {
			if (this.generator == generator) {
				return this;
			}
			this.allocation = null;
			this.ret = null;
			this.args = NO_ARGS;
		}
		this.generator = generator;

		final SignatureWriter<F> writer =
				generator.getFunctions().codeBackend().addSignature(this);
		final SignatureBuilder builder = new SignatureBuilder(this, writer);

		build(builder);

		assert this.ret != null :
			"Signature does not declare a return type: " + this;

		this.allocation = writer.done();

		return this;
	}

	final Generator getGenerator() {
		return this.generator;
	}

	final void setReturn(Return<?> ret) {
		assert this.ret == null :
			"Return type of " + this + " already set. Can not change to " + ret;
		this.ret = ret;
	}

	final Return<?> getReturn() {
		return this.ret;
	}

	final Arg<?>[] getArgs() {
		return this.args;
	}

	final void addArg(Arg<?> arg) {
		this.args = ArrayUtil.append(this.args, arg);
	}

}
