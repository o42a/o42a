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

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.impl.ArrayItemIR;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public final class ArrayItem extends ArrayElement {

	private final int index;
	private final Ref valueRef;

	public ArrayItem(int index, Ref valueRef) {
		super(
				valueRef,
				valueRef.distribute(),
				ValueType.INTEGER.constantRef(
						valueRef,
						valueRef.distribute(),
						Long.valueOf(index)));
		this.index = index;
		this.valueRef = valueRef;
	}

	private ArrayItem(
			Scope enclosing,
			ArrayItem propagatedFrom,
			PrefixPath prefix) {
		super(enclosing, propagatedFrom, prefix);
		this.index = propagatedFrom.getIndex();
		this.valueRef = propagatedFrom.getValueRef().prefixWith(prefix);
	}

	public final int getIndex() {
		return this.index;
	}

	public final Ref getValueRef() {
		return this.valueRef;
	}

	@Override
	public Link getArtifact() {
		return new ItemLink(this);
	}

	@Override
	public final ArrayItem getPropagatedFrom() {
		return (ArrayItem) super.getPropagatedFrom();
	}

	@Override
	public final ArrayItem getFirstDeclaration() {
		return (ArrayItem) super.getFirstDeclaration();
	}

	@Override
	public final ArrayItem getLastDefinition() {
		return (ArrayItem) super.getLastDefinition();
	}

	public ArrayItem prefixWith(PrefixPath prefix) {
		if (prefix.isEmpty()) {
			if (prefix.getStart() == getScope()) {
				return this;
			}
			return propagateTo(prefix.getStart(), prefix);
		}
		return new ArrayItem(getIndex(), getValueRef().prefixWith(prefix));
	}

	public void resolveAll(Resolver resolver) {
		getValueRef().resolveValues(resolver);
	}

	protected ArrayItem reproduce(Array array, Reproducer reproducer) {
		getEnclosingScope().assertCompatible(reproducer.getReproducingScope());

		final Ref valueRef = getValueRef().reproduce(reproducer);

		if (valueRef == null) {
			return null;
		}

		return new ArrayItem(getIndex(), valueRef);
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		return new ArrayItemIR(generator, this);
	}

	ArrayItem propagateTo(Scope scope, PrefixPath prefix) {
		return new ArrayItem(scope, this, prefix);
	}

	private static final class ItemLink extends Link {

		private final ArrayItem item;

		ItemLink(ArrayItem item) {
			super(
					item,
					item.isConstant()
					? ArtifactKind.LINK : ArtifactKind.VARIABLE);
			this.item = item;
		}

		@Override
		protected TargetRef buildTargetRef() {
			return this.item.getValueRef().toTargetRef(this.item.getTypeRef());
		}

	}

}
