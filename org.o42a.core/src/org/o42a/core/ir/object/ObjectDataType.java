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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.IRSymbolSeparator.DETAIL;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.*;


public final class ObjectDataType extends Type<ObjectDataType.Op> {

	public static final int OBJ_FLAG_RT = 0x1;
	public static final int OBJ_FLAG_ABSTRACT = 0x2;
	public static final int OBJ_FLAG_PROTOTYPE = 0x4;
	public static final int OBJ_FLAG_VOID = 0x80000000;
	public static final int OBJ_FLAG_FALSE = 0x40000000;

	private final IRGenerator generator;

	private RelPtrRec object;
	private Int32rec flags;
	private RelPtrRec start;
	private Int32rec allBodiesLayout;
	private ValOp.Type value;
	private CodeRec<ObjectValFunc> valueFunc;
	private CodeRec<ObjectCondFunc> requirementFunc;
	private CodeRec<ObjectValFunc> claimFunc;
	private CodeRec<ObjectCondFunc> conditionFunc;
	private CodeRec<ObjectValFunc> propositionFunc;
	private CodeRec<ObjectRefFunc> ancestorFunc;
	private AnyPtrRec ancestorType;
	private AnyPtrRec ownerType;
	private RelList<ObjectBodyIR> ascendants;
	private RelList<ObjectBodyIR> samples;

	ObjectDataType(IRGenerator generator) {
		super("ObjectData");
		this.generator = generator;
	}

	public final RelPtrRec getObject() {
		return this.object;
	}

	public final Int32rec getFlags() {
		return this.flags;
	}

	public final RelPtrRec getStart() {
		return this.start;
	}

	public final Int32rec getAllBodiesLayout() {
		return this.allBodiesLayout;
	}

	public final ValOp.Type getValue() {
		return this.value;
	}

	public final CodeRec<ObjectValFunc> getValueFunc() {
		return this.valueFunc;
	}

	public final CodeRec<ObjectCondFunc> getRequirementFunc() {
		return this.requirementFunc;
	}

	public final CodeRec<ObjectValFunc> getClaimFunc() {
		return this.claimFunc;
	}

	public final CodeRec<ObjectCondFunc> getConditionFunc() {
		return this.conditionFunc;
	}

	public final CodeRec<ObjectValFunc> getPropositionFunc() {
		return this.propositionFunc;
	}

	public final AnyPtrRec getOwnerType() {
		return this.ownerType;
	}

	public final CodeRec<ObjectRefFunc> getAncestorFunc() {
		return this.ancestorFunc;
	}

	public final AnyPtrRec getAncestorType() {
		return this.ancestorType;
	}

	public final RelList<ObjectBodyIR> getAscendants() {
		return this.ascendants;
	}

	public final RelList<ObjectBodyIR> getSamples() {
		return this.samples;
	}

	@Override
	public ObjectDataType.Op op(StructWriter writer) {
		return new ObjectDataType.Op(writer);
	}

	@Override
	protected void allocate(SubData<ObjectDataType.Op> data) {
		this.object = data.addRelPtr("object");
		this.flags = data.addInt32("flags");
		this.start = data.addRelPtr("start");
		this.allBodiesLayout = data.addInt32("all_bodies_layout");
		this.value = data.addInstance("value", this.generator.valType());
		this.valueFunc = data.addCodePtr(
				"value_f",
				this.generator.objectValSignature());
		this.requirementFunc = data.addCodePtr(
				"requirement_f",
				this.generator.objectCondSignature());
		this.claimFunc = data.addCodePtr(
				"claim_f",
				this.generator.objectValSignature());
		this.conditionFunc = data.addCodePtr(
				"condition_f",
				this.generator.objectCondSignature());
		this.propositionFunc = data.addCodePtr(
				"proposition_f",
				this.generator.objectValSignature());
		this.ownerType = data.addPtr("owner_type");
		this.ancestorFunc = data.addCodePtr(
				"ancestor_f",
				this.generator.objectRefSignature());
		this.ancestorType = data.addPtr("ancestor_type");
		this.ascendants =
			new Ascendants().allocate(this.generator, data, "ascendants");
		this.samples =
			new Samples().allocate(this.generator, data, "samples");
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final ObjectDataType getType() {
			return (ObjectDataType) super.getType();
		}

		public final DataOp<RelOp> object(Code code) {
			return writer().relPtr(code, getType().getObject());
		}

		public final AnyOp objectPtr(Code code) {
			return object(code).load(code).offset(code, this);
		}

		public final DataOp<RelOp> start(Code code) {
			return writer().relPtr(code, getType().getStart());
		}

		public final AnyOp startPtr(Code code) {
			return start(code).load(code).offset(code, this);
		}

		public final ValOp value(Code code) {
			return writer().struct(code, getType().getValue());
		}

		public final CodeOp<ObjectValFunc> valueFunc(Code code) {
			return writer().func(code, getType().getValueFunc());
		}

		public final CodeOp<ObjectCondFunc> requirementFunc(Code code) {
			return writer().func(code, getType().getRequirementFunc());
		}

		public final CodeOp<ObjectValFunc> claimFunc(Code code) {
			return writer().func(code, getType().getClaimFunc());
		}

		public final CodeOp<ObjectCondFunc> conditionFunc(Code code) {
			return writer().func(code, getType().getConditionFunc());
		}

		public final CodeOp<ObjectValFunc> propositionFunc(Code code) {
			return writer().func(code, getType().getPropositionFunc());
		}

		public final CodeOp<ObjectRefFunc> ancestorFunc(Code code) {
			return writer().func(code, getType().getAncestorFunc());
		}

		public final DataOp<AnyOp> ancestorType(Code code) {
			return writer().ptr(code, getType().getAncestorType());
		}

		public final ObjectDataOp op(
				CodeBuilder builder,
				ObjectPrecision precision) {
			return new ObjectDataOp(builder, this, precision);
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		public String toString() {
			return getType() + " data";
		}

	}

	private static final class Ascendants extends RelList<ObjectBodyIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				ObjectBodyIR item) {

			final IRGenerator generator = item.getGenerator();
			final String name =
				"ascendant" + DETAIL
				+ item.getAscendant().ir(generator).getId();
			final AscendantDescIR.Type desc = data.addInstance(
					name,
					generator.ascendantDescType(),
					new AscendantDescIR(item));

			return desc.getPointer();
		}

	}

	private static final class Samples extends RelList<ObjectBodyIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				ObjectBodyIR item) {

			final IRGenerator generator = item.getGenerator();
			final String name =
				"sample" + DETAIL
				+ item.getAscendant().ir(generator).getId();
			final SampleDescIR.Type desc = data.addInstance(
					name,
					generator.sampleDescType(),
					new SampleDescIR(item));

			return desc.getPointer();
		}

	}

}
