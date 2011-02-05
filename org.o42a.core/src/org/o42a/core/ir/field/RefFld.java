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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.CodeOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.member.field.Field;


public abstract class RefFld extends Fld {

	private final ObjectIR targetIR;
	private Obj targetAscendant;

	private boolean targetIRAllocated;
	private boolean filling;
	private boolean filledFields;
	private boolean filledAll;

	private Function<ObjectRefFunc> constructor;
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

	@Override
	public Type<?> getInstance() {
		return (Type<?>) super.getInstance();
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
	protected abstract Type<?> getType();

	@Override
	public abstract RefFldOp op(Code code, ObjOp host);

	protected void allocateMethods() {

		final Function<ObjectRefFunc> reusedConstructor = reusedConstructor();

		if (reusedConstructor != null) {
			this.constructor = reusedConstructor;
			this.constructorReused = true;
			return;
		}

		this.constructor = getGenerator().newFunction().create(
				getField().ir(getGenerator()).getId().detail("constructor"),
				getGenerator().objectRefSignature());
	}

	protected void fill() {
		getInstance().getObject().setNull();
		createConstructor();
	}

	protected final void createConstructor() {
		if (this.constructorReused) {
			getInstance().getConstructor().setValue(
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

		getInstance().getConstructor().setValue(
				this.constructor.getPointer());
	}

	protected void buildConstructor(
			ObjBuilder builder,
			Code code,
			CodePos exit) {

		final RefFldOp fld = op(code, builder.host());

		final ObjectOp result = construct(builder, code, exit, fld);
		final AnyOp res = result.toAny(code);

		fld.ptr().object(code).store(code, res);
		res.returnValue(code);
	}

	protected ObjectOp construct(
			ObjBuilder builder,
			Code code,
			CodePos exit,
			RefFldOp fld) {

		final Artifact<?> artifact = getField().getArtifact();
		final Obj object = artifact.toObject();

		if (object != null) {
			return builder.newObject(
					code,
					exit,
					object,
					CtrOp.FIELD_PROPAGATION);
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

		return link.getType();
	}

	@Override
	protected Content<Type<?>> content() {
		return new FldContent<Type<?>>(this);
	}

	private void fillTarget(ObjectBodyIR targetBodyIR) {
		getInstance().getObject().setValue(targetBodyIR.getPointer().toAny());
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

	private Function<ObjectRefFunc> reusedConstructor() {
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
		final RefFld overriddenFld =
			(RefFld) overriddenOwnerIR.fld(getField().getKey());

		return overriddenFld.constructor;
	}

	public static abstract class Op extends Fld.Op {

		Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public Type<?> getType() {
			return (Type<?>) super.getType();
		}

		public final DataOp<AnyOp> object(Code code) {
			return writer().ptr(code, getType().getObject());
		}

		public CodeOp<ObjectRefFunc> constructor(Code code) {
			return writer().func(code, getType().getConstructor());
		}

		public AnyOp target(Code code, AnyOp host) {

			final AnyOp object = object(code).load(code);
			final CondBlk noTarget = object.isNull(code).branch(
					code,
					"no_target",
					"has_target");
			final CodeBlk hasTarget = noTarget.otherwise();

			final AnyOp object1 = hasTarget.phi(object);

			hasTarget.go(code.tail());

			final AnyOp object2 = construct(noTarget, host);

			noTarget.go(code.tail());

			return code.phi(object1, object2);
		}

		private AnyOp construct(Code code, AnyOp host) {

			final ObjectRefFunc constructor = constructor(code).load(code);
			final AnyOp self = host.toAny(code);

			code.dumpName("Constructor: ", constructor);
			code.dumpName("Host: ", self);
			return constructor.call(code, self);
		}

	}

	public static abstract class Type<O extends Op> extends Fld.Type<O> {

		protected final IRGenerator generator;
		private AnyPtrRec object;
		private CodeRec<ObjectRefFunc> constructor;

		Type(IRGenerator generator, CodeId id) {
			super(id);
			this.generator = generator;
		}

		public final AnyPtrRec getObject() {
			return this.object;
		}

		public final CodeRec<ObjectRefFunc> getConstructor() {
			return this.constructor;
		}

		@Override
		public void allocate(SubData<O> data) {
			this.object = data.addPtr("object");
			this.constructor = data.addCodePtr(
					"constructor",
					this.generator.objectRefSignature());
		}

	}

	private static class FldContent<T extends Type<?>> implements Content<T> {

		private final RefFld fld;

		FldContent(RefFld fld) {
			this.fld = fld;
		}

		public final RefFld fld() {
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
