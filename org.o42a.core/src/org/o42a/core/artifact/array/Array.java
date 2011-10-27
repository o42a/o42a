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
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import java.util.IdentityHashMap;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.impl.ArrayContentReproducer;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


public final class Array extends Placed {

	private final Obj origin;
	private final Obj owner;
	private final ArrayValueStruct valueStruct;
	private final ArrayItem[] items;
	private IdentityHashMap<Scope, Array> clones;

	public Array(
			LocationInfo location,
			Distributor distributor,
			ArrayValueStruct valueStruct,
			ArrayItem[] items) {
		super(location, distributor);
		this.origin = this.owner = owner(distributor.getScope());
		valueStruct.getItemTypeRef().assertSameScope(distributor);
		this.valueStruct = valueStruct;
		this.items = items;
	}

	private Array(Array from, Obj owner) {
		super(from, from.distributeIn(owner));
		this.origin = from.getOrigin();
		this.owner = owner;

		final PrefixPath prefix = upgradePrefix(from, getScope());

		this.valueStruct = from.getValueStruct().prefixWith(prefix);
		this.items = from.propagateItems(owner.getScope(), prefix);
	}

	public final Obj getOrigin() {
		return this.origin;
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
		return propagateTo(scope).items;
	}

	public Array propagateTo(Scope scope) {
		if (scope == getScope()) {
			return this;
		}

		assertCompatible(scope);

		if (this.clones != null) {

			final Array clone = this.clones.get(scope);

			if (clone != null) {
				return clone;
			}
		} else {
			this.clones = new IdentityHashMap<Scope, Array>();
		}

		final Array clone = new Array(this, owner(scope));

		this.clones.put(scope, clone);

		return clone;
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

	public Array prefixWith(PrefixPath prefix) {
		if (prefix.isEmpty()) {
			if (prefix.getStart() == getScope()) {
				return this;
			}
			return propagateTo(prefix.getStart());
		}
		return new Array(
				this,
				distributeIn(prefix.getStart().getContainer()),
				getValueStruct().prefixWith(prefix),
				prefixItems(prefix));
	}

	public final Value<Array> toValue() {
		return getValueStruct().constantValue(this);
	}

	private static Obj owner(Scope scope) {

		final Obj owner = scope.toObject();

		assert owner != null :
			"Enclosing scope is not object: " + scope;

		return owner;
	}

	private ArrayItem[] propagateItems(Scope scope, PrefixPath prefix) {

		final ArrayItem[] newItems = new ArrayItem[this.items.length];

		for (int i = 0; i < newItems.length; ++i) {

			final ArrayItem oldItem = this.items[i];

			newItems[i] = oldItem.propagateTo(scope, prefix);
		}

		return newItems;
	}

	private ArrayItem[] prefixItems(PrefixPath prefix) {

		final ArrayItem[] newItems = new ArrayItem[this.items.length];

		for (int i = 0; i < newItems.length; ++i) {

			final ArrayItem oldItem = this.items[i];

			newItems[i] = oldItem.prefixWith(prefix);
		}

		return newItems;
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
