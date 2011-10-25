/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ref.impl.type;

import static org.o42a.core.value.ValueStructFinder.DEFAULT_VALUE_STRUCT_FINDER;

import org.o42a.core.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueStructFinder;
import org.o42a.util.log.Loggable;


public final class DefaultStaticTypeRef extends StaticTypeRef {

	private final Ref fixedRef;
	private final Ref untouchedRef;
	private final ValueStructFinder valueStructFinder;
	private ValueStruct<?, ?> valueStruct;

	public DefaultStaticTypeRef(
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper,
			ValueStructFinder valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		super(rescoper);
		this.fixedRef = ref.toStatic();
		this.untouchedRef = untouchedRef;
		ref.assertSameScope(untouchedRef);
		this.valueStructFinder = valueStructFinder;
		this.valueStruct = valueStruct;
	}

	@Override
	public final CompilerContext getContext() {
		return this.untouchedRef.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.untouchedRef.getLoggable();
	}

	@Override
	public final Ref getRef() {
		return this.fixedRef;
	}

	@Override
	public final Ref getUntachedRef() {
		return this.untouchedRef;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final ValueStruct<?, ?> defaultValueStruct =
				getRef().valueStruct(getRef().getScope());
		final ValueStruct<?, ?> valueStruct =
				this.valueStructFinder.valueStructBy(
						getRef(),
						defaultValueStruct);

		assert defaultValueStruct.assertAssignableFrom(valueStruct);

		return this.valueStruct = valueStruct.rescope(getRescoper());
	}

	@Override
	public StaticTypeRef setValueStruct(ValueStructFinder valueStructFinder) {

		final ValueStructFinder vsFinder;
		final ValueStruct<?, ?> valueStruct;

		if (valueStructFinder != null) {
			vsFinder = valueStructFinder;
			valueStruct = valueStructFinder.toValueStruct();
		} else {
			vsFinder = DEFAULT_VALUE_STRUCT_FINDER;
			valueStruct = null;
		}

		return new DefaultStaticTypeRef(
				getRef(),
				getUntachedRef(),
				getRescoper(),
				vsFinder,
				valueStruct);
	}

	@Override
	public String toString() {
		if (this.fixedRef == null) {
			return super.toString();
		}
		return this.fixedRef.toString();
	}

	@Override
	protected DefaultStaticTypeRef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {

		final ValueStruct<?, ?> valueStruct;

		if (this.valueStruct == null) {
			valueStruct = null;
		} else {
			valueStruct = this.valueStruct.rescope(additionalRescoper);
		}

		return new DefaultStaticTypeRef(
				getRef(),
				getUntachedRef(),
				rescoper,
				this.valueStructFinder,
				valueStruct);
	}

	@Override
	protected StaticTypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper) {

		final ValueStruct<?, ?> valueStruct;

		if (this.valueStruct == null) {
			valueStruct = null;
		} else {
			valueStruct = this.valueStruct.reproduce(reproducer);
			if (valueStruct == null) {
				return null;
			}
		}

		return new DefaultStaticTypeRef(
				ref,
				untouchedRef,
				rescoper,
				this.valueStructFinder,
				valueStruct);
	}

}
