/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.value.array;

import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.value.ValueKnowledge.*;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.ir.value.array.ArrayIRGenerator;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public final class Array extends Placed {

	private final Array origin;
	private final PrefixPath prefix;
	private final Obj owner;
	private final TypeParameters<Array> typeParameters;
	private final ArrayItem[] items;
	private ArrayIR ir;
	private ValueKnowledge valueKnowledge;
	private boolean hasStaticItems;

	public Array(
			LocationInfo location,
			Distributor distributor,
			TypeParameters<Array> typeParameters,
			ArrayItem[] items) {
		super(location, distributor);
		this.origin = this;
		this.prefix = PrefixPath.emptyPrefix(distributor.getScope());
		this.owner = owner(distributor.getScope());
		typeParameters.assertSameScope(distributor);
		this.typeParameters = typeParameters;
		this.items = items;
	}

	private Array(Array from, PrefixPath prefix) {
		super(from, from.distributeIn(prefix.getStart().getContainer()));
		this.origin = from.getOrigin();
		this.prefix = from.getPrefix().and(prefix);
		this.owner = owner(prefix.getStart());
		this.typeParameters = from.getTypeParameters().prefixWith(prefix);
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
		return getTypeParameters().getValueType().toArrayType();
	}

	public final TypeParameters<Array> getTypeParameters() {
		return this.typeParameters;
	}

	public final boolean isVariable() {
		return getValueType().isVariable();
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

		final TypeParameters<Array> typeParameters =
				getTypeParameters().reproduce(contentReproducer);

		if (typeParameters == null) {
			return null;
		}

		final ArrayItem[] items = reproduceItems(
				items(reproducer.getReproducingScope()),
				contentReproducer);

		return new Array(this, distributor, typeParameters, items);
	}

	public final Array prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new Array(this, prefix);
	}

	public final Array upgradeScope(Scope toScope) {
		if (toScope.is(getScope())) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

	public final Value<Array> toValue() {
		return getTypeParameters().compilerValue(this);
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

		out.append(getTypeParameters()).append('[');

		if (this.items.length == 0) {
			return out.append(']').toString();
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
			if (!isVariable()) {
				this.valueKnowledge = RUNTIME_CONSTRUCTED_VALUE;
			} else {
				this.valueKnowledge = VARIABLE_VALUE;
			}
		} else {
			if (!isVariable()) {
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
			final ArrayItem newItem = oldItem.reproduce(reproducer);

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
