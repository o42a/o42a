/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.value.string;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectDataIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.OpPresets;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.*;
import org.o42a.core.value.ValueType;


final class StringValueTypeIR extends ValueTypeIR<String> {

	StringValueTypeIR(Generator generator, StringValueType valueType) {
		super(generator, valueType);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return new StringValueIR(this, objectIR);
	}

	@Override
	protected StaticsIR<String> createStaticsIR() {
		return new StringStaticsIR(this);
	}

	private static final class StringValueIR extends ValueIR {

		StringValueIR(ValueTypeIR<?> valueTypeIR, ObjectIR objectIR) {
			super(valueTypeIR, objectIR);
		}

		@Override
		public OpPresets valuePresets(OpPresets presets) {
			return presets;
		}

		@Override
		public <H extends ObjectOp> ValueOp<H> op(H object) {
			return new StringValueOp<>(this, object);
		}

		@Override
		public Val initialValue(ObjectDataIR dataIR) {

			final String value = ValueType.STRING.cast(
					getObjectIR()
					.getObject()
					.value()
					.getValue()
					.getCompilerValue());

			return ValueType.STRING.ir(getGenerator()).staticsIR().val(value);
		}

	}

	private static final class StringValueOp<H extends ObjectOp>
			extends DefaultValueOp<H> {

		StringValueOp(ValueIR valueIR, H object) {
			super(valueIR, object);
		}

		@Override
		public StateOp<H> state() {
			return new StringStateOp<>(object());
		}

	}

	private static final class StringStateOp<H extends ObjectOp>
			extends StateOp<H> {

		StringStateOp(H host) {
			super(host);
		}

		@Override
		public void init(Block code, ValOp value) {
			value().length(null, code).store(
					code,
					value.length(null, code).load(null, code),
					ATOMIC);
			value().rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code),
					ATOMIC);

			code.releaseBarrier();

			flags().store(code, value.flags(code).get());
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
