/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ir;

import static org.o42a.core.ir.op.NoArgFunc.NO_ARG;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.util.string.ID;


public abstract class CodeBuilder {

	private static final SignalGC SIGNAL_GC = new SignalGC();

	public static CodeBuilder defaultBuilder(Function<?> function, Obj object) {
		return new DefaultBuilder(function, object);
	}

	private final CompilerContext context;
	private final Function<?> function;
	private final CodeObjects objects;
	private CodeLocals locals;
	private boolean signalGC;
	private int nameSeq;

	protected CodeBuilder(CompilerContext context, Function<?> function) {
		this.context = context;
		this.function = function;
		this.objects = new CodeObjects(this);
	}

	public final Generator getGenerator() {
		return this.function.getGenerator();
	}

	public final CompilerContext getContext() {
		return this.context;
	}

	public final Function<?> getFunction() {
		return this.function;
	}

	public final ObjectSignature<?> getObjectSignature() {
		return (ObjectSignature<?>) this.function.getSignature();
	}

	public abstract HostOp host();

	public abstract ObjectOp owner();

	public final CodeObjects objects() {
		return this.objects;
	}

	public final CodeLocals locals() {
		if (this.locals != null) {
			return this.locals;
		}
		return this.locals = new CodeLocals();
	}

	public final ID nextId() {
		return getFunction().getId().anonymous(++this.nameSeq);
	}

	public final CodeDirs dirs(Block code, CodePos falseDir) {
		return CodeDirs.codeDirs(this, code, falseDir);
	}

	public final void signalGC() {
		if (this.signalGC) {
			return;
		}
		this.signalGC = true;
		getFunction().addLastDisposal(SIGNAL_GC);
	}

	public ValOp voidVal(Code code) {
		return ValueType.VOID
				.ir(getGenerator()).staticsIR()
				.valPtr(Void.VOID)
				.op(null, code)
				.op(this, Val.VOID_VAL);
	}

	private static final class SignalGC implements Disposal {

		@Override
		public void dispose(Code code) {
			code.getGenerator()
			.externalFunction()
			.link("o42a_gc_signal", NO_ARG)
			.op(null, code)
			.call(code);
		}

		@Override
		public String toString() {
			return "SignalGC";
		}

	}

}
