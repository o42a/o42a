/*
    Root Object Definition
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.root.adapter;

import static org.o42a.root.adapter.ParseFunc.PARSE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;


public abstract strictfp class FloatByString extends ByString<Double> {

	private static final int PLUS_SIGN = 0x002b;
	private static final int COMMA = 0x002c;
	private static final int HYPHEN_MINUS = 0x002d;
	private static final int PERIOD = 0x002e;
	private static final int CAPITAL_E = 0x0045;
	private static final int SMALL_E = 0x0065;
	private static final int MINUS_SIGN = 0x2212;

	private static final byte PARSE_SIGN = 0;
	private static final byte PARSE_INT_MANTISSA = 1;
	private static final byte PARSE_FRAC_MATISSA = 2;
	private static final byte PARSE_EXPONENT_SIGN = 3;
	private static final byte PARSE_EXPONENT = 4;

	public FloatByString(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	protected Double byString(
			LocationInfo location,
			Resolver resolver,
			String input) {

		final int len = input.length();

		if (len == 0) {
			if (reportError(resolver)) {
				getLogger().error(
						"empty_input",
						location,
						"Empty string can not be converted to float");
			}
			return null;
		}

		double int_mantissa = 0.0;
		double frac_mantissa = 0.0;
		int frac_mantissa_len = 0;
		double exponent = 0.0;

		byte stage = PARSE_SIGN;
		boolean space = false;
		boolean negative = false;
		double value = 0.0;
		int i = 0;

		do {

			final int c = input.codePointAt(i);

			i += Character.charCount(c);

			switch (c) {
			case HYPHEN_MINUS:
			case MINUS_SIGN:
				if (stage != PARSE_SIGN && stage != PARSE_EXPONENT_SIGN) {
					break;
				}
				negative = true;
				++stage;
				continue;
			case PLUS_SIGN:
				if (stage != PARSE_SIGN && stage != PARSE_EXPONENT_SIGN) {
					break;
				}
				++stage;
				continue;
			case COMMA:
			case PERIOD:
				if (stage > PARSE_INT_MANTISSA) {
					break;
				}
				if (space) {
					if (reportError(resolver)) {
						getLogger().error(
								"invalid_input",
								location,
								"Unexpected space in number at position %d",
								i);
					}
					return null;
				}
				int_mantissa = negative ? -value : value;
				space = false;
				value = 0.0;
				stage = PARSE_FRAC_MATISSA;
				continue;
			case CAPITAL_E:
			case SMALL_E:
				if (stage >= PARSE_EXPONENT) {
					break;
				}
				if (space) {
					if (reportError(resolver)) {
						getLogger().error(
								"invalid_input",
								location,
								"Unexpected space in number at position %d",
								i);
					}
					return null;
				}
				if (stage == PARSE_FRAC_MATISSA) {
					frac_mantissa = negative ? -value : value;
				} else {
					int_mantissa = negative ? -value : value;
				}
				space = false;
				value = 0.0;
				negative = false;
				stage = PARSE_EXPONENT_SIGN;
				continue;
			default:
				if (Character.getType(c) == Character.SPACE_SEPARATOR) {
					if (space) {
						if (reportError(resolver)) {
							getLogger().error(
									"invalid_input",
									location,
									"Two subsequent spaces in number"
									+ " at position %d",
									i);
						}
						return null;
					}
					if (stage == PARSE_SIGN
							|| stage == PARSE_EXPONENT_SIGN
							|| (stage == PARSE_FRAC_MATISSA
									&& frac_mantissa_len == 0)) {
						if (reportError(resolver)) {
							getLogger().error(
									"invalid_input",
									location,
									"Unexpected space in number at position %d",
									i);
						}
						return null;
					}
					space = true;
					continue;
				}
			}

			final int digit = Character.digit(c, 10);

			if (digit < 0) {
				if (reportError(resolver)) {
					getLogger().error(
							"invalid_input",
							location,
							"Illegal character in number at position %d",
							i);
				}
				return null;
			}

			value = value * 10.0 + digit;
			if (hasError(location, resolver, value)) {
				return null;
			}
			if (stage == PARSE_FRAC_MATISSA) {
				++frac_mantissa_len;
			} else if (stage == PARSE_SIGN) {
				stage = PARSE_INT_MANTISSA;
			}

			space = false;
		} while (i < len);

		if (space) {
			if (reportError(resolver)) {
				getLogger().error(
						"invalid_input",
						location,
						"Unexpected space after number at position %d",
						len - 1);
			}
			return null;
		}

		switch (stage) {
		case PARSE_INT_MANTISSA:
			int_mantissa = negative ? -value : value;
			break;
		case PARSE_FRAC_MATISSA:
			frac_mantissa = negative ? -value : value;
			break;
		case PARSE_EXPONENT:
			exponent = negative ? -value : value;
			break;
		default:
			if (reportError(resolver)) {
				getLogger().error(
						"empty_input",
						location,
						"Unexpected end of floating-point number input");
			}
			return null;
		}

		double res;

		if (frac_mantissa_len == 0) {
			res = int_mantissa;
		} else {

			final double pow = Math.pow(10.0, -frac_mantissa_len);

			if (hasError(location, resolver, pow)) {
				return null;
			}
			res = frac_mantissa * pow + int_mantissa;
			if (hasError(location, resolver, res)) {
				return null;
			}
		}
		if (stage == PARSE_EXPONENT) {

			final double pow = Math.pow(10.0, exponent);

			if (hasError(location, resolver, pow)) {
				return null;
			}
			res = res * pow;
			if (hasError(location, resolver, res)) {
				return null;
			}
		}

		return res;
	}

	@Override
	protected ValOp parse(ValDirs dirs, ValOp inputVal) {

		final ParseFunc parseFunc =
				parseFunc(dirs.getGenerator()).op(null, dirs.code());

		return parseFunc.parse(dirs, inputVal);
	}

	private FuncPtr<ParseFunc> parseFunc(Generator generator) {
		return generator.externalFunction()
				.link("o42a_float_by_str", PARSE);
	}

	private boolean hasError(
			LocationInfo location,
			Resolver resolver,
			double x) {
		if (Double.isNaN(x)) {
			if (reportError(resolver)) {
				getLogger().error("nan", location, "Not a number");
			}
			return true;
		}
		if (Double.isInfinite(x)) {
			if (reportError(resolver)) {
				getLogger().error(
						"float_overflow",
						location,
						"Floating point number overflow");
			}
			return true;
		}

		final double abs = Math.abs(x);

		if (abs < Double.MIN_NORMAL && x > 0.0) {
			if (reportError(resolver)) {
				getLogger().error(
						"float_underflow",
						location,
						"Floating point number underflow");
			}
			return true;
		}
		return false;
	}

}
