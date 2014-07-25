/*
    Compiler Core
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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;

import java.util.function.BiConsumer;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectFunc;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.Val;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class RefFld<
		F extends RefFld.Op<F>,
		C extends ObjectFunc<C>>
				extends MemberFld<F> {

	public static final StatelessType STATELESS_FLD = new StatelessType();
	public static final StatefulType STATEFUL_FLD = new StatefulType();

	public static final ID FLD_CTR_ID = ID.id("fld_ctr");
	public static final ID CONSTRUCT_ID = ID.rawId("construct");
	public static final ID CLONE_ID = ID.rawId("clone");

	private final Obj target;
	private Obj targetAscendant;

	private boolean dummy;
	private boolean constructorBuilt;
	private boolean targetIRAllocated;
	private boolean filling;
	private boolean filledFields;
	private boolean filledAll;

	private FuncRec<C> vmtConstructor;
	private FuncPtr<C> constructor;

	public RefFld(Field field, Obj target) {
		super(field);
		this.target = target;
	}

	public final Obj getTarget() {
		return this.target;
	}

	public final Obj getTargetAscendant() {
		return this.targetAscendant;
	}

	@Override
	public final boolean isDummy() {
		return this.dummy;
	}

	@Override
	public Type<F> getInstance() {
		return (Type<F>) super.getInstance();
	}

	public final void allocate(ObjectIRBodyData data, Obj targetAscendant) {
		getTarget().assertDerivedFrom(targetAscendant);
		this.targetAscendant = targetAscendant;
		allocate(data);
	}

	public final void allocateDummy(ObjectIRBodyData data) {
		this.dummy = true;
		this.targetIRAllocated = true;
		this.filledFields = true;
		this.filledAll = true;
		allocate(data);
	}

	@Override
	public void allocateMethods(SubData<VmtIROp> vmt) {
		if (isOmitted()) {
			return;
		}
		buildConstructor();
		this.vmtConstructor = vmt.addFuncPtr(
				getId().detail("constructor"),
				getConstructorSignature());
	}

	@Override
	public void fillMethods() {
		if (isOmitted()) {
			return;
		}
		vmtConstructor().setConstant(true).setValue(constructor());
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
	protected abstract Type<F> getType();

	@Override
	protected void allocate(ObjectIRBodyData data) {
		super.allocate(data);
		if (!this.targetIRAllocated) {
			this.targetIRAllocated = isOmitted();
		}
	}

	protected FuncPtr<C> reuseConstructor() {
		if (getField().isUpdated()) {
			return null;
		}

		// Field is a clone of overridden one.
		// Reuse constructor from overridden field.
		final Field lastDefinition = getField().getLastDefinition();
		final Obj overriddenOwner =
				lastDefinition.getEnclosingScope().toObject();
		final ObjectIR overriddenOwnerIR =
				overriddenOwner.ir(getGenerator()).getBodyType().getObjectIR();
		@SuppressWarnings("unchecked")
		final RefFld<F, C> overriddenFld =
				(RefFld<F, C>) overriddenOwnerIR.fld(getField().getKey());

		return overriddenFld.cloneFunc();
	}

	protected void fill() {
		if (!getType().isStateless()) {
			// Initialize object pointer to null.
			// May be overridden by fillTarget().
			getInstance().object().setNull();
		}
	}

	protected void fillDummy() {
		if (!getType().isStateless()) {
			// Initialize object pointer to null.
			// May be overridden by fillTarget().
			getInstance().object().setNull();
		}
	}

	protected abstract ObjectSignature<C> getConstructorSignature();

	protected abstract void buildConstructor(ObjBuilder builder, CodeDirs dirs);

	protected abstract Obj targetType(Obj bodyType);

	@Override
	protected Content<Type<F>> content() {
		return new FldContent<>(this);
	}

	@Override
	protected Content<Type<F>> dummyContent() {
		return new Content<Type<F>>() {
			@Override
			public void allocated(Type<F> instance) {
			}
			@Override
			public void fill(Type<F> instance) {
				fillDummy();
			}
		};
	}

	@Override
	protected abstract RefFldOp<F, C> op(Code code, ObjOp host, F ptr);

	protected final FuncPtr<C> constructor() {
		return this.constructor;
	}

	protected FuncPtr<C> cloneFunc() {
		return constructor();
	}

	protected final FuncRec<C> vmtConstructor() {
		assert this.vmtConstructor != null :
			"Constructor of " + this + " is not allocated in VMT";
		return this.vmtConstructor;
	}

	protected abstract FuncPtr<C> constructorStub();

	private void fillTarget() {
		if (getType().isStateless()) {
			return;
		}
		if (runtimeConstructedTarget()) {
			return;
		}

		final ObjectIR targetIR = getTarget().ir(getGenerator());
		final ObjectIRBody targetBodyIR =
				targetIR.bodyIR(getTargetAscendant());
		final Ptr<DataOp> targetPtr =
				targetBodyIR.pointer(getGenerator()).toData();

		getInstance().object().setValue(targetPtr);
	}

	private void buildConstructor() {
		if (this.constructorBuilt) {
			return;
		}
		this.constructorBuilt = true;
		if (isDummy()) {
			this.constructor =
					getGenerator()
					.getFunctions()
					.nullPtr(getConstructorSignature());
			return;
		}

		final FuncPtr<C> reusedConstructor = reuseConstructor();

		if (reusedConstructor != null) {
			this.constructor = reusedConstructor;
			return;
		}

		this.constructor = getGenerator().newFunction().create(
				getField().getId().detail(CONSTRUCT_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildConstructor)).getPointer();
	}

	private boolean runtimeConstructedTarget() {
		if (getTarget().getConstructionMode().isRuntime()) {
			return true;
		}

		final Obj target = getTarget();

		if (!target.value().getStatefulness().isEager()) {
			return false;
		}

		// Objects with initially unknown eagerly evaluated values
		// should be constructed at run time.
		final Val initialValue =
				target.ir(getGenerator()).getDataIR().getInitialValue();

		return initialValue.isIndefinite();
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

	public static abstract class Op<S extends Op<S>>
					extends Fld.Op<S> {

		public Op(StructWriter<S> writer) {
			super(writer);
		}

		@Override
		public Type<S> getType() {
			return (Type<S>) super.getType();
		}

		public final DataRecOp object(ID id, Code code) {
			return ptr(id, code, getType().object());
		}

	}

	public static abstract class Type<F extends Op<F>>
					extends Fld.Type<F> {

		private DataRec object;

		public Type(ID id) {
			super(id);
		}

		public abstract boolean isStateless();

		public final DataRec object() {
			assert !isStateless() :
				this + " is stateless";
			return this.object;
		}

		@Override
		protected void allocate(SubData<F> data) {
			if (!isStateless()) {
				this.object = data.addDataPtr("object");
			}
		}

	}

	public static final class StatelessOp extends RefFld.Op<StatelessOp> {

		private StatelessOp(StructWriter<StatelessOp> writer) {
			super(writer);
		}

		@Override
		public final StatelessType getType() {
			return (StatelessType) super.getType();
		}

	}

	public static final class StatelessType extends RefFld.Type<StatelessOp> {

		private StatelessType() {
			super(ID.rawId("o42a_fld_link"));
		}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public StatelessOp op(StructWriter<StatelessOp> writer) {
			return new StatelessOp(writer);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.LINK.code());
		}

	}

	public static final class StatefulOp extends RefFld.Op<StatefulOp> {

		private StatefulOp(StructWriter<StatefulOp> writer) {
			super(writer);
		}

		@Override
		public final StatefulType getType() {
			return (StatefulType) super.getType();
		}

	}

	public static final class StatefulType extends RefFld.Type<StatefulOp> {

		private StatefulType() {
			super(ID.rawId("o42a_fld_obj"));
		}

		@Override
		public boolean isStateless() {
			return false;
		}

		@Override
		public StatefulOp op(StructWriter<StatefulOp> writer) {
			return new StatefulOp(writer);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.OBJ.code());
		}

	}

	private static class FldContent<
			F extends Op<F>,
			T extends Type<F>,
			C extends ObjectFunc<C>>
					implements Content<T> {

		private final RefFld<F, C> fld;

		FldContent(RefFld<F, C> fld) {
			this.fld = fld;
		}

		public final RefFld<F, C> fld() {
			return this.fld;
		}

		@Override
		public void allocated(T instance) {
			this.fld.buildConstructor();
		}

		@Override
		public void fill(T instance) {

			final boolean fillTarget;

			if (!fld().targetIRAllocated && !fld().getField().isUpdated()) {
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

	protected final class ConstructorBuilder implements FunctionBuilder<C> {

		private final BiConsumer<ObjBuilder, CodeDirs> build;

		public ConstructorBuilder(BiConsumer<ObjBuilder, CodeDirs> build) {
			this.build = build;
		}

		@Override
		public void build(Function<C> constructor) {

			final Block failure = constructor.addBlock("failure");
			final ObjBuilder builder = new ObjBuilder(
					constructor,
					failure.head(),
					getBodyIR(),
					getBodyIR().getClosestAscendant(),
					getBodyIR().getObjectIR().isExact() ? EXACT : COMPATIBLE);
			final CodeDirs dirs =
					builder.dirs(constructor, failure.head());

			this.build.accept(builder, dirs);

			if (failure.exists()) {
				failure.nullPtr().returnValue(failure);
			}
		}

	}

}
