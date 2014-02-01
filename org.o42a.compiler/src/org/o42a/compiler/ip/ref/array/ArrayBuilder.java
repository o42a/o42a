/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.ParentAccessRules;
import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;


abstract class ArrayBuilder {

	private final ArrayConstructor constructor;

	ArrayBuilder(ArrayConstructor constructor) {
		this.constructor = constructor;
	}

	public final ArrayConstructor getConstructor() {
		return this.constructor;
	}

	public Array createArray(AccessDistributor enclosing, Scope scope) {

		final ParentAccessRules accessRules = new ParentAccessRules(
				enclosing.getContainer(),
				enclosing.getAccessRules());
		final AccessDistributor distributor = accessRules.distribute(
				enclosing.distributeIn(scope.getContainer()));
		final boolean typeByItems = typeByItems();
		final TypeParameters<Array> typeParams;
		TypeRef arrayItemType;

		if (!typeByItems) {
			typeParams = knownTypeParameters().upgradeScope(scope);
			arrayItemType =
					typeParams.getValueType()
					.toArrayType()
					.itemTypeRef(typeParams);
		} else {
			typeParams = null;
			arrayItemType = null;
		}

		final ArgumentNode[] argNodes =
				this.constructor.getNode().getArguments();
		final ArrayItem[] items = new ArrayItem[argNodes.length];

		for (int i = 0; i < argNodes.length; ++i) {

			final ArgumentNode argNode = argNodes[i];
			final ExpressionNode itemNode = argNode.getValue();

			if (argNode.isInitializer()) {
				getConstructor().getLogger().syntaxError(argNode.getInit());
			}
			if (itemNode == null) {
				getConstructor().getLogger().noValue(argNode);
			} else {

				final Ref itemRef = itemNode.accept(
						this.constructor.ip().expressionVisitor(),
						distributor);

				if (itemRef != null) {

					final TypeRef itemType = itemRef.getInterface();

					if (arrayItemType == null) {
						arrayItemType = itemType;
					} else if (!typeByItems) {
						itemType.relationTo(arrayItemType)
						.checkDerived(this.constructor.getLogger());
					} else {
						arrayItemType =
								arrayItemType.relationTo(itemType)
								.check(this.constructor.getLogger())
								.commonAscendant();
					}

					items[i] = new ArrayItem(i, itemRef);

					continue;
				}
			}

			items[i] = new ArrayItem(i, errorRef(
					new Location(this.constructor.getContext(), itemNode),
					distributor));
		}

		final TypeParameters<Array> finalTypeParams;

		if (!typeByItems) {
			finalTypeParams = typeParams;
		} else if (arrayItemType != null) {
			finalTypeParams = arrayType().typeParameters(arrayItemType);
		} else {
			finalTypeParams = arrayType().typeParameters(
					ValueType.VOID.typeRef(this.constructor, scope));
		}

		return new Array(
				this.constructor,
				distributor,
				finalTypeParams,
				items);
	}

	protected abstract ArrayValueType arrayType();

	protected abstract boolean typeByItems();

	protected abstract TypeParameters<Array> knownTypeParameters();

}
