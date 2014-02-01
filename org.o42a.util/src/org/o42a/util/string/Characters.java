/*
    Utilities
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
package org.o42a.util.string;


public final class Characters {

	public static final int EXCLAMATION_MARK = '!';
	public static final int QUOTATION_MARK = '"';
	public static final int NUMBER_SIGN = '#';
	public static final int DOLLAR_SIGN = '$';
	public static final int PERCENT_SIGN = '%';
	public static final int AMPERSAND = '&';
	public static final int APOSTROPHE = '\'';
	public static final int LEFT_PARENTHESIS = '(';
	public static final int RIGHT_PARENTHESIS = ')';
	public static final int ASTERISK = '*';
	public static final int PLUS_SIGN = '+';
	public static final int COMMA = ',';
	public static final int HYPHEN_MINUS = '-';
	public static final int FULL_STOP = '.';
	public static final int SOLIDUS = '/';
	public static final int COLON = ':';
	public static final int SEMICOLON = ';';
	public static final int LESS_THAN_SIGN = '<';
	public static final int EQUALS_SIGN = '=';
	public static final int GREATER_THAN_SIGN = '>';
	public static final int QUESTION_MARK = '?';
	public static final int COMMERCIAL_AT = '@';
	public static final int LEFT_SQUARE_BRACKET = '[';
	public static final int REVERSE_SOLIDUS = '\\';
	public static final int RIGHT_SQUARE_BRACKET = ']';
	public static final int CIRCUMFLEX_ACCENT = '^';
	public static final int LOW_LINE = '_';
	public static final int GRAVE_ACCENT = '`';
	public static final int LEFT_CURLY_BRACKET = '{';
	public static final int VERTICAL_LINE = '|';
	public static final int RIGHT_CURLY_BRACKET = '}';
	public static final int TILDE = '~';

	public static final int HYPHEN = 0x2010;
	public static final int NON_BREAKING_HYPHEN = 0x2011;
	public static final int HORIZONTAL_ELLIPSIS = 0x2026;

	public static final int NOT_SIGN = 0x00ac;
	public static final int MULTIPLICATION_SIGN = 0x00d7;
	public static final int DOT_OPERATOR = 0x22c5;
	public static final int DIVISION_SIGN = 0x00f7;
	public static final int DIVISION_SLASH = 0x2215;
	public static final int MINUS_SIGN = 0x2212;

	public static final int NOT_EQUAL_TO = 0x2260;
	public static final int LESS_THAN_OR_EQUAL_TO = 0x2264;
	public static final int GREATER_THAN_OR_EQUAL_TO = 0x2265;

	public static final int INFINITY = 0x221E;

	public static boolean isDigit(int c) {
		return '0' <= c && c <= '9';
	}

	public static boolean isHexDigit(int c) {
		if (c < '0') {
			return false;
		}
		if (c <= '9') {
			return true;
		}
		if (c > 'f' || c < 'A') {
			return false;
		}
		if (c >= 'a') {
			return true;
		}
		if (c <= 'F') {
			return true;
		}
		return false;
	}

	private Characters() {
	}

}
