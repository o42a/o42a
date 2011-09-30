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
package org.o42a.core.artifact.array;

import static org.o42a.core.ref.Ref.errorRef;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.impl.ArrayContentReproducer;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public class Array extends Placed {

	private final Obj owner;
	private final ArrayValueStruct valueStruct;
	private final ArrayItem[] items;

	public Array(
			LocationInfo location,
			Distributor distributor,
			ArrayValueStruct valueStruct,
			ArrayItem[] items) {
		super(location, distributor);
		this.owner = owner(distributor.getScope());
		valueStruct.getItemTypeRef().assertSameScope(distributor);
		this.valueStruct = valueStruct;
		this.items = items;
	}

	public final Obj getOwner() {
		return this.owner;
	}

	public final ArrayValueStruct getValueStruct() {
		return this.valueStruct;
	}

	public final boolean isConstant() {
		return this.valueStruct.isConstant();
	}

	public final ArrayItem[] items(Scope scope) {
		assertCompatible(scope);
		// TODO Construct array items for the given scope.
		return this.items;
	}

	public final Ref toRef() {
		return getValueStruct().constantRef(this, distribute(), this);
	}

	public Array reproduce(Reproducer reproducer) {
		assertCompatibleScope(reproducer.getReproducingScope());

		final Distributor distributor = distribute();
		final ArrayContentReproducer contentReproducer =
				new ArrayContentReproducer(
						this,
						distributor,
						reproducer);

		final ArrayValueStruct valueStruct =
				getValueStruct().reproduce(contentReproducer);

		if (valueStruct == null) {
			return null;
		}

		final ArrayItem[] items = reproduceItems(
				items(reproducer.getReproducingScope()),
				contentReproducer);

		return new Array(this, distributor, valueStruct, items);
	}

	private static Obj owner(Scope scope) {

		final Obj owner = scope.toObject();

		assert owner != null :
			"Enclosing scope is not object: " + scope;

		return owner;
	}

	private ArrayItem[] reproduceItems(
			ArrayItem[] items,
			Reproducer reproducer) {

		final ArrayItem[] newItems = new ArrayItem[items.length];

		for (int i = 0; i < items.length; ++i) {

			final ArrayItem oldItem = items[i];
			final ArrayItem newItem = oldItem.reproduce(this, reproducer);

			if (newItem != null) {
				newItems[i] = newItem;
				continue;
			}

			newItems[i] = new ArrayItem(
					i,
					errorRef(oldItem, reproducer.distribute()));
		}

		return newItems;
	}

}
