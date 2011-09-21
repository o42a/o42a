/*
    Compiler
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
package org.o42a.compiler.ip.ref.array;

import static org.o42a.core.ref.Ref.errorRef;

import org.o42a.ast.expression.ArgumentNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;


final class ArrayObject extends Obj {

	private final ArrayConstructor constructor;
	private final Reproducer reproducer;
	private final ArrayObject reproducedFrom;
	private ArrayItem[] items;

	ArrayObject(ArrayConstructor constructor) {
		super(constructor, constructor.distribute());
		this.constructor = constructor;
		this.reproducer = null;
		this.reproducedFrom = null;
	}

	ArrayObject(
			ArrayConstructor constructor,
			Reproducer reproducer,
			ArrayObject reproducedFrom) {
		super(constructor, constructor.distribute());
		this.constructor = constructor;
		this.reproducer = reproducer;
		this.reproducedFrom = reproducedFrom;
	}

	public final ArrayConstructor getConstructor() {
		return this.constructor;
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
	protected Definitions explicitDefinitions() {

		final ArrayValueStruct valueStruct = new ArrayValueStruct(
				this.constructor.getItemTypeRef(),
				this.constructor.isConstant());
		final Ref constantRef = valueStruct.constantRef(
				this,
				distribute(),
				items());

		return constantRef.toValueDef().toDefinitions();
	}

	private ArrayItem[] items() {
		if (this.items != null) {
			return this.items;
		}
		if (this.reproducedFrom == null) {
			return this.items = createItems();
		}
		return this.items = reproduceItems();
	}

	private ArrayItem[] createItems() {

		final ArgumentNode[] argNodes =
				this.constructor.getNode().getArguments();
		final ArrayItem[] items = new ArrayItem[argNodes.length];
		final Distributor distributor = distribute();

		for (int i = 0; i < argNodes.length; ++i) {

			final ArgumentNode argNode = argNodes[i];
			final ExpressionNode itemNode = argNode.getValue();
			final Location location = new Location(getContext(), itemNode);
			final Ref indexRef = ValueStruct.INTEGER.constantRef(
					location,
					distributor,
					Long.valueOf(i));

			if (itemNode != null) {

				final Ref itemRef = itemNode.accept(
						this.constructor.ip().expressionVisitor(),
						distributor);

				if (itemRef != null) {
					items[i] = new ArrayItem(indexRef, itemRef);
					continue;
				}
			}

			items[i] = new ArrayItem(indexRef, errorRef(location, distributor));
		}

		return items;
	}

	private ArrayItem[] reproduceItems() {

		final Distributor distributor = distribute();
		final ArrayItem[] oldItems = this.reproducedFrom.items();
		final ArrayItem[] newItems = new ArrayItem[oldItems.length];

		for (int i = 0; i < oldItems.length; ++i) {

			ArrayItem oldItem = oldItems[i];
			final ArrayItemReproducer itemReproducer =
					new ArrayItemReproducer(
							oldItem,
							distributor,
							this.reproducer);
			final ArrayItem newItem = oldItem.reproduce(itemReproducer);

			if (newItem != null) {
				newItems[i] = newItem;
				continue;
			}

			newItems[i] = new ArrayItem(
					ValueStruct.INTEGER.constantRef(
							oldItem,
							distributor,
							Long.valueOf(i)),
					errorRef(oldItem, distributor));
		}

		return newItems;
	}

}
