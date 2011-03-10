/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.DebugFieldInfo.DEBUG_FIELD_INFO_TYPE;
import static org.o42a.codegen.debug.DebugFieldInfo.fieldName;

import java.security.SecureRandom;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public class DebugTypeInfo extends Struct<DebugTypeInfo.Op> {

	private static final SecureRandom typeCodeGenerator = new SecureRandom();

	private final Type<?> target;
	private final int code;

	private Int32rec typeCode;
	private Int32rec fieldNum;
	private AnyPtrRec name;

	public DebugTypeInfo(Type<?> target) {
		this.target = target;
		this.code = typeCodeGenerator.nextInt();
	}

	@Override
	public boolean isDebugInfo() {
		return true;
	}

	public final Type<?> getTarget() {
		return this.target;
	}

	public final int getCode() {
		return this.code;
	}

	public final Int32rec typeCode() {
		return this.typeCode;
	}

	public final Int32rec fieldNum() {
		return this.fieldNum;
	}

	public final AnyPtrRec name() {
		return this.name;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("DEBUG").sub("type")
		.sub(getTarget().codeId(factory));
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.typeCode = data.addInt32("type_code");
		this.fieldNum = data.addInt32("field_num");
		this.name = data.addPtr("name");

		final Generator generator = data.getGenerator();
		final Debug debug = generator;

		typeCode().setValue(getCode());
		fieldNum().setValue(getTarget().size(generator));

		final CodeId typeId = getTarget().codeId(generator);

		debug.setName(
				name(),
				generator.id("DEBUG").sub("type_name").sub(typeId),
				typeId.toString());

		for (Data<?> field : getTarget().iterate(generator)) {
			addFieldInfo(data, field);
		}
	}

	private void addFieldInfo(SubData<Op> data, Data<?> field) {

		final Type<?> fieldInstance = field.getInstance();

		if (fieldInstance != null && fieldInstance.isEmbedded()) {
			for (Data<?> f : getTarget().iterate(data.getGenerator())) {
				addFieldInfo(data, f);
			}
			return;
		}

		data.addInstance(
				data.getGenerator().id("field").detail(fieldName(field)),
				DEBUG_FIELD_INFO_TYPE,
				new DebugFieldInfo(field));
	}

	@Override
	protected void fill() {
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

}
