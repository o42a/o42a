/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.codegen.debug.DebugEnvOp.DEBUG_ENV_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureAllocation;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.debug.DebugEnvOp;
import org.o42a.util.ArrayUtil;


public abstract class Signature<F extends Func> {

	private static final Arg<?>[] NO_ARGS = new Arg<?>[0];

	private CodeId codeId;
	private Generator generator;
	private SignatureAllocation<F> allocation;
	private Return<?> ret;
	private Arg<DebugEnvOp> debugEnv;
	private Arg<?>[] args = NO_ARGS;

	public boolean isDebuggable() {
		return true;
	}

	public Return<?> returns(Generator generator) {
		allocate(generator);
		return this.ret;
	}

	public final CodeId getId() {
		if (this.codeId != null) {
			return this.codeId;
		}
		return codeId(CodeIdFactory.DEFAULT_CODE_ID_FACTORY);
	}

	public final CodeId codeId(CodeIdFactory factory) {
		if (this.codeId != null && this.codeId.compatibleWith(factory)) {
			return this.codeId;
		}
		return this.codeId = buildCodeId(factory);
	}

	public final CodeId codeId(Generator generator) {
		return codeId(generator.getCodeIdFactory());
	}

	public final Arg<?>[] args(Generator generator) {
		allocate(generator);
		return this.args;
	}

	public final SignatureAllocation<F> allocation(Generator generator) {
		allocate(generator);
		return this.allocation;
	}

	public final Arg<DebugEnvOp> debugEnv() {
		return this.debugEnv;
	}

	public abstract F op(FuncCaller<F> caller);

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (this.ret != null) {
			out.append(this.ret.typeName()).append(' ');
		} else {
			out.append("? ");
		}
		out.append(getId());
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

	protected abstract CodeId buildCodeId(CodeIdFactory factory);

	protected abstract void build(SignatureBuilder builder);

	final Signature<F> allocate(Generator generator) {
		if (this.allocation != null) {
			if (this.generator == generator) {
				return this;
			}
			this.allocation = null;
			this.debugEnv = null;
			this.ret = null;
			this.args = NO_ARGS;
		}
		this.generator = generator;

		final SignatureWriter<F> writer =
			generator.getFunctions().codeBackend().addSignature(this);
		final SignatureBuilder builder = new SignatureBuilder(this, writer);

		if (generator.isDebug() && isDebuggable()) {
			this.debugEnv = builder.addPtr("__o42a_dbg_env__", DEBUG_ENV_TYPE);
		}

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

	@SuppressWarnings("unchecked")
	final void addArg(Arg<?> arg) {
		this.args = ArrayUtil.append(this.args, arg);
	}

}
