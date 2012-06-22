/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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


import org.o42a.core.object.ObjectType;
import org.o42a.core.source.CompilerLogger;


final class StaticTypeRelation extends TypeRelation {

	StaticTypeRelation(TypeRef of, TypeRef to) {
		super(of, to);
	}

	@Override
	public TypeRelation revert() {
		return new StaticTypeRelation(to(), of());
	}

	@Override
	protected Kind relationKind(
			CompilerLogger logger,
			boolean checkDerivationOnly) {
		if (!to().isValid()) {
			return Kind.PREFERRED;
		}
		if (!of().isValid()) {
			return Kind.INVALID;
		}

		final ObjectType type1 =
				of().getType().type().getLastDefinition().type();
		final ObjectType type2 =
				to().getType().type().getLastDefinition().type();

		if (type1.getObject().getScope() == type2.getObject().getScope()) {
			return Kind.SAME;
		}
		if (type2.derivedFrom(type1)) {
			return Kind.ASCENDANT;
		}
		if (type1.derivedFrom(type2)) {
			return Kind.DERIVATIVE;
		}
		if (logger != null) {
			logger.incompatible(to(), of());
		}

		return Kind.INCOMPATIBLE;
	}

}
