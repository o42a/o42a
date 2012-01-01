/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.ast.expression.ArgumentNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


final class ArrayObject extends Obj {

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

	public final ArrayConstructor getConstructor() {
		return this.constructor;
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
		return new Ascendants(this).setAncestor(
				this.constructor.ancestor(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected ValueStruct<?, ?> determineValueStruct() {
		if (!this.typeByItems) {
			return super.determineValueStruct();
		}
		return getArray().getValueStruct();
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Array array = getArray();
		final ValueDef def =
				array.getValueStruct().constantDef(this, this, array);

		return def.toDefinitions();
	}

	private Array createArray() {

		final ArrayValueStruct valueStruct;
		TypeRef arrayItemType;

		if (!this.typeByItems) {
			valueStruct = (ArrayValueStruct) value().getValueStruct();
			arrayItemType = valueStruct.getItemTypeRef();
		} else {
			valueStruct = null;
			arrayItemType = null;
		}

		final ArgumentNode[] argNodes =
				this.constructor.getNode().getArguments();
		final ArrayItem[] items = new ArrayItem[argNodes.length];
		final Distributor enclosing = distributeIn(getEnclosingContainer());

		for (int i = 0; i < argNodes.length; ++i) {

			final ArgumentNode argNode = argNodes[i];
			final ExpressionNode itemNode = argNode.getValue();
			final Location location = new Location(getContext(), itemNode);

			if (itemNode != null) {

				final Ref itemRef = itemNode.accept(
						this.constructor.ip().expressionVisitor(),
						enclosing);

				if (itemRef != null) {

					final Ref rescopedItemRef = itemRef.rescope(getScope());
					final TypeRef itemType = rescopedItemRef.ancestor(itemRef);

					if (arrayItemType == null) {
						arrayItemType = itemType;
					} else if (!this.typeByItems) {
						itemType.checkDerivedFrom(arrayItemType);
					} else {
						arrayItemType = arrayItemType.commonAscendant(itemType);
					}

					items[i] = new ArrayItem(i, rescopedItemRef);

					continue;
				}
			}

			items[i] = new ArrayItem(i, errorRef(location, distribute()));
		}

		final ArrayValueStruct finalValueStruct;

		if (!this.typeByItems) {
			finalValueStruct = valueStruct;
		} else if (arrayItemType != null) {
			finalValueStruct = new ArrayValueStruct(
					arrayItemType,
					this.constructor.isConstant());
		} else {
			finalValueStruct = new ArrayValueStruct(
					ValueType.VOID.typeRef(this, getScope()),
					this.constructor.isConstant());
		}

		return new Array(this, distribute(), finalValueStruct, items);
	}

	private Array reproduceArray() {

		final Array array = this.reproducedFrom.getArray();
		final Array reproduced = array.reproduce(this.reproducer);

		if (reproduced != null) {
			return reproduced;
		}

		return new Array(
				array,
				this.reproducer.distribute(),
				new ArrayValueStruct(
						errorRef(
								array.getValueStruct().getItemTypeRef(),
								distribute()).toTypeRef(),
						array.isConstant()),
				new ArrayItem[0]);
	}

}
