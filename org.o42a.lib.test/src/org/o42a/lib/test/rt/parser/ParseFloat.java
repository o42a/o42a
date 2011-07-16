/*
    Test Framework
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
package org.o42a.lib.test.rt.parser;

import org.o42a.common.adapter.FloatByString;
import org.o42a.common.source.SingleURLSource;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;


final class ParseFloat extends FloatByString {

	private static final URLSourceTree PARSE_FLOAT =
			new SingleURLSource(Parser.PARSER, "float.o42a");

	ParseFloat(Parser parser) {
		super(compileField(parser, PARSE_FLOAT));
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = super.calculateBuiltin(resolver);

		if (!value.getLogicalValue().isTrue()) {
			return value;
		}

		return value().getValueType().runtimeValue();
	}

	@Override
	protected strictfp ValOp parse(ValDirs dirs, ValOp inputVal) {
		dirs.code().debug("Run-time float");
		return super.parse(dirs, inputVal);
	}

}
