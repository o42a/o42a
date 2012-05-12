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
package org.o42a.core.ir.object.impl.value;

import static org.o42a.core.ir.object.impl.value.PredefObjValues.predefObjValues;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.source.CompilerContext;


public enum PredefObjValue {

	FALSE_OBJ_VALUE() {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value_false");
		}

		@Override
		public void write(DefDirs dirs) {
			dirs.code().go(dirs.falseDir());
		}

	},

	VOID_OBJ_VALUE() {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value_void");
		}

		@Override
		public void write(DefDirs dirs) {
			dirs.returnValue(voidValue().op(dirs.getBuilder(), dirs.code()));
		}

	},

	STUB_OBJ_VALUE() {

		@Override
		public CodeId codeId(Generator generator) {
			return generator.id("_o42a_obj_value_stub");
		}

		@Override
		public void write(DefDirs dirs) {
			dirs.code().debug("Object value stub invoked");
			dirs.code().go(dirs.falseDir());
		}

	};

	public abstract CodeId codeId(Generator generator);

	public final FuncPtr<ObjectValueFunc> get(
			CompilerContext context,
			Generator generator) {
		return predefObjValues(generator).get(context, this);
	}

	public abstract void write(DefDirs dirs);

}
