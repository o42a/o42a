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

import static org.o42a.core.ref.impl.ResolutionRootFinder.resolutionRoot;

import org.o42a.core.Scope;
import org.o42a.core.object.ObjectType;
import org.o42a.core.source.CompilerLogger;


final class DefaultTypeRelation extends TypeRelation {

	DefaultTypeRelation(TypeRef of, TypeRef to) {
		super(of, to);
	}

	@Override
	public TypeRelation revert() {
		return new DefaultTypeRelation(to(), of());
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

		final Scope root1 = resolutionRoot(of());
		final Scope root2 = resolutionRoot(to());

		final ObjectType type1 = of().getType().type();
		final ObjectType type2 = to().getType().type();

		if (root1.is(root2)) {
			if (type1.getObject().is(type2.getObject())) {

				final TypeRelation.Kind structRelation =
						of().getValueStruct().relationTo(to().getValueStruct());

				if (logger != null && structRelation == Kind.INCOMPATIBLE) {
					logger.incompatible(to(), of().getValueStruct());
				}

				return structRelation;
			}
			if (type1.derivedFrom(type2)) {
				return checkDerivative(logger);
			}
			if (checkDerivationOnly) {
				if (logger != null) {
					logger.notDerivedFrom(of(), to());
				}
				return Kind.INCOMPATIBLE;
			}
			if (type2.derivedFrom(type1)) {
				return checkAscendant(logger);
			}
			if (logger != null) {
				logger.incompatible(to(), of());
			}
			return Kind.INCOMPATIBLE;
		}

		if (root2.contains(root1)) {
			if (type1.derivedFrom(type2)) {
				return checkDerivative(logger);
			}
			if (logger != null) {
				logger.notDerivedFrom(of(), to());
			}
			return Kind.INCOMPATIBLE;
		}
		if (checkDerivationOnly) {
			if (logger != null) {
				logger.notDerivedFrom(of(), to());
			}
			return Kind.INCOMPATIBLE;
		}

		if (root1.contains(root2)) {
			if (type2.derivedFrom(type1)) {
				return checkAscendant(logger);
			}
			if (logger != null) {
				logger.notDerivedFrom(to(), of());
			}
			return Kind.INCOMPATIBLE;
		}

		if (logger != null) {
			logger.incompatible(to(), of());
		}

		return Kind.INCOMPATIBLE;
	}

	private Kind checkAscendant(CompilerLogger logger) {
		if (!assignable(of(), to())) {
			if (logger != null) {
				logger.incompatible(to(), of().getParameters());
			}
			return Kind.INCOMPATIBLE;
		}
		return Kind.ASCENDANT;
	}

	private Kind checkDerivative(CompilerLogger logger) {
		if (!assignable(to(), of())) {
			if (logger != null) {
				logger.incompatible(of(), to().getParameters());
			}
			return Kind.INCOMPATIBLE;
		}
		return Kind.DERIVATIVE;
	}

	private static boolean assignable(TypeRef dest, TypeRef value) {

		final TypeParameters destParameters = dest.getParameters();

		if (destParameters.assignableFrom(value.getParameters())) {
			return true;
		}
		if (dest.getValueType().isVoid()) {
			return true;
		}

		return false;
	}

}
