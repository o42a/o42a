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

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.member.field.Field;


public abstract class RefFld<C extends Func> extends Fld {

	private final ObjectIR targetIR;
	private Obj targetAscendant;

	private boolean targetIRAllocated;
	private boolean filling;
	private boolean filledFields;
	private boolean filledAll;

	private Function<C> constructor;
	private boolean constructorReused;

	RefFld(ObjectBodyIR bodyIR, Field<?> field) {
		super(bodyIR, field);
		this.targetIR = field.getArtifact().materialize().ir(getGenerator());
	}

	public final ObjectIR getTargetIR() {
		return this.targetIR;
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
		getTargetIR().getObject().assertDerivedFrom(targetAscendant);
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
	public abstract RefFldOp<C> op(Code code, ObjOp host);

	protected void allocateMethods() {

		final Function<C> reusedConstructor = reusedConstructor();

		if (reusedConstructor != null) {
			this.constructor = reusedConstructor;
			this.constructorReused = true;
			return;
		}

		this.constructor = getGenerator().newFunction().create(
				getField().ir(getGenerator()).getId().detail("constructor"),
				getType().getSignature());
	}

	protected void fill() {
		getInstance().object().setNull();
		createConstructor();
	}

	protected final void createConstructor() {
		if (this.constructorReused) {
			getInstance().constructor().setValue(
					this.constructor.getPointer());
			return;
		}

		final CodeBlk failure = this.constructor.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				this.constructor,
				failure.head(),
				0,
				getBodyIR(),
				getBodyIR().getAscendant(),
				COMPATIBLE);

		buildConstructor(builder, this.constructor, failure.head());

		if (failure.exists()) {
			failure.nullPtr().returnValue(failure);
		}

		this.constructor.done();

		getInstance().constructor().setValue(
				this.constructor.getPointer());
	}

	protected void buildConstructor(
			ObjBuilder builder,
			Code code,
			CodePos exit) {

		final RefFldOp<C> fld = op(code, builder.host());

		final ObjectOp result = construct(builder, code, exit, fld);
		final AnyOp res = result.toAny(code);

		fld.ptr().object(code).store(code, res);
		res.returnValue(code);
	}

	protected ObjectOp construct(
			ObjBuilder builder,
			Code code,
			CodePos exit,
			RefFldOp<C> fld) {

		final Artifact<?> artifact = getField().getArtifact();
		final Obj object = artifact.toObject();

		if (object != null) {
			return builder.newObject(
					code,
					exit,
					object,
					CtrOp.PROPAGATION);
		}

		final Link link = artifact.toLink();
		final HostOp target =
			link.getTargetRef().target(code, exit, builder.host());

		return target.materialize(code, exit);
	}

	protected final Obj targetType(Obj bodyType) {

		final Field<?> actual = bodyType.member(getField().getKey()).toField();
		final Artifact<?> artifact = actual.getArtifact();
		final Obj object = artifact.toObject();

		if (object != null) {
			return object;
		}

		final Link link = artifact.toLink();

		return link.getTypeRef().getType();
	}

	@Override
	protected Content<Type<?, C>> content() {
		return new FldContent<Type<?, C>, C>(this);
	}

	private void fillTarget(ObjectBodyIR targetBodyIR) {
		getInstance().object().setValue(
				targetBodyIR.pointer(targetBodyIR.getGenerator()).toData());
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
				fillTarget(this.targetIR.bodyIR(getTargetAscendant()));
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

	private Function<C> reusedConstructor() {
		if (!getField().isClone()) {
			return null;
		}

		// field is a clone of overridden one
		// reuse constructor from overridden field

		final Field<?> lastDefinition = getField().getLastDefinition();
		final Obj overriddenOwner =
			lastDefinition.getEnclosingScope().getContainer().toObject();
		final ObjectIR overriddenOwnerIR =
			overriddenOwner.ir(getGenerator()).getBodyType().getObjectIR();
		@SuppressWarnings("unchecked")
		final RefFld<C> overriddenFld =
			(RefFld<C>) overriddenOwnerIR.fld(getField().getKey());

		return overriddenFld.constructor;
	}

	public static abstract class Op<C extends Func> extends Fld.Op {

		Op(StructWriter writer) {
			super(writer);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Type<?, C> getType() {
			return (Type<?, C>) super.getType();
		}

		public final RecOp<DataOp> object(Code code) {
			return writer().ptr(code, getType().object());
		}

		public FuncOp<C> constructor(Code code) {
			return writer().func(code, getType().constructor());
		}

		public DataOp target(Code code, ObjOp host) {

			final DataOp object = object(code).load(code);
			final CondBlk noTarget = object.isNull(code).branch(
					code,
					"no_target",
					"has_target");
			final CodeBlk hasTarget = noTarget.otherwise();

			final DataOp object1 = hasTarget.phi(object);

			hasTarget.go(code.tail());

			final DataOp object2 = construct(noTarget, host);

			noTarget.go(code.tail());

			return code.phi(object1, object2);
		}

		protected abstract DataOp construct(
				Code code,
				ObjOp host,
				C constructor);

		private DataOp construct(Code code, ObjOp host) {

			final C constructor = constructor(code).load(code);

			code.dumpName("Constructor: ", constructor);
			code.dumpName("Host: ", host.ptr());

			return construct(code, host, constructor);
		}

	}

	public static abstract class Type<O extends Op<C>, C extends Func>
			extends Fld.Type<O> {

		private DataRec<DataOp> object;
		private FuncRec<C> constructor;

		Type() {
		}

		public final DataRec<DataOp> object() {
			return this.object;
		}

		public final FuncRec<C> constructor() {
			return this.constructor;
		}

		@Override
		public void allocate(SubData<O> data) {
			this.object = data.addDataPtr("object");
			this.constructor = data.addCodePtr(
					"constructor",
					getSignature());
		}

		protected abstract Signature<C> getSignature();

	}

	private static class FldContent<T extends Type<?, C>, C extends Func>
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

}
