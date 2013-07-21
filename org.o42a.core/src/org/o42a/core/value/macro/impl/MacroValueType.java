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
package org.o42a.core.value.macro.impl;

import static org.o42a.core.ir.value.type.ValueIRDesc.VOID_VALUE_IR_DESC;
import static org.o42a.core.value.macro.impl.PrefixedMacro.prefixMacro;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.Value;
import org.o42a.core.value.macro.Macro;


public final class MacroValueType extends SingleValueType<Macro> {

	public static final MacroValueType INSTANCE = new MacroValueType();

	private MacroValueType() {
		super("macro", Macro.class);
	}

	@Override
	public Obj typeObject(Intrinsics intrinsics) {
		return intrinsics.getMacro();
	}

	@Override
	public Path path(Intrinsics intrinsics) {
		return Path.ROOT_PATH.append(
				typeObject(intrinsics).getScope().toField().getKey());
	}

	@Override
	public ValueIRDesc irDesc() {
		return VOID_VALUE_IR_DESC;
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

		return value.getTypeParameters()
				.prefixWith(prefix)
				.compilerValue(newMacro);
	}

	@Override
	protected ValueTypeIR<Macro> createIR(Generator generator) {
		return new MacroValueTypeIR(generator, this);
	}

}
