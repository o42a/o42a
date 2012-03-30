/*
    Compiler Core
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
package org.o42a.core.object.array;

import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.value.ValueKnowledge.*;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.ir.value.array.ArrayIRGenerator;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.impl.ArrayContentReproducer;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public final class Array extends Placed {

	private final Array origin;
	private final PrefixPath prefix;
	private final Obj owner;
	private final ArrayValueStruct valueStruct;
	private final ArrayItem[] items;
	private ArrayIR ir;
	private ValueKnowledge valueKnowledge;
	private boolean hasStaticItems;

	public Array(
			LocationInfo location,
			Distributor distributor,
			ArrayValueStruct valueStruct,
			ArrayItem[] items) {
		super(location, distributor);
		this.origin = this;
		this.prefix = PrefixPath.emptyPrefix(distributor.getScope());
		this.owner = owner(distributor.getScope());
		valueStruct.getItemTypeRef().assertSameScope(distributor);
		this.valueStruct = valueStruct;
		this.items = items;
	}

	private Array(Array from, PrefixPath prefix) {
		super(from, from.distributeIn(prefix.getStart().getContainer()));
		this.origin = from.getOrigin();
		this.prefix = from.getPrefix().and(prefix);
		this.owner = owner(prefix.getStart());
		this.valueStruct = from.getValueStruct().prefixWith(prefix);
		this.items = from.prefixItems(prefix);
		this.valueKnowledge = from.getValueKnowledge();
		this.hasStaticItems = from.hasStaticItems();
	}

	public final boolean isOrigin() {
		return getOrigin() == this;
	}

	public final Array getOrigin() {
		return this.origin;
	}

	public final PrefixPath getPrefix() {
		return this.prefix;
	}

	public final Obj getOwner() {
		return this.owner;
	}

	public final ArrayValueType getValueType() {
		return getValueStruct().getValueType();
	}

	public final ArrayValueStruct getValueStruct() {
		return this.valueStruct;
	}

	public final boolean isConstant() {
		return this.valueStruct.isConstant();
	}

	public final ValueKnowledge getValueKnowledge() {
		analyseItems();
		return this.valueKnowledge;
	}

	public final boolean hasStaticItems() {
		analyseItems();
		return this.hasStaticItems;
	}

	public final ArrayItem[] getItems() {
		return this.items;
	}

	public final int length() {
		return getItems().length;
	}

	public final boolean isEmpty() {
		return length() == 0;
	}

	public final ArrayItem[] items(Scope scope) {
		return upgradeScope(scope).items;
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

	public final Array prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new Array(this, prefix);
	}

	public final Array upgradeScope(Scope toScope) {
		if (toScope == getScope()) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public final Value<Array> toValue() {
		return getValueStruct().compilerValue(this);
	}

	public final ArrayIR ir(ArrayIRGenerator generator) {
		if (this.ir != null
				&& this.ir.getGenerator() == generator.getGenerator()) {
			return this.ir;
		}
		return this.ir = new ArrayIR(generator, this);
	}

	@Override
	public String toString() {
		if (this.items == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		if (isConstant()) {
			out.append("[(`");
		} else {
			out.append("[(``");
		}
		out.append(getValueStruct().getItemTypeRef());

		if (this.items.length == 0) {
			return out.append(")]").toString();
		}

		out.append(") ");

		for (ArrayItem item : this.items) {
			out.append(item.getValueRef());
		}

		out.append(']');

		return out.toString();
	}

	private void analyseItems() {
		if (this.valueKnowledge != null) {
			return;
		}

		boolean runtime = false;
		boolean staticItems = true;

		for (ArrayItem item : getItems()) {
			if (!runtime) {

				final Obj itemObject =
						item.getValueRef().getResolution().toObject();

				if (itemObject.getConstructionMode().isRuntime()) {
					runtime = true;
					if (!staticItems) {
						break;
					}
				}
			}
			if (staticItems && !item.getValueRef().isStatic()) {
				staticItems = false;
				if (runtime) {
					break;
				}
			}
		}

		if (staticItems) {
			this.hasStaticItems = true;
		}
		if (runtime) {
			if (isConstant()) {
				this.valueKnowledge = RUNTIME_CONSTRUCTED_VALUE;
			} else {
				this.valueKnowledge = VARIABLE_VALUE;
			}
		} else {
			if (isConstant()) {
				this.valueKnowledge = KNOWN_VALUE;
			} else {
				this.valueKnowledge = INITIALLY_KNOWN_VALUE;
			}
		}
	}

	private static Obj owner(Scope scope) {

		final Obj owner = scope.toObject();

		assert owner != null :
			"Enclosing scope is not object: " + scope;

		return owner;
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
