/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.value.PredefObjValues.predefObjValues;
import static org.o42a.core.ir.op.PrintMessageFunc.PRINT_MESSAGE;
import static org.o42a.core.value.Value.voidValue;

import java.io.UnsupportedEncodingException;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.PrintMessageFunc;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;


public enum PredefObjValue {

	FALSE_OBJ_VALUE(false) {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value_false");
		}

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {
			dirs.code().go(dirs.falseDir());
		}

	},

	VOID_OBJ_VALUE(false) {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value_void");
		}

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {
			dirs.returnValue(voidValue().op(dirs.getBuilder(), dirs.code()));
		}

	},

	STUB_OBJ_VALUE(false) {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value_stub");
		}

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {

			final Generator generator = dirs.getGenerator();
			final Ptr<AnyOp> message;

			try {
				message = generator.addBinary(
						generator.id("ERROR").sub("object_value_stub"),
						true,
						"Object value stub accessed".getBytes("ASCII"));
			} catch (UnsupportedEncodingException e) {
				throw new Error("ASCII not supported", e);
			}

			final FuncPtr<PrintMessageFunc> fn =
					generator.externalFunction().link(
							"o42a_error_print",
							PRINT_MESSAGE);

			final Block code = dirs.code();

			fn.op(null, code).print(code, message.op(null, code));
			dirs.code().go(dirs.falseDir());
		}

	},

	DEFAULT_OBJ_VALUE(true) {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value");
		}

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {

			final Block code = dirs.code();
			final ObjectOp owner = dirs.getBuilder().owner();

			data.claimFunc(code).load(null, code).call(dirs, owner);
			data.propositionFunc(code).load(null, code).call(dirs, owner);
		}

	};

	private final boolean typeAware;

	PredefObjValue(boolean typeAware) {
		this.typeAware = typeAware;
	}

	public final boolean isTypeAware() {
		return this.typeAware;
	}

	public abstract CodeId codeId(Generator generator);

	public final FuncPtr<ObjectValueFunc> get(
			CompilerContext context,
			Generator generator,
			ValueType<?> valueType) {
		return predefObjValues(generator).get(context, this, valueType);
	}

	public abstract void write(DefDirs dirs, ObjectIRData.Op data);

}
