/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.type.impl;

import org.o42a.core.object.ObjectType;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.CompilerLogger;


public final class StaticTypeRelation extends TypeRelation {

	public StaticTypeRelation(
			TypeRef of,
			TypeRef to,
			boolean parametersIgnored) {
		super(of, to, parametersIgnored);
	}

	@Override
	public TypeRelation ignoreParameters() {
		return new StaticTypeRelation(of(), to(), true);
	}

	@Override
	public TypeRelation revert() {
		return new StaticTypeRelation(to(), of(), parametersIgnored());
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

		if (type1.getObject().getScope().is(type2.getObject().getScope())) {
			return Kind.SAME;
		}
		if (type2.derivedFrom(type1)) {
			return Kind.ASCENDANT;
		}
		if (type1.derivedFrom(type2)) {
			return Kind.DERIVATIVE;
		}
		if (logger != null) {
			logger.incompatible(to().getLocation(), of());
		}

		return Kind.INCOMPATIBLE;
	}

}
