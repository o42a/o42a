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

import static org.o42a.core.ir.object.CtrOp.CTR_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;


public abstract class CodeBuilder {

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

	public static CodeBuilder hostlessBuilder(
			CompilerContext context,
			Function<?> function) {
		return new HostlessBuilder(context, function);
	}

	private final CompilerContext context;
	private final Function<?> function;
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

	public final CodeDirs falseWhenUnknown(
			Block code,
			CodePos falseDir) {
		return CodeDirs.falseWhenUnknown(this, code, falseDir);
	}

	public final CodeDirs splitWhenUnknown(
			Block code,
			CodePos falseDir,
			CodePos unknownDir) {
		return CodeDirs.splitWhenUnknown(this, code, falseDir, unknownDir);
	}

	public ObjectOp newObject(
			CodeDirs dirs,
			ObjectOp scope,
			ObjectOp ancestor,
			Obj sample) {

		final AllocationDirs alloc = dirs.allocate("new_object");
		final CtrOp.Op ctr = alloc.allocate(alloc.id("ctr"), CTR_TYPE);
		final CodeDirs subDirs = alloc.dirs();
		final ObjectOp result = ctr.op(this).newObject(
				subDirs,
				scope,
				ancestor,
				sample.ir(getGenerator()).op(this, subDirs.code()));

		alloc.done();

		return result;
	}

	public ObjectOp objectAncestor(CodeDirs dirs, Obj object) {

		final TypeRef ancestorType = object.type().getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final RefOp ancestor = ancestorType.op(dirs, host());

		return ancestor.target(dirs).materialize(dirs);
	}

}
