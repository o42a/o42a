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

import java.util.function.BiConsumer;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.Val;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class RefFld<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>,
		C extends ObjectFn<C>>
				extends MemberFld<F, T> {

	public static final StatefulType STATEFUL_FLD = new StatefulType();

	public static final ID FLD_CTR_ID = ID.id("fctr");
	public static final ID CONSTRUCT_ID = ID.rawId("construct");
	public static final ID CLONE_ID = ID.rawId("clone");

	private final Obj target;
	private final Obj targetAscendant;

	private boolean targetIRAllocated;
	private boolean filling;
	private boolean filledFields;
	private boolean filledAll;

	private FuncRec<C> vmtConstructor;
	private FuncPtr<C> constructor;

	public RefFld(
			ObjectIRBody bodyIR,
			Field field,
			boolean dummy,
			Obj target,
			Obj targetAscendant) {
		super(bodyIR, field, dummy);
		assert dummy || target.assertDerivedFrom(targetAscendant);
		this.target = target;
		this.targetAscendant = targetAscendant;
		if (isDummy()) {
			this.targetIRAllocated = true;
			this.filledFields = true;
			this.filledAll = true;
		}
		this.targetIRAllocated = isOmitted();
	}

	public final Obj getTarget() {
		return this.target;
	}

	public final Obj getTargetAscendant() {
		return this.targetAscendant;
	}

	@Override
	public void allocateMethods(SubData<VmtIROp> vmt) {
		if (isOmitted()) {
			return;
		}
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
		assert !getBodyIR().bodies().isTypeBodies() :
			"Can not allocate target in object type bodies";
		if (this.targetIRAllocated) {
			return;
		}
		this.targetIRAllocated = true;
		// target object is allocated - fill it
		fill(false, true);
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
				overriddenOwner.ir(getGenerator());
		@SuppressWarnings("unchecked")
		final RefFld<F, T, C> overriddenFld =
				(RefFld<F, T, C>) overriddenOwnerIR.bodies()
				.fld(getField().getKey());

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
	protected Content<T> content() {
		return new FldContent<>(this);
	}

	@Override
	protected Content<T> dummyContent() {
		return instance -> fillDummy();
	}

	@Override
	protected abstract RefFldOp<F, T, C> op(Code code, ObjOp host, F ptr);

	protected final FuncPtr<C> constructor() {
		if (this.constructor != null) {
			return this.constructor;
		}
		assert !getBodyIR().bodies().isTypeBodies() :
			"Can not build constructor for type field " + this;
		if (isDummy()) {
			return this.constructor =
					getGenerator()
					.getFunctions()
					.nullPtr(getConstructorSignature());
		}

		final FuncPtr<C> reusedConstructor = reuseConstructor();

		if (reusedConstructor != null) {
			return this.constructor = reusedConstructor;
		}

		return this.constructor = getGenerator().newFunction().create(
				getField().getId().detail(CONSTRUCT_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildConstructor)).getPointer();
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
		final Ptr<DataOp> targetPtr = targetIR.ptr().toData();

		getInstance().object().setValue(targetPtr);
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

	public static final class StatelessType extends RefFld.Type<StatelessOp> {

		private StatelessType() {
			super(null);
		}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public StatelessOp op(StructWriter<StatelessOp> writer) {
			return new StatelessOp(writer);
		}

	}

	private static class FldContent<
			F extends Op<F>,
			T extends Type<F>,
			C extends ObjectFn<C>>
					implements Content<T> {

		private final RefFld<F, T, C> fld;

		FldContent(RefFld<F, T, C> fld) {
			this.fld = fld;
		}

		public final RefFld<F, T, C> fld() {
			return this.fld;
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
					getObjectIR(),
					COMPATIBLE);
			final CodeDirs dirs = builder.dirs(constructor, failure.head());
			final CodeDirs subDirs =
					dirs.begin(
							CONSTRUCT_ID,
							"Constructing field `" + getField() + "`");

			this.build.accept(builder, subDirs);

			subDirs.done();

			if (failure.exists()) {
				failure.nullDataPtr().returnValue(failure);
			}
		}

	}

}
