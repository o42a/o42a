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

import static org.o42a.core.ref.impl.type.DefaultValueStructFinder.DEFAULT_VALUE_STRUCT_FINDER;

import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Lambda;
import org.o42a.util.log.Loggable;


public final class DefaultTypeRef extends TypeRef {

	private final Ref ref;
	private final Lambda<ValueStruct<?, ?>, Ref> valueStructFinder;
	private ValueStruct<?, ?> valueStruct;

	public DefaultTypeRef(
			Ref ref,
			Rescoper rescoper,
			Lambda<ValueStruct<?, ?>, Ref> valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		super(rescoper);
		this.ref = ref;
		if (valueStructFinder != null) {
			this.valueStructFinder = valueStructFinder;
		} else {
			this.valueStructFinder = DEFAULT_VALUE_STRUCT_FINDER;
		}
		this.valueStruct = valueStruct;
	}

	@Override
	public final CompilerContext getContext() {
		return this.ref.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.ref.getLoggable();
	}

	@Override
	public boolean isStatic() {
		return getRef().isStatic();
	}

	@Override
	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public final Ref getUntachedRef() {
		return this.ref;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final ValueStruct<?, ?> valueStruct;
		final ValueStruct<?, ?> foundValueStruct =
				this.valueStructFinder.get(getRef());

		if (foundValueStruct != null) {
			valueStruct = foundValueStruct;
		} else {
			valueStruct = DEFAULT_VALUE_STRUCT_FINDER.get(getRef());
		}

		return this.valueStruct =
				valueStruct.rescope(getRef().toRescoper().and(getRescoper()));
	}

	@Override
	public StaticTypeRef toStatic() {
		return new DefaultStaticTypeRef(
				getRef(),
				getUntachedRef(),
				getRescoper(),
				this.valueStructFinder,
				this.valueStruct);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	@Override
	protected DefaultTypeRef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {

		final ValueStruct<?, ?> valueStruct;

		if (this.valueStruct == null) {
			valueStruct = null;
		} else {
			valueStruct = this.valueStruct.rescope(additionalRescoper);
		}

		return new DefaultTypeRef(
				getRef(),
				rescoper,
				this.valueStructFinder,
				valueStruct);
	}

	@Override
	protected DefaultTypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper) {
		assert ref == untouchedRef :
			ref + " should be the same as " + untouchedRef;

		final ValueStruct<?, ?> valueStruct;

		if (this.valueStruct == null) {
			valueStruct = null;
		} else {
			valueStruct = this.valueStruct.reproduce(reproducer);
			if (valueStruct == null) {
				return null;
			}
		}

		return new DefaultTypeRef(
				ref,
				rescoper,
				this.valueStructFinder,
				valueStruct);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		validate();
		this.ref.resolveAll(resolver);
	}

}
