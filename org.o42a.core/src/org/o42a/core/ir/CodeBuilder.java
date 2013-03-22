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

import static org.o42a.core.ir.object.op.CtrOp.CTR_ID;
import static org.o42a.core.ir.object.op.CtrOp.CTR_TYPE;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.op.NoArgFunc.NO_ARG;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.util.string.ID;


public abstract class CodeBuilder {

	private static final SignalGC SIGNAL_GC = new SignalGC();

	public static CodeBuilder defaultBuilder(Function<?> function, Obj object) {
		return new DefaultBuilder(function, object);
	}

	public static ObjectOp objectAncestor(
			CodeDirs dirs,
			HostOp host,
			Obj object) {

		final TypeRef ancestorType = object.type().getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final RefOp ancestor = ancestorType.op(host);

		return ancestor.target(dirs)
				.materialize(dirs, tempObjHolder(dirs.getAllocator()));
	}

	private final CompilerContext context;
	private final Function<?> function;
	private boolean signalGC;
	private int nameSeq;
	private CodeLocals locals;

	protected CodeBuilder(CompilerContext context, Function<?> function) {
		this.context = context;
		this.function = function;
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

	public final ObjectOp newObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectOp ancestor,
			Obj sample) {
		return newObject(
				dirs,
				holder,
				owner,
				ancestor == null
				? null : ancestor.objectType(dirs.code()).ptr(),
				sample);
	}

	public final ObjectOp newObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectIRTypeOp ancestor,
			Obj sample) {

		final Code alloc = dirs.code().getAllocator().allocation();
		final CtrOp.Op ctr = alloc.allocate(CTR_ID, CTR_TYPE);
		final ObjectOp newObject = ctr.op(this).newObject(
				dirs,
				holder,
				owner,
				ancestor,
				sample.ir(getGenerator()).op(this, dirs.code()));

		newObject.fillDeps(dirs, sample);

		return newObject;
	}

	public final ObjectOp objectAncestor(CodeDirs dirs, Obj object) {
		return objectAncestor(dirs, host(), object);
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
