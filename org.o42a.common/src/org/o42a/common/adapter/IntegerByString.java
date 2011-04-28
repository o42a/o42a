/*
    Modules Commons
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
package org.o42a.common.adapter;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;


public class IntegerByString extends ByString<Long> {

	private static final int PLUS_SIGN = 0x002b;
	private static final int HYPHEN_MINUS = 0x002d;
	private static final int MINUS_SIGN = 0x2212;

	public IntegerByString(Obj owner, String name, String sourcePath) {
		super(owner, ValueType.INTEGER, name, sourcePath);
	}

	@Override
	protected Long byString(LocationInfo location, String input) {
		return integerByString(location, input, 10);
	}

	@Override
	protected void parse(Code code, ValOp result, ObjectOp input) {

		final Generator generator = input.getGenerator();
		final ValOp inputValue = input.writeValue(code);
		final ParseWithRadixFunc parseFunc =
			parseFunc(generator).op(null, code);

		parseFunc.parse(code, result, inputValue, 10);
	}

	private FuncPtr<ParseWithRadixFunc> parseFunc(Generator generator) {
		return generator.externalFunction(
				"o42a_int_by_str",
				ParseWithRadixFunc.PARSE_WITH_RADIX);
	}

	private Long integerByString(
			LocationInfo location,
			String input,
			int radix) {

		final int len = input.length();

		if (len == 0) {
			getLogger().error(
					"empty_input",
					location,
					"Empty string can not be converted to integer");
			return null;
		}

		boolean space = false;
		boolean negative = false;
		boolean hasValue = false;
		long value = 0;
		int i = 0;

		do {

			final boolean first = i == 0;
			final int c = input.codePointAt(i);

			i += Character.charCount(c);

			if (first) {
				switch (c) {
				case HYPHEN_MINUS:
				case MINUS_SIGN:
					negative = true;
					continue;
				case PLUS_SIGN:
					continue;
				}
			} else if (Character.getType(c) == Character.SPACE_SEPARATOR) {
				if (space) {
					getLogger().error(
							"invalid_input",
							location,
							"Two subsequent spaces in number at position %d",
							i);
					return null;
				}
				space = true;
				continue;
			}

			final int digit = Character.digit(c, radix);

			if (digit < 0) {
				getLogger().error(
						"invalid_input",
						location,
						"Illegal character in number at position %d",
						i);
				return null;
			}

			if (negative) {
				value = value * radix - digit;
				if (value > 0) {
					getLogger().error(
							"integer_overflow",
							location,
							"Integer overflow");
					return null;
				}
			} else {
				value = value * radix + digit;
				if (value < 0) {
					getLogger().error(
							"integer_overflow",
							location,
							"Integer overflow");
					return null;
				}
			}

			space = false;
			hasValue = true;
		} while (i < len);

		if (space) {
			getLogger().error(
					"invalid_input",
					location,
					"Unexpected space after number at position %d",
					len - 1);
			return null;
		}
		if (!hasValue) {
			getLogger().error(
					"empty_input",
					location,
					"Unexpected end of integer input");
			return null;
		}

		return value;
	}

}
