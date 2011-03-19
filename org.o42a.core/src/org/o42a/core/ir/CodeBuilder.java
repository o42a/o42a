/*
    Compiler Core
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
package org.o42a.core.ir;

import static org.o42a.core.ir.object.CtrOp.CTR_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.CompilerContext;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.type.TypeRef;


public class CodeBuilder {

	public static CodeBuilder codeBuilder(
			Function<? extends ObjectFunc> function,
			CodePos exit,
			Scope scope,
			ObjectPrecision hostPrecision) {

		final Generator generator = function.getGenerator();
		final LocalScope local = scope.toLocal();

		if (local != null) {
			return new LocalBuilder(function, local.ir(generator));
		}

		final Obj scopeObject = scope.getContainer().toObject();

		if (scopeObject != null) {
			return new CodeBuilder(
					function,
					exit,
					scopeObject.ir(generator).getBodyType(),
					scopeObject,
					hostPrecision);
		}

		return new CodeBuilder(function, scope);
	}

	public static CodeBuilder codeBuilder(
			CompilerContext context,
			Function<?> function) {
		return new CodeBuilder(context, function);
	}

	private final CompilerContext context;
	private final HostOp host;
	private final Function<?> function;
	private int nameSeq;

	protected CodeBuilder(
			Function<? extends ObjectFunc> function,
			CodePos exit,
			ObjectBodyIR hostIR,
			Obj hostType,
			ObjectPrecision hostPrecision) {
		this.context = hostIR.getAscendant().getContext();
		this.function = function;
		if (hostPrecision.isCompatible()) {
			this.host =
				function.arg(getObjectSignature().object())
				.to(function, hostIR)
				.op(this, hostType, hostPrecision);
		} else {
			this.host = anonymousObject(
					this,
					function.arg(getObjectSignature().object()),
					hostType)
					.cast(function, exit, hostType);
		}
	}

	protected CodeBuilder(
			Function<? extends ObjectFunc> function,
			LocalIR scopeIR) {
		this.context = scopeIR.getScope().getContext();
		this.function = function;
		this.host = scopeIR.op(this, function);
	}

	private CodeBuilder(
			Function<? extends ObjectFunc> function,
			Scope scope) {
		this.context = scope.getContext();
		this.function = function;
		this.host = scope.ir(function.getGenerator()).op(this, function);
	}

	private CodeBuilder(
			CompilerContext context,
			Function<?> function) {
		this.context = context;
		this.function = function;
		this.host = null;
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

	public HostOp host() {
		return this.host;
	}

	public final CodeId nextId() {
		return getFunction().getId().anonymous(++this.nameSeq);
	}

	public ObjectOp newObject(
			Code code,
			CodePos exit,
			ObjectOp scope,
			ObjectRefFunc ancestorFunc,
			Obj sample,
			int flags) {

		final CtrOp.Op ctr = code.allocate(CTR_TYPE);

		return ctr.op(this).newObject(
				code,
				exit,
				scope,
				ancestorFunc,
				sample.ir(getGenerator()).op(this, code),
				flags);
	}

	public final ObjectOp newObject(
			Code code,
			CodePos exit,
			Obj sample,
			int flags) {
		return newObject(
				code,
				exit,
				objectAncestor(code, exit, sample),
				sample,
				flags);
	}

	public ObjectOp newObject(
			Code code,
			CodePos exit,
			ObjectOp ancestor,
			Obj sample,
			int flags) {

		final CtrOp.Op ctr = code.allocate(CTR_TYPE);

		return ctr.op(this).newObject(
				code,
				exit,
				ancestor,
				sample.ir(getGenerator()).op(this, code),
				flags);
	}

	public ObjectOp objectAncestor(Code code, CodePos exit, Obj object) {

		final TypeRef ancestorType = object.getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final RefOp ancestor = ancestorType.op(code, exit, host());

		return ancestor.target(code, exit).materialize(code, exit);
	}

}
