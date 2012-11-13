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
package org.o42a.core.value.macro.impl;

import static org.o42a.core.value.macro.impl.PrefixedMacro.prefixMacro;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.core.value.Value;
import org.o42a.core.value.macro.Macro;


public class MacroValueStruct extends SingleValueStruct<Macro> {

	public static final MacroValueStruct INSTANCE = new MacroValueStruct();

	private MacroValueStruct() {
		super(MacroValueType.INSTANCE);
	}

	@Override
	protected Value<Macro> prefixValueWith(
			Value<Macro> value,
			PrefixPath prefix) {
		if (!value.getKnowledge().hasCompilerValue()) {
			return value;
		}

		final Macro oldMacro = value.getCompilerValue();
		final Macro newMacro = prefixMacro(prefix, oldMacro);

		if (oldMacro == newMacro) {
			return value;
		}

		return compilerValue(newMacro);
	}

	@Override
	protected ValueStructIR<SingleValueStruct<Macro>, Macro> createIR(
			Generator generator) {
		return new MacroValueStructIR(generator, this);
	}

}
