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
package org.o42a.core.ref.type;

import org.o42a.core.source.CompilerLogger;


public abstract class TypeRelation {

	private final TypeRef of;
	private final TypeRef to;
	private Kind kind;
	private final boolean parametersIgnored;

	public TypeRelation(TypeRef of, TypeRef to, boolean parametersIgnored) {
		this.of = of;
		this.to = to;
		this.parametersIgnored = parametersIgnored;
		of.assertSameScope(to);
	}

	public final TypeRef of() {
		return this.of;
	}

	public final TypeRef to() {
		return this.to;
	}

	public final boolean parametersIgnored() {
		return this.parametersIgnored;
	}

	public Kind getKind() {
		if (this.kind != null) {
			return this.kind;
		}
		return this.kind = relationKind(null, false);
	}

	public final boolean isPreferredDerivative() {
		return getKind().isPreferredDerivative();
	}

	public final boolean isPreferredAscendant() {
		return getKind().isPreferredAscendant();
	}

	public final boolean isError() {
		return getKind().isError();
	}

	public final boolean isAscendant() {
		return getKind().isAscendant();
	}

	public final boolean isDerivative() {
		return relationKind(null, true).isDerivative();
	}

	public final boolean isSame() {
		return getKind().isSame();
	}

	public abstract TypeRelation ignoreParameters();

	public final TypeRelation revert(boolean revert) {
		return revert ? revert() : this;
	}

	public abstract TypeRelation revert();

	public final TypeRelation check(CompilerLogger logger) {
		this.kind = relationKind(logger, false);
		return this;
	}

	public final boolean checkDerived(CompilerLogger logger) {
		return relationKind(logger, true).isDerivative();
	}

	public final TypeRef commonDerivative() {
		return getKind().isPreferredDerivative() ? of() : to();
	}

	public final TypeRef commonAscendant() {
		return getKind().isPreferredAscendant() ? of() : to();
	}

	@Override
	public String toString() {
		return "TypeRelation[of " + this.of + " to " + this.to + ']';
	}

	protected abstract Kind relationKind(
			CompilerLogger logger,
			boolean checkDerivationOnly);

	public enum Kind {

		SAME(),
		ASCENDANT(),
		DERIVATIVE(),
		INCOMPATIBLE(),
		PREFERRED(),
		INVALID();

		public final boolean isPreferredDerivative() {
			if (isError()) {
				return this != INVALID;
			}
			return isDerivative();
		}

		public final boolean isPreferredAscendant() {
			if (isError()) {
				return this != INVALID;
			}
			return isAscendant();
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

		public final boolean isSame() {
			return this == SAME;
		}

		public Kind revert(boolean revert) {
			return revert ? revert() : this;
		}

		public Kind revert() {
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

}
