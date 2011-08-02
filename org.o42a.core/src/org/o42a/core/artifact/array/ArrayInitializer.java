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

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public class ArrayInitializer extends Placed {

	public static ArrayInitializer invalidArrayInitializer(
			LocationInfo location,
			Distributor distributor) {
		return new ArrayInitializer(location, distributor);
	}

	public static ArrayInitializer valueArrayInitializer(Ref value) {
		return new ValueArrayInitializer(value);
	}

	public static ArrayInitializer arrayInitializer(
			CompilerContext context,
			LogInfo location,
			Distributor distributor,
			TypeRef itemType,
			Ref[] items) {
		return new ArrayInitializer(
				context,
				location,
				distributor,
				itemType,
				items);
	}

	TypeRef itemType;
	Ref[] items;

	private ArrayInitializer(
			LocationInfo location,
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
			Ref[] items) {
		super(context, location, distributor);
		this.itemType = itemType;
		this.items = items;
	}

	private ArrayInitializer(
			LocationInfo location,
			Distributor distributor,
			TypeRef itemType,
			Ref[] items) {
		super(location, distributor);
		this.items = items;
		this.itemType = itemType;
	}

	public boolean isValid() {
		return getItems() != null;
	}

	public TypeRef getItemType() {
		return this.itemType;
	}

	public Ref[] getItems() {
		return this.items;
	}

	public ArrayInitializer toStatic() {
		if (!isValid()) {
			return this;
		}

		final StaticTypeRef itemType = this.itemType.toStatic();

		boolean changed = itemType != this.itemType;

		final Ref[] items = new Ref[this.items.length];

		for (int i = 0; i < items.length; ++i) {

			final Ref item = this.items[i];
			final Ref staticItem = item.toStatic();

			items[i] = staticItem;
			changed |= item != staticItem;
		}

		if (!changed) {
			return this;
		}

		return new ArrayInitializer(
				this,
				distribute(),
				itemType,
				items);
	}

	public ArrayTypeRef arrayTypeRef(TypeRef expectedItemType) {
		if (this.items.length == 0) {
			return arrayTypeObject(voidRef(this, distribute()), 1);
		}

		final TypeRef expected =
			expectedItemType != null
			? expectedItemType.upgradeScope(getScope()) : null;
		ArrayTypeRef type = null;

		for (Ref item : this.items) {

			final ArrayTypeRef itemType = arrayTypeRef(item, expected);

			if (itemType == null) {
				return null;
			}
			if (type == null) {
				type = itemType;
				continue;
			}

			final ArrayTypeRef commonInheritant =
				type.commonDerivative(itemType);

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

	public void resolveAll(Resolver resolver) {

		final TypeRef itemType = getItemType();

		if (itemType != null) {
			itemType.resolveAll(resolver);
		}

		final Ref[] items = getItems();

		if (items != null) {
			for (Ref item : items) {
				item.resolveAll(resolver);
			}
		}
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

	private ArrayTypeRef arrayTypeRef(Ref item, TypeRef expectedItemType) {

		final Array array = item.getResolution().toArray();

		if (array != null) {
			return array.getInitializer().arrayTypeRef(expectedItemType);
		}

		final TypeRef itemType = item.ancestor(item);

		if (expectedItemType != null) {

			final TypeRelation relation = expectedItemType.relationTo(itemType);

			if (!relation.isAscendant()) {
				if (!relation.isError()) {
					getLogger().notDerivedFrom(itemType, expectedItemType);
				}
				return null;
			}
		}

		return ArrayTypeRef.arrayTypeRef(itemType, 0);
	}

	private static final class ValueArrayInitializer extends ArrayInitializer {

		private final Ref value;
		private boolean invalid;

		ValueArrayInitializer(Ref value) {
			super(value, value.distribute());
			this.value = value;
		}

		@Override
		public TypeRef getItemType() {
			if (this.itemType != null) {
				return this.itemType;
			}

			final Array array = getArray();

			if (array == null) {
				return null;
			}

			return this.itemType = array.getArrayTypeRef().getItemTypeRef();
		}

		@Override
		public Ref[] getItems() {
			if (this.items != null) {
				return this.items;
			}

			final Array array = getArray();

			if (array == null) {
				return null;
			}

			return this.items = array.getInitializer().getItems();
		}

		@Override
		public ArrayInitializer toStatic() {
			if (!isValid()) {
				return this;
			}

			final Ref staticValue = this.value.toStatic();

			if (staticValue == this.value) {
				return this;
			}

			return new ValueArrayInitializer(staticValue);
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

		private Array getArray() {
			if (this.invalid) {
				return null;
			}

			final Array array = this.value.getResolution().toArray();

			if (array == null) {
				getLogger().error("not_array", this, "Not array");
				this.invalid = true;
			}
			return array;
		}

	}

}
