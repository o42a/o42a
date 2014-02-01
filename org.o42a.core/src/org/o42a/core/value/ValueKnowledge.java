/*
    Compiler Core
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
package org.o42a.core.value;


public enum ValueKnowledge {

	FALSE_VALUE(Condition.FALSE),
	KNOWN_VALUE(Condition.TRUE),
	INITIALLY_KNOWN_VALUE(Condition.TRUE),
	RUNTIME_CONSTRUCTED_VALUE(Condition.TRUE),
	VARIABLE_VALUE(Condition.TRUE),
	RUNTIME_VALUE(Condition.RUNTIME);

	private final Condition condition;

	ValueKnowledge(Condition condition) {
		this.condition = condition;
	}

	public final Condition getCondition() {
		return this.condition;
	}

	public final boolean hasCompilerValue() {
		if (!isKnownToCompiler()) {
			return false;
		}
		return ordinal() >= KNOWN_VALUE.ordinal();
	}

	public final boolean isKnown() {
		return ordinal() <= KNOWN_VALUE.ordinal();
	}

	public final boolean isInitiallyKnown() {
		return ordinal() <= INITIALLY_KNOWN_VALUE.ordinal();
	}

	public final boolean isFalse() {
		return getCondition().isFalse();
	}

	public final boolean isKnownToCompiler() {
		return this != RUNTIME_VALUE;
	}

	public final boolean isVariable() {
		return this == INITIALLY_KNOWN_VALUE || this == VARIABLE_VALUE;
	}

}
