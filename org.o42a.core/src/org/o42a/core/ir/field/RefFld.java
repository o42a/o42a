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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.ir.op.ObjectSignature;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;


public abstract class RefFld<C extends ObjectFunc<C>> extends Fld {

	private final Obj target;
	private Obj targetAscendant;

	private boolean targetIRAllocated;
	private boolean filling;
	private boolean filledFields;
	private boolean filledAll;

	private FuncPtr<C> constructor;

	public RefFld(ObjectBodyIR bodyIR, Field<?> field) {
		super(bodyIR, field);
		this.targetIRAllocated = isOmitted();
		this.target = field.getArtifact().materialize();
	}

	public final Obj getTarget() {
		return this.target;
	}

	public final Obj getTargetAscendant() {
		return this.targetAscendant;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Type<?, C> getInstance() {
		return (Type<?, C>) super.getInstance();
	}

	public final void allocate(SubData<?> data, Obj targetAscendant) {
		getTarget().assertDerivedFrom(targetAscendant);
		this.targetAscendant = targetAscendant;
		allocate(data);
	}

	@Override
	public void targetAllocated() {
		if (this.targetIRAllocated) {
			return;
		}
		this.targetIRAllocated = true;
		// target object is allocated - fill it
		fill(false, true);
	}

	@Override
	protected abstract Type<?, C> getType();

	@Override
	public abstract RefFldOp<?, C> op(Code code, ObjOp host);

	protected void allocateMethods() {

		final FuncPtr<C> reusedConstructor = reusedConstructor();

		if (reusedConstructor != null) {
			this.constructor = reusedConstructor;
			return;
		}

		final FieldAnalysis analysis = getField().toMember().getAnalysis();

		if (!analysis.runtimeConstruction().isUsedBy(getGenerator())) {
			this.constructor = getType().constructorStub();
			return;
		}

		this.constructor = getGenerator().newFunction().create(
				getField().ir(getGenerator()).getId().detail("constructor"),
				getType().getSignature(),
				new ConstructorBuilder()).getPointer();
	}

	protected void fill() {
		getInstance().object().setNull();
		getInstance().constructor().setConstant(true).setValue(
				this.constructor);
	}

	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Code code = dirs.code();
		final RefFldOp<?, C> fld = op(code, builder.host());

		final ObjectOp result = construct(builder, dirs, fld);
		final DataOp res = result.toData(code);

		fld.ptr().object(null, code).store(code, res);
		res.returnValue(code);
	}

	protected ObjectOp construct(
			ObjBuilder builder,
			CodeDirs dirs,
			RefFldOp<?, C> fld) {

		final Artifact<?> artifact = getField().getArtifact();
		final Obj object = artifact.toObject();

		if (object != null) {

			final ObjOp host = builder.host();

			return builder.newObject(
					dirs,
					host,
					builder.objectAncestor(dirs, object),
					object);
		}

		final Link link = artifact.toLink();
		final HostOp target =
				link.getTargetRef().target(dirs, builder.host());

		return target.materialize(dirs);
	}

	protected final Obj targetType(Obj bodyType) {

		final Field<?> actual =
				bodyType.member(getField().getKey())
				.toField()
				.field(dummyUser());
		final Artifact<?> artifact = actual.getArtifact();
		final Obj object = artifact.toObject();

		if (object != null) {
			return object;
		}

		final Link link = artifact.toLink();

		return link.getTypeRef().typeObject(dummyUser());
	}

	@Override
	protected Content<Type<?, C>> content() {
		return new FldContent<Type<?, C>, C>(this);
	}

	private void fillTarget() {
		if (getTarget().getConstructionMode().isRuntime()) {
			return;
		}

		final ObjectIR targetIR = getTarget().ir(getGenerator());
		final ObjectBodyIR targetBodyIR =
				targetIR.bodyIR(getTargetAscendant());
		final Ptr<DataOp> targetPtr =
				targetBodyIR.pointer(getGenerator()).toData();
		final DataRec object = getInstance().object();

		object.setConstant(!getKind().isVariable()).setValue(targetPtr);
	}

	private void fill(boolean fillFields, boolean fillTarget) {
		if (this.filledAll || this.filling) {
			// already filled-in or in process
			return;
		}

		this.filling = true;
		try {
			if (fillFields) {
				fill();
			}
			if (fillTarget) {
				fillTarget();
			}
		} finally {
			this.filling = false;
		}

		if (fillFields) {
			this.filledFields = true;
		}
		if (fillTarget) {
			if (this.filledFields) {
				// fields are fully filled
				this.filledAll = true;
			}
		} else if (this.targetIRAllocated) {
			// target IR allocated while filling in
			// fill fields with target
			fill(false, true);
		}
	}

	private FuncPtr<C> reusedConstructor() {
		if (!getField().isClone()) {
			return null;
		}

		// field is a clone of overridden one
		// reuse constructor from overridden field

		final Field<?> lastDefinition = getField().getLastDefinition();
		final Obj overriddenOwner =
				lastDefinition.getEnclosingScope().toObject();
		final ObjectIR overriddenOwnerIR =
				overriddenOwner.ir(getGenerator()).getBodyType().getObjectIR();
		@SuppressWarnings("unchecked")
		final RefFld<C> overriddenFld =
				(RefFld<C>) overriddenOwnerIR.fld(getField().getKey());

		return overriddenFld.constructor;
	}

	public static abstract class Op<S extends Op<S, C>, C extends ObjectFunc<C>>
			extends Fld.Op<S> {

		public Op(StructWriter<S> writer) {
			super(writer);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Type<S, C> getType() {
			return (Type<S, C>) super.getType();
		}

		public final DataRecOp object(CodeId id, Code code) {
			return ptr(id, code, getType().object());
		}

		public FuncOp<C> constructor(CodeId id, Code code) {
			return func(id, code, getType().constructor());
		}

		public DataOp target(Code code, ObjOp host) {

			final DataOp object = object(null, code).load(null, code);
			final CondCode noTarget = object.isNull(null, code).branch(
					code,
					"no_target",
					"has_target");
			final Code hasTarget = noTarget.otherwise();

			final DataOp object1 = hasTarget.phi(null, object);

			hasTarget.go(code.tail());

			final DataOp object2 = construct(noTarget, host);

			noTarget.go(code.tail());

			return code.phi(null, object1, object2);
		}

		protected abstract DataOp construct(
				Code code,
				ObjOp host,
				C constructor);

		private DataOp construct(Code code, ObjOp host) {

			final C constructor = constructor(null, code).load(null, code);

			code.dumpName("Constructor: ", constructor);
			code.dumpName("Host: ", host.ptr());

			return construct(code, host, constructor);
		}

	}

	public static abstract class Type<
			S extends Op<S, C>,
			C extends ObjectFunc<C>>
					extends Fld.Type<S> {

		private DataRec object;
		private FuncRec<C> constructor;

		public Type() {
		}

		public final DataRec object() {
			return this.object;
		}

		public final FuncRec<C> constructor() {
			return this.constructor;
		}

		@Override
		public void allocate(SubData<S> data) {
			this.object = data.addDataPtr("object");
			this.constructor = data.addFuncPtr(
					"constructor",
					getSignature());
		}

		protected abstract ObjectSignature<C> getSignature();

		protected abstract FuncPtr<C> constructorStub();

	}

	private static class FldContent<
			T extends Type<?, C>,
			C extends ObjectFunc<C>>
					implements Content<T> {

		private final RefFld<C> fld;

		FldContent(RefFld<C> fld) {
			this.fld = fld;
		}

		public final RefFld<C> fld() {
			return this.fld;
		}

		@Override
		public void allocated(T instance) {
			this.fld.allocateMethods();
		}

		@Override
		public void fill(T instance) {

			final boolean fillTarget;

			if (!fld().targetIRAllocated
					&& fld().getField().isClone()
					&& fld().getField().isPrototype()) {
				// field is a clone - do not fill the target
				fillTarget = false;
			} else {
				fillTarget = true;
			}

			fld().fill(!fld().filledFields, fillTarget);
		}

		@Override
		public String toString() {
			return "Decls[" + this.fld + ']';
		}

	}

	private final class ConstructorBuilder implements FunctionBuilder<C> {

		@Override
		public void build(Function<C> constructor) {

			final Code failure = constructor.addBlock("failure");
			final ObjBuilder builder = new ObjBuilder(
					constructor,
					failure.head(),
					getBodyIR(),
					getBodyIR().getAscendant(),
					getBodyIR().getObjectIR().isExact() ? EXACT : COMPATIBLE);
			final CodeDirs dirs =
					builder.falseWhenUnknown(constructor, failure.head());

			buildConstructor(builder, dirs);

			if (failure.exists()) {
				failure.nullPtr().returnValue(failure);
			}
		}

	}

}
