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
import org.o42a.ast.field.InterfaceNode;
import org.o42a.ast.field.TypeNode;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


final class ArrayObject extends Obj {

	private final ArrayConstructor constructor;
	private final Reproducer reproducer;
	private final ArrayObject reproducedFrom;
	private Array array;

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
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				this.constructor.ancestor(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getArray().toRef().toValueDef().toDefinitions();
	}

	private Array createArray() {

		final ArrayValueStruct valueStruct = new ArrayValueStruct(
				createItemTypeRef(),
				this.constructor.isConstant());
		final ArgumentNode[] argNodes =
				this.constructor.getNode().getArguments();
		final ArrayItem[] items = new ArrayItem[argNodes.length];
		final Distributor denclosing = distributeIn(getEnclosingContainer());

		for (int i = 0; i < argNodes.length; ++i) {

			final ArgumentNode argNode = argNodes[i];
			final ExpressionNode itemNode = argNode.getValue();
			final Location location = new Location(getContext(), itemNode);

			if (itemNode != null) {

				final Ref itemRef = itemNode.accept(
						this.constructor.ip().expressionVisitor(),
						denclosing);

				if (itemRef != null) {
					items[i] =
							new ArrayItem(i, itemRef.rescope(getScope()));
					continue;
				}
			}

			items[i] = new ArrayItem(i, errorRef(location, distribute()));
		}

		return new Array(this, distribute(), valueStruct, items);
	}

	private TypeRef createItemTypeRef() {

		final InterfaceNode interfaceNode = this.constructor.getInterfaceNode();

		if (interfaceNode == null) {
			return ValueType.VOID.typeRef(
					new Location(
							getContext(),
							this.constructor.getNode().getOpening()),
					getScope());
		}

		final TypeNode typeNode = interfaceNode.getType();

		if (typeNode == null) {
			return ValueType.VOID.typeRef(
					new Location(getContext(), interfaceNode),
					getScope());
		}

		final TypeRef itemTypeRef = typeNode.accept(
				this.constructor.ip().typeVisitor(),
				distributeIn(getEnclosingContainer()));

		if (itemTypeRef != null) {
			return itemTypeRef.rescope(getScope());
		}

		return errorRef(
				new Location(getContext(), typeNode),
				distribute()).toTypeRef();
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
