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
package org.o42a.core.artifact.array.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectValue;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class ArrayElementStep extends Step {

	private final PathBinding<Ref> index;
	private ArrayValueStruct arrayStruct;
	private boolean error;

	ArrayElementStep(PathBinding<Ref> index) {
		this.index = index;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public boolean isMaterial() {
		return false;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.VALUE_REF_USAGE;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {
		if (this.error) {
			return null;
		}

		final Ref indexRef = indexRef(path);
		final Obj array = start.toObject();

		assert array != null :
			"Not an array object: " + start;

		final ObjectValue arrayValue = array.value().explicitUseBy(resolver);

		if (resolver.isFullResolution()) {
			array.value().resolveAll(resolver);
		}

		final ArrayValueStruct arrayStruct =
				ArrayValueStruct.class.cast(arrayValue.getValueStruct());

		if (this.arrayStruct == null) {
			this.arrayStruct = arrayStruct;
		}

		final Resolver indexResolver =
				resolver.getPathStart().newResolver(resolver);
		final Resolution indexResolution = indexRef.resolve(indexResolver);

		if (resolver.isFullResolution()) {
			indexRef.resolve(indexResolver).resolveValue();
		}

		if (indexResolution.isError()) {
			return null;
		}

		final ObjectValue indexValue =
				indexResolution.materialize().value().explicitUseBy(resolver);

		if (indexValue.getValueType() != ValueType.INTEGER) {
			this.error = true;
			path.getLogger().error(
					"non_integer_array_index",
					indexRef,
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
				path.getLogger().error(
						"negative_array_index",
						indexRef,
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
						arrayVal.getCompilerValue().items(start);

				if (itemIdx >= items.length) {
					this.error = true;
					path.getLogger().error(
							"invalid_array_index",
							indexRef,
							"Array index %d is too big."
							+ " Array has only %d elements",
							itemIdx,
							items.length);
					return null;
				}

				final ArrayItem item = items[(int) itemIdx];

				walker.arrayElement(array, this, item);

				return item.getContainer();
			}
		}

		final RtArrayElement element = rtElement(resolver, path, start);

		walker.arrayElement(array, this, element);

		return element.getContainer();
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final PathBinding<Ref> index = reproducer.reproduce(this.index);

		if (index == null) {
			return null;
		}

		final ArrayElementStep step = new ArrayElementStep(index);

		step.error = this.error;

		return reproducedPath(step.toPath());
	}

	@Override
	public PathOp op(PathOp start) {
		return new ArrayElementOp(
				start,
				this.arrayStruct,
				indexRef(start.getPath()));
	}

	@Override
	public String toString() {
		if (this.index == null) {
			return super.toString();
		}
		return "[" + this.index + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return objectFieldDefinition(path, distributor);
	}

	private final Ref indexRef(BoundPath path) {
		return path.getBindings().boundOf(this.index);
	}

	private RtArrayElement rtElement(
			PathResolver resolver,
			BoundPath path,
			Scope start) {

		final Ref indexRef =
				indexRef(path).upgradeScope(resolver.getPathStart());

		return new RtArrayElement(start, indexRef);
	}

}
