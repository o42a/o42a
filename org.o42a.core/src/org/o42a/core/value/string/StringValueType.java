/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.value.type.ValueIRDesc.EXTERN_VALUE_IR_DESC;
import static org.o42a.util.string.StringCodec.escapeControlChars;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.type.StaticsIR;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.*;


public final class StringValueType extends SingleValueType<String> {

	public static StringValueType INSTANCE = new StringValueType();

	private StringValueType() {
		super("string", String.class);
	}

	@Override
	public SingleValueStruct<String> struct() {
		return ValueStruct.STRING;
	}

	@Override
	public Obj typeObject(Intrinsics intrinsics) {
		return intrinsics.getString();
	}

	@Override
	public Path path(Intrinsics intrinsics) {
		return Path.ROOT_PATH.append(
				typeObject(intrinsics).getScope().toField().getKey());
	}

	@Override
	public String valueString(String value) {

		final StringBuilder out = new StringBuilder(value.length() + 2);

		out.append('"');
		escapeControlChars(out, value);
		out.append('"');

		return out.toString();
	}

	@Override
	public ValueIRDesc irDesc() {
		return EXTERN_VALUE_IR_DESC;
	}

	@Override
	protected StaticsIR<String> createStaticsIR(Generator generator) {
		return new StringStaticsIR(generator, this);
	}

	@Override
	protected KeeperIR<?, ?> createKeeperIR(
			TypeParameters<String> parameters,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		return new StringKeeperIR(parameters, bodyIR, keeper);
	}

}
