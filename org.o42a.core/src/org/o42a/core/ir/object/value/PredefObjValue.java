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

import static org.o42a.core.ir.IRNames.ERROR_ID;
import static org.o42a.core.ir.object.value.PredefObjValues.predefObjValues;
import static org.o42a.core.ir.op.PrintMessageFunc.PRINT_MESSAGE;
import static org.o42a.core.value.Value.voidValue;

import java.io.UnsupportedEncodingException;

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
import org.o42a.util.string.ID;


public enum PredefObjValue {

	FALSE_OBJ_VALUE(ID.id("_o42a_obj_value_false"), false) {

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {
			dirs.code().go(dirs.falseDir());
		}

	},

	VOID_OBJ_VALUE(ID.id("_o42a_obj_value_void"), false) {

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {
			dirs.returnValue(voidValue().op(dirs.getBuilder(), dirs.code()));
		}

	},

	STUB_OBJ_VALUE(ID.id("_o42a_obj_value_stub"), false) {

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {

			final Generator generator = dirs.getGenerator();
			final Ptr<AnyOp> message;

			try {
				message = generator.addBinary(
						ERROR_ID.sub("object_value_stub"),
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

	DEFAULT_OBJ_VALUE(ID.id("_o42a_obj_value"), true) {

		@Override
		public void write(DefDirs dirs, ObjectIRData.Op data) {

			final Block code = dirs.code();
			final ObjectOp owner = dirs.getBuilder().owner();

			data.claimFunc(code).load(null, code).call(dirs, owner);
			data.propositionFunc(code).load(null, code).call(dirs, owner);
		}

	};

	private final ID id;
	private final boolean typeAware;

	PredefObjValue(ID id, boolean typeAware) {
		this.id = id;
		this.typeAware = typeAware;
	}

	public final ID getId() {
		return this.id;
	}

	public final boolean isTypeAware() {
		return this.typeAware;
	}

	public final FuncPtr<ObjectValueFunc> get(
			CompilerContext context,
			Generator generator,
			ValueType<?> valueType) {
		return predefObjValues(generator).get(context, this, valueType);
	}

	public abstract void write(DefDirs dirs, ObjectIRData.Op data);

}
