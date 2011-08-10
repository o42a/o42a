/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array.impl.decl;

import static org.o42a.core.artifact.array.ArrayTypeRef.arrayTypeRef;

import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.array.ArrayTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;


final class OverriderArray extends Array {

	private final ArrayFieldVariant variant;
	private boolean built;

	OverriderArray(ArrayFieldVariant variant) {
		super(
				variant.getField(),
				variant.getArrayField().getOverridden()[0].getArtifact());
		this.variant = variant;
	}

	@Override
	public boolean isValid() {
		return super.isValid() && this.variant.validate();
	}

	@Override
	public String toString() {
		return this.variant.toString();
	}

	@Override
	protected ArrayTypeRef buildTypeRef() {
		build();
		return this.variant.getTypeRef();
	}

	@Override
	protected TypeRef buildItemTypeRef() {
		return null;
	}

	@Override
	protected ArrayInitializer buildInitializer() {
		build();

		final ArrayInitializer initializer = this.variant.getInitializer();

		if (initializer != null) {
			return initializer;
		}

		return this.variant.getArrayField().derivedInitializer();
	}

	private void build() {
		if (this.built) {
			return;
		}
		this.built = true;

		final ArrayTypeRef knownTypeRef = knownTypeRef();

		this.variant.build(knownTypeRef, knownTypeRef.getItemTypeRef());
	}

	private ArrayTypeRef knownTypeRef() {

		final ArrayTypeRef derived =
				this.variant.getArrayField().derivedTypeRef();
		final TypeRef declared =
				this.variant.getArrayField().declaredItemTypeRef();

		if (declared != null) {

			final TypeRelation relation =
					derived.getItemTypeRef().relationTo(declared);

			if (relation.isAscendant()) {
				return arrayTypeRef(declared, derived.getDimension());
			}
			if (!relation.isError()) {
				getLogger().notDerivedFrom(declared, derived);
			}
			this.variant.invalid();
		}

		return derived;
	}

}
