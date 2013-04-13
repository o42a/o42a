/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.field.array;

import static org.o42a.core.ir.field.FldKind.ARRAY_STATE;
import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Sample;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.Value;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public class ArraySte extends Fld<ArraySte.Op>
		implements Content<ArraySte.Type> {

	public static final Type ARRAY_STE = new Type();

	private static final Name ARRAY_STE_NAME = CASE_SENSITIVE.name("A");
	private static final ID ARRAY_STE_ID = ARRAY_STE_NAME.toID();
	private static final MemberName ARRAY_STE_MEMBER =
			fieldName(ARRAY_STE_NAME);

	public static MemberKey arraySteKey(CompilerContext context) {
		return ARRAY_STE_MEMBER.key(arrayObject(context).getScope());
	}

	private static Obj arrayObject(CompilerContext context) {
		return context.getIntrinsics().getArray();
	}

	private MemberKey key;
	private Obj definedIn;

	@Override
	public ID getId() {
		return ARRAY_STE_ID;
	}

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = ARRAY_STE_MEMBER.key(
				arrayObject(getBodyIR().getAscendant().getContext())
				.getScope());
	}

	@Override
	public FldKind getKind() {
		return FldKind.ARRAY_STATE;
	}

	@Override
	public boolean isOverrider() {
		return false;
	}

	@Override
	public final Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	public Obj getDefinedIn() {
		if (this.definedIn != null) {
			return this.definedIn;
		}
		return this.definedIn = definedIn(getObject());
	}

	public final void declare(ObjectIRBodyData data) {
		allocate(data);
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {

		final ValType val = instance.value();
		final Value<?> value = getObject().value().getValue();

		if (!value.getKnowledge().isInitiallyKnown()) {
			val.set(INDEFINITE_VAL);
		} else if (value.getKnowledge().isFalse()) {
			val.set(FALSE_VAL);
		} else {

			final ArrayValueType arrayType =
					getObject().type().getValueType().toArrayType();
			final Array array =
					arrayType.cast(value).getCompilerValue();

			val.set(arrayType.ir(getGenerator()).staticsIR().val(array));
		}
	}

	@Override
	public ArraySteOp op(Code code, ObjOp host) {
		return new ArraySteOp(
				host,
				this,
				host.ptr().field(code, getInstance()));
	}

	@Override
	protected boolean mayOmit() {
		return false;
	}

	@Override
	protected Type getType() {
		return ARRAY_STE;
	}

	@Override
	protected Content<ArraySte.Type> content() {
		return this;
	}

	final Obj getObject() {
		return getObjectIR().getObject();
	}

	final ObjectIR getObjectIR() {
		return getBodyIR().getObjectIR();
	}

	private static Obj definedIn(Obj object) {
		if (!object.type().getValueType().isArray()) {
			return null;
		}

		Obj definedIn = null;

		for (Sample sample : object.type().getSamples()) {

			final Obj sampleDefinedIn = definedIn(sample.getObject());

			if (sampleDefinedIn == null) {
				continue;
			}
			if (definedIn == null) {
				definedIn = sampleDefinedIn;
				continue;
			}
			if (!definedIn.is(sampleDefinedIn)) {
				return object;
			}
		}

		final Obj ancestorDefinedIn =
				definedIn(object.type().getAncestor().getType());

		if (ancestorDefinedIn == null) {
			return definedIn != null ? definedIn : object;
		}
		if (definedIn == null || definedIn.is(ancestorDefinedIn)) {
			return ancestorDefinedIn;
		}

		return object;
	}

	public static final class Type extends Fld.Type<Op> {

		private ValType value;

		private Type() {
			super(ID.rawId("o42a_ste_array"));
		}

		public final ValType value() {
			return this.value;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.value = data.addInstance(ID.id("value"), VAL_TYPE);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | ARRAY_STATE.code());
		}

	}

	public static final class Op extends Fld.Op<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		public final ValType.Op value(ID id, Code code) {
			return struct(id, code, ARRAY_STE.value());
		}

	}

}
