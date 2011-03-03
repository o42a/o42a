/*
    Compiler Core
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
package org.o42a.core.st;


public final class StatementKinds {

	private static final byte CONDITION_MASK = 0x01;
	private static final byte VALUE_MASK = 0x02;
	private static final byte FIELD_MASK = 0x04;

	public static final StatementKinds NO_STATEMENTS =
		new StatementKinds((byte) 0);
	public static final StatementKinds CLAUSE_DECLARATIONS =
		NO_STATEMENTS;
	public static final StatementKinds CONDITIONS =
		new StatementKinds(CONDITION_MASK);
	public static final StatementKinds VALUES =
		new StatementKinds(VALUE_MASK);
	public static final StatementKinds FIELD_DECLARATIONS =
		new StatementKinds(FIELD_MASK);

	private static final byte DEFINITION_MASK = CONDITION_MASK | VALUE_MASK;
	private static final byte DECLARATION_MASK = VALUE_MASK | FIELD_MASK;

	private final byte mask;

	private StatementKinds(byte mask) {
		this.mask = mask;
	}

	public final boolean isEmpty() {
		return this.mask == 0;
	}

	public final boolean haveCondition() {
		return (this.mask & CONDITION_MASK) != 0;
	}

	public final boolean onlyConditions() {
		return this.mask == CONDITION_MASK;
	}

	public final boolean haveValue() {
		return (this.mask & VALUE_MASK) != 0;
	}

	public final boolean haveField() {
		return (this.mask & FIELD_MASK) != 0;
	}

	public final boolean haveDefinition() {
		return (this.mask & DEFINITION_MASK) != 0;
	}

	public final boolean haveDeclaration() {
		return (this.mask & DECLARATION_MASK) != 0;
	}

	public StatementKinds add(StatementKinds other) {

		final byte mask = (byte) (this.mask | other.mask);

		if (mask == this.mask) {
			return this;
		}
		if (mask == other.mask) {
			return other;
		}

		return new StatementKinds(mask);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "Statements[]";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("Statements[");
		if (haveCondition()) {
			out.append("logical");
			comma = true;
		}
		if (haveValue()) {
			if (comma) {
				out.append(", ");
			}
			out.append("value");
			comma = true;
		}
		if (haveField()) {
			if (comma) {
				out.append(", ");
			}
			out.append("field");
		}
		out.append(']');

		return out.toString();
	}

}
