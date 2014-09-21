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
import static org.o42a.util.fn.Init.init;

import java.util.function.BiConsumer;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.ir.object.vmt.VmtIROp;
import org.o42a.core.ir.object.vmt.VmtRecord;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public abstract class RefVmtRecord<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>,
		C extends ObjectFn<C>>
				implements VmtRecord {

	public static final ID CONSTRUCT_ID = ID.rawId("construct");
	public static final ID CLONE_ID = ID.rawId("clone");
	public static final ID FLD_CTR_ID = ID.id("fctr");

	private final RefFld<F, T, C> fld;
	private final Init<RefConstructor<C>> constructor =
			init(this::createConstructor);
	private FuncRec<C> vmtConstructor;

	public RefVmtRecord(RefFld<F, T, C> fld) {
		this.fld = fld;
	}

	public RefFld<F, T, C> fld() {
		return this.fld;
	}

	public final Generator getGenerator() {
		return fld().getGenerator();
	}

	public abstract ObjectSignature<C> getConstructorSignature();

	public final FuncPtr<C> constructor() {
		return this.constructor.get().ptr();
	}

	@Override
	public boolean derive(VmtIR vmtIR) {
		if (!this.constructor.get().isDerived()) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final RefFld<F, T, C> derivedFld =
				(RefFld<F, T, C>)
				vmtIR.getObjectIR().bodies().findFld(fld().getKey());

		if (derivedFld == null) {
			return false;
		}

		this.vmtConstructor = derivedFld.vmtRecord().vmtConstructor();

		assert this.vmtConstructor != null :
			"Derived constructor of " + this + " is not allocated in VMT";

		return true;
	}

	@Override
	public void allocateMethods(SubData<VmtIROp> vmt) {
		this.vmtConstructor = vmt.addFuncPtr(
				fld().getId().detail("constructor"),
				getConstructorSignature());
	}

	@Override
	public void fillMethods() {
		vmtConstructor().setConstant(true).setValue(constructor());
	}

	protected final FuncRec<C> vmtConstructor() {
		assert this.vmtConstructor != null :
			"Constructor of " + this + " is not allocated in VMT";
		return this.vmtConstructor;
	}

	protected FuncPtr<C> cloneFunc() {
		return constructor();
	}

	protected FuncPtr<C> reuseConstructor() {

		final Field field = fld().getField();

		if (field.isUpdated()) {
			return null;
		}

		// Field is a clone of overridden one.
		// Reuse constructor from overridden field.
		final Field lastDefinition = field.getLastDefinition();
		final Obj overriddenOwner =
				lastDefinition.getEnclosingScope().toObject();
		final ObjectIR overriddenOwnerIR =
				overriddenOwner.ir(getGenerator());
		@SuppressWarnings("unchecked")
		final RefFld<F, T, C> overriddenFld =
				(RefFld<F, T, C>) overriddenOwnerIR.bodies()
				.fld(field.getKey());

		return overriddenFld.vmtRecord().cloneFunc();
	}

	protected abstract void buildConstructor(ObjBuilder builder, CodeDirs dirs);

	private RefConstructor<C> createConstructor() {
		assert !fld().getBodyIR().bodies().isTypeBodies() :
			"Can not build constructor for type field " + this;
		if (fld().isDummy()) {

			final FuncPtr<C> nullPtr =
					getGenerator()
					.getFunctions()
					.nullPtr(getConstructorSignature());

			return new RefConstructor<>(nullPtr, true);
		}

		final FuncPtr<C> reusedConstructor = reuseConstructor();

		if (reusedConstructor != null) {
			return new RefConstructor<>(reusedConstructor, true);
		}

		final Function<C> constructor = getGenerator().newFunction().create(
				fld().getField().getId().detail(CONSTRUCT_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildConstructor));

		return new RefConstructor<>(constructor.getPointer(), false);
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
					fld().getObjectIR(),
					COMPATIBLE);
			final CodeDirs dirs = builder.dirs(constructor, failure.head());
			final CodeDirs subDirs =
					dirs.begin(
							CONSTRUCT_ID,
							"Constructing field `" + fld().getField() + "`");

			this.build.accept(builder, subDirs);

			subDirs.done();

			if (failure.exists()) {
				failure.nullDataPtr().returnValue(failure);
			}
		}

	}

	private static final class RefConstructor<C extends ObjectFn<C>> {

		private final FuncPtr<C> ptr;
		private final boolean derived;

		RefConstructor(FuncPtr<C> ptr, boolean derived) {
			this.ptr = ptr;
			this.derived = derived;
		}

		public final FuncPtr<C> ptr() {
			return this.ptr;
		}

		public final boolean isDerived() {
			return this.derived;
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

	}

}
