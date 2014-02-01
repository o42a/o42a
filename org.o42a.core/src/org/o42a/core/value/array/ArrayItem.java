/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.LinkData;
import org.o42a.core.value.link.TargetRef;


public final class ArrayItem extends ArrayElement {

	private final ArrayItemData data;
	private final int index;
	private final Ref valueRef;

	public ArrayItem(int index, Ref valueRef) {
		super(valueRef, valueRef.distribute());
		this.data = new ArrayItemData(this);
		this.index = index;
		this.valueRef = valueRef;
	}

	public final int getIndex() {
		return this.index;
	}

	public final Ref getValueRef() {
		return this.valueRef;
	}

	public ArrayItem prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new ArrayItem(getIndex(), getValueRef().prefixWith(prefix));
	}

	@Override
	public void resolveAll(FullResolver resolver) {
		getValueRef().resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
		this.data.resolveAll(resolver);
	}

	@Override
	public String toString() {
		if (this.valueRef == null) {
			return super.toString();
		}
		return getScope() + "[" + getIndex() + "]: " + getValueRef();
	}

	@Override
	protected Obj createTarget() {
		return this.data.createTarget();
	}

	@Override
	protected ArrayElement findLinkIn(Scope enclosing) {

		final Obj array = enclosing.toObject();
		final ObjectValue arrayValue = array.value();
		final TypeParameters<?> parameters =
				array.type().getParameters();
		final TypeParameters<Array> arrayParameters =
				parameters.getValueType().toArrayType().cast(parameters);
		final Value<Array> arrayVal =
				arrayParameters.cast(arrayValue.getValue());
		final ArrayItem[] items =
				arrayVal.getCompilerValue().items(enclosing);

		return items[getIndex()];
	}

	protected ArrayItem reproduce(Reproducer reproducer) {
		getScope().assertCompatible(reproducer.getReproducingScope());

		final Ref valueRef = getValueRef().reproduce(reproducer);

		if (valueRef == null) {
			return null;
		}

		return new ArrayItem(getIndex(), valueRef);
	}

	private static final class ArrayItemData extends LinkData<ArrayItem> {

		ArrayItemData(ArrayItem link, TargetRef targetRef) {
			super(link, targetRef);
		}

		ArrayItemData(ArrayItem link) {
			super(link);
		}

		@Override
		protected TargetRef buildTargetRef() {
			return getLink().getValueRef()
					.toTargetRef(getLink().getInterfaceRef());
		}

	}

}
