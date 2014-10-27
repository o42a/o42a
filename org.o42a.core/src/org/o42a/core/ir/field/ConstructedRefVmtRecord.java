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

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE_OBJECT;

import java.util.function.BiConsumer;

import org.o42a.codegen.code.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.object.op.ObjectSignature;
import org.o42a.core.ir.object.vmt.VmtIROp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


public abstract class ConstructedRefVmtRecord<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>,
		C extends ObjectFn<C>>
				extends RefVmtRecord<F, T, FuncPtr<C>, FuncRec<C>> {

	public ConstructedRefVmtRecord(ConstructedRefFld<F, T, C> fld) {
		super(fld);
	}

	@Override
	public ConstructedRefFld<F, T, C> fld() {
		return (ConstructedRefFld<F, T, C>) super.fld();
	}

	public abstract ObjectSignature<C> getConstructorSignature();

	public final FuncPtr<C> constructor() {
		return content();
	}

	@Override
	protected FuncRec<C> allocateRecord(SubData<VmtIROp> vmt) {
		return vmt.addFuncPtr(
				fld().getId().detail("constructor"),
				getConstructorSignature());
	}

	protected FuncPtr<C> cloneFunc() {
		return constructor();
	}

	@Override
	protected FuncPtr<C> dummyContent() {
		return getGenerator().getFunctions().nullPtr(getConstructorSignature());
	}

	@Override
	protected FuncPtr<C> reuseContent() {

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
		final ConstructedRefFld<F, T, C> overriddenFld =
				(ConstructedRefFld<F, T, C>) overriddenOwnerIR.bodies()
				.fld(field.getKey());

		return overriddenFld.vmtRecord().cloneFunc();
	}

	@Override
	protected FuncPtr<C> createContent() {
		return getGenerator().newFunction().create(
				fld().getField().getId().detail(CONSTRUCT_ID),
				getConstructorSignature(),
				new ConstructorBuilder(this::buildConstructor))
				.getPointer();
	}

	protected abstract void buildConstructor(ObjBuilder builder, CodeDirs dirs);

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
					COMPATIBLE_OBJECT);
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

}
