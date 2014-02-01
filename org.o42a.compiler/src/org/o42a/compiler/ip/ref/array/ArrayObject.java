/*
    Compiler
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
package org.o42a.compiler.ip.ref.array;

import static org.o42a.core.ref.Ref.errorRef;

import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.ConstructedObject;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;


final class ArrayObject extends ConstructedObject {

	private final ArrayConstructor constructor;
	private final boolean typeByItems;
	private final Reproducer reproducer;
	private final ArrayObject reproducedFrom;
	private Array array;

	ArrayObject(ArrayConstructor constructor, boolean typeByItems) {
		super(constructor, constructor.distribute());
		this.constructor = constructor;
		this.typeByItems = typeByItems;
		this.reproducer = null;
		this.reproducedFrom = null;
	}

	ArrayObject(
			ArrayConstructor constructor,
			Reproducer reproducer,
			ArrayObject reproducedFrom) {
		super(constructor, constructor.distribute());
		this.constructor = constructor;
		this.typeByItems = reproducedFrom.typeByItems;
		this.reproducer = reproducer;
		this.reproducedFrom = reproducedFrom;
	}

	public Array getArray() {
		if (this.array != null) {
			return this.array;
		}
		if (this.reproducedFrom == null) {
			return this.array = createArray();
		}
		return this.array = reproduceArray();
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this)
		.setAncestor(this.constructor.ancestor(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected TypeParameters<?> determineTypeParameters() {
		if (!this.typeByItems) {
			return super.determineTypeParameters();
		}
		return getArray().getTypeParameters();
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Def def = new ArrayConstantDef(this, getArray());

		return def.toDefinitions(type().getParameters());
	}

	private Array createArray() {
		return new Builder(this).createArray(
				this.constructor.getAccessRules().distribute(
						distributeIn(getEnclosingContainer())),
				getScope());
	}

	private Array reproduceArray() {

		final Array array = this.reproducedFrom.getArray();
		final Array reproduced = array.reproduce(this.reproducer);

		if (reproduced != null) {
			return reproduced;
		}

		final ArrayValueType valueType = array.getValueType();

		return new Array(
				array,
				this.reproducer.distribute(),
				valueType.typeParameters(
						errorRef(
								valueType.itemTypeRef(
										array.getTypeParameters()),
								distribute()).toTypeRef()),
				new ArrayItem[0]);
	}

	private static final class Builder extends ArrayBuilder {

		private final ArrayObject object;

		Builder(ArrayObject object) {
			super(object.constructor);
			this.object = object;
		}

		@Override
		protected ArrayValueType arrayType() {
			return ArrayValueType.ROW;
		}

		@Override
		protected boolean typeByItems() {
			return getConstructor().typeByItems();
		}

		@Override
		protected TypeParameters<Array> knownTypeParameters() {
			return this.object.type().getParameters().toArrayParameters();
		}

	}

}
