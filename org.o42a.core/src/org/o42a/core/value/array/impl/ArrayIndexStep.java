/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.value.array.impl;

import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueStruct;


public class ArrayIndexStep extends Step {

	private final Ref array;
	private final Ref index;
	private boolean error;

	public ArrayIndexStep(Ref array, Ref index) {
		this.array = array;
		this.index = index;
		index.assertSameScope(array);
	}

	public final Ref getArray() {
		return this.array;
	}

	public final Ref getIndex() {
		return this.index;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public String toString() {
		if (this.index == null) {
			return super.toString();
		}
		return this.array.toString() + '[' + this.index + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
	}

	@Override
	protected Container resolve(StepResolver resolver) {
		if (this.error) {
			return null;
		}

		final Resolver arrayResolver = resolver.getStart().resolver();
		final Resolution arrayResolution = this.array.resolve(arrayResolver);

		if (arrayResolution.isError()) {
			this.error = true;
			return null;
		}

		final Obj array = arrayResolution.toObject();
		final ObjectValue arrayValue = array.value().explicitUseBy(resolver);

		if (resolver.isFullResolution()) {
			arrayValue.resolveAll(resolver);
		}

		final ArrayValueStruct arrayStruct =
				ArrayValueStruct.class.cast(arrayValue.getValueStruct());
		final Resolution indexResolution = this.index.resolve(arrayResolver);

		if (resolver.isFullResolution()) {

			final FullResolver fullResolver =
					arrayResolver.fullResolver(resolver, VALUE_REF_USAGE);

			this.array.resolveAll(fullResolver);
			this.index.resolveAll(fullResolver);
		}

		if (indexResolution.isError()) {
			return null;
		}

		final ObjectValue indexValue =
				indexResolution.toObject().value().explicitUseBy(resolver);

		if (!indexValue.getValueType().is(ValueType.INTEGER)) {
			this.error = true;
			resolver.getLogger().error(
					"non_integer_array_index",
					this.index,
					"Array index should be integer");
			return null;
		}

		final Value<Long> arrayIndexVal =
				ValueType.INTEGER.cast(indexValue.getValue());

		if (arrayIndexVal.getKnowledge().isKnown()) {
			if (arrayIndexVal.getKnowledge().isFalse()) {
				return null;
			}

			final long itemIdx = arrayIndexVal.getCompilerValue();

			if (itemIdx < 0) {
				this.error = true;
				resolver.getLogger().error(
						"negative_array_index",
						this.index,
						"Negative array index");
				return null;
			}

			final Value<Array> arrayVal =
					arrayStruct.cast(arrayValue.getValue());

			if (arrayVal.getKnowledge().isKnownToCompiler()) {
				if (arrayVal.getKnowledge().isFalse()) {
					return null;
				}

				final ArrayItem[] items =
						arrayVal.getCompilerValue().items(array.getScope());

				if (itemIdx >= items.length) {
					this.error = true;
					resolver.getLogger().error(
							"invalid_array_index",
							this.index,
							"Array index %d is too big."
							+ " Array has only %d elements",
							itemIdx,
							items.length);
					return null;
				}

				final ArrayItem item = items[(int) itemIdx];

				resolver.getWalker().arrayIndex(
						resolver.getStart(),
						this,
						this.array,
						this.index,
						item);

				return item.getTarget();
			}
		}

		final RtArrayElement element = new RtArrayElement(
				array.getScope(),
				this.index.upgradeScope(resolver.getStart()));

		resolver.getWalker().arrayIndex(
				resolver.getStart(),
				this,
				this.array,
				this.index,
				element);

		return element.getTarget();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		// Array element normalization not supported yet.
		normalizer.cancel();
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		// Array element normalization not supported yet.
		normalizer.cancel();
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final Ref array = this.array.reproduce(reproducer.getReproducer());

		if (array == null) {
			return null;
		}

		final Ref index = this.index.reproduce(reproducer.getReproducer());

		if (index == null) {
			return null;
		}

		final ArrayIndexStep step = new ArrayIndexStep(array, index);

		step.error = this.error;

		return reproducedPath(step.toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new ArrayIndexOp(start, this);
	}

}
