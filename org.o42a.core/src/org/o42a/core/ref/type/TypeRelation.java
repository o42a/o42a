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
package org.o42a.core.ref.type;


public enum TypeRelation {

	SAME(),
	ASCENDANT(),
	DERIVATIVE(),
	INCOMPATIBLE(),
	PREFERRED(),
	INVALID();

	public final boolean isPreferred() {
		if (isError()) {
			return this != INVALID;
		}
		return isDerivative();
	}

	public final boolean isError() {
		return ordinal() >= INCOMPATIBLE.ordinal();
	}

	public final boolean isAscendant() {
		return this == SAME || this == ASCENDANT;
	}

	public final boolean isDerivative() {
		return this == SAME || this == DERIVATIVE;
	}

	public TypeRelation revert(boolean revert) {
		return revert ? revert() : this;
	}

	public TypeRelation revert() {
		switch (this) {
		case SAME:
		case INCOMPATIBLE:
			return this;
		case ASCENDANT:
			return DERIVATIVE;
		case DERIVATIVE:
			return ASCENDANT;
		case PREFERRED:
			return INVALID;
		case INVALID:
			return PREFERRED;
		}
		throw new IllegalStateException(
				"Can not revert type relation: " + this);
	}

}
