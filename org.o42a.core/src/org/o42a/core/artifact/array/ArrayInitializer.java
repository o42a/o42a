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
package org.o42a.core.artifact.array;

import static org.o42a.core.artifact.array.ArrayTypeRef.arrayTypeObject;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.util.log.LogInfo;


public final class ArrayInitializer extends Placed {

	public static ArrayInitializer invalidArrayInitializer(
			LocationSpec location,
			Distributor distributor) {
		return new ArrayInitializer(location, distributor);
	}

	public static ArrayInitializer arrayInitializer(
			CompilerContext context,
			LogInfo location,
			Distributor distributor,
			TypeRef itemType,
			FieldDefinition[] items) {
		return new ArrayInitializer(
				context,
				location,
				distributor,
				itemType,
				items);
	}

	private final FieldDefinition[] items;
	private final TypeRef itemType;

	private ArrayInitializer(
			LocationSpec location,
			Distributor distributor) {
		super(location, distributor);
		this.itemType = null;
		this.items = null;
	}

	private ArrayInitializer(
			CompilerContext context,
			LogInfo location,
			Distributor distributor,
			TypeRef itemType,
			FieldDefinition[] items) {
		super(context, location, distributor);
		this.itemType = itemType;
		this.items = items;
	}

	public boolean isValid() {
		return this.items != null;
	}

	public TypeRef getItemType() {
		return this.itemType;
	}

	public FieldDefinition[] getItems() {
		return this.items;
	}

	public ArrayTypeRef arrayTypeRef(TypeRef expectedItemType) {
		if (this.items.length == 0) {
			return arrayTypeObject(voidRef(this, distribute()), 1);
		}

		final TypeRef expected =
			expectedItemType != null
			? expectedItemType.upgradeScope(getScope()) : null;
		ArrayTypeRef type = null;

		for (FieldDefinition item : this.items) {

			final ArrayTypeRef itemType = arrayTypeRef(item, expected);

			if (itemType == null) {
				return null;
			}
			if (type == null) {
				type = itemType;
				continue;
			}

			final ArrayTypeRef commonInheritant =
				type.commonInheritant(itemType);

			if (commonInheritant != null) {
				type = commonInheritant;
				continue;
			}

			if (type.getDimension() != itemType.getDimension()) {
				getLogger().unexpectedArrayDimension(
						item,
						itemType.getDimension(),
						type.getDimension());
			} else {
				getLogger().unexpectedType(item, itemType, type);
			}

			return null;
		}

		if (type == null) {
			return ArrayTypeRef.arrayTypeRef(expected, 1);
		}

		return ArrayTypeRef.arrayTypeRef(
				type.getItemTypeRef(),
				type.getDimension() + 1);
	}

	public ArrayTypeRef arrayTypeRef(ArrayTypeRef expectedTypeRef) {
		if (expectedTypeRef == null) {
			return arrayTypeRef((TypeRef) null);
		}

		final ArrayTypeRef result =
			arrayTypeRef(expectedTypeRef.getItemTypeRef());

		if (result.getDimension() != expectedTypeRef.getDimension()) {
			getLogger().unexpectedArrayDimension(
					this,
					result.getDimension(),
					expectedTypeRef.getDimension());
			return null;
		}

		return result;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("ArrayInitializer");
		out.append('[');
		for (int i = 0; i < this.items.length; ++i) {
			if (i != 0) {
				out.append(", ");
			}
			out.append(this.items[i]);
		}
		out.append(']');

		return out.toString();
	}

	private ArrayTypeRef arrayTypeRef(
			FieldDefinition definition,
			TypeRef expectedItemType) {
		if (definition.isArray()) {
			return definition.getArrayInitializer().arrayTypeRef(
					expectedItemType);
		}

		final Ref value = definition.getValue();

		if (value == null) {
			getLogger().notArrayItemInitializer(definition);
			return null;
		}

		final TypeRef itemType = value.toTargetRef().getTypeRef();

		if (expectedItemType != null) {
			if (!itemType.derivedFrom(expectedItemType)) {
				getLogger().unexpectedType(
						definition,
						itemType,
						expectedItemType);
				return null;
			}
		}

		return ArrayTypeRef.arrayTypeRef(itemType, 0);
	}

}
