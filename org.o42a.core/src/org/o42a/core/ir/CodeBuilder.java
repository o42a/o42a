/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.op.CtrOp.CTR_TYPE;
import static org.o42a.core.ir.op.NoArgFunc.NO_ARG;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.core.Scope;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.ObjectPrecision;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;


public abstract class CodeBuilder {

	private static final SignalGC SIGNAL_GC = new SignalGC();

	public static CodeBuilder defaultBuilder(Function<?> function, Obj object) {
		return new DefaultBuilder(function, object);
	}

	public static CodeBuilder codeBuilder(
			Function<? extends ObjectFunc<?>> function,
			CodePos exit,
			Scope scope,
			ObjectPrecision hostPrecision) {

		final Generator generator = function.getGenerator();
		final LocalScope local = scope.toLocal();

		if (local != null) {
			return new LocalBuilder(function, local.ir(generator));
		}

		final Obj scopeObject = scope.toObject();

		assert scopeObject != null :
			"Unsupported scope: " + scope;

		return new ObjBuilder(
				function,
				exit,
				scopeObject.ir(generator).getBodyType(),
				scopeObject,
				hostPrecision);
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

		return ancestor.target(dirs).materialize(dirs);
	}

	private final CompilerContext context;
	private final Function<?> function;
	private boolean signalGC;
	private int nameSeq;

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

	public final CodeId nextId() {
		return getFunction().getId().anonymous(++this.nameSeq);
	}

	public final CodeDirs dirs(Block code, CodePos falseDir) {
		return CodeDirs.codeDirs(this, code, falseDir);
	}

	public ObjectOp newObject(
			CodeDirs dirs,
			ObjectOp owner,
			ObjectOp ancestor,
			Obj sample) {

		final AllocationCode alloc = dirs.code().getAllocator().allocation();
		final CtrOp.Op ctr = alloc.allocate(alloc.id("ctr"), CTR_TYPE);

		return ctr.op(this).newObject(
				dirs,
				owner,
				ancestor,
				sample.ir(getGenerator()).op(this, dirs.code()));
	}

	public final ObjectOp objectAncestor(CodeDirs dirs, Obj object) {
		return objectAncestor(dirs, host(), object);
	}

	public final void signalGC() {
		if (this.signalGC) {
			return;
		}
		this.signalGC = true;

		final AllocationCode allocation =
				getFunction().getAllocator().allocation();

		allocation.addLastDisposal(SIGNAL_GC);
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
