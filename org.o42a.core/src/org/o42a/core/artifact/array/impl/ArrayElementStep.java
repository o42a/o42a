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
package org.o42a.core.artifact.array.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import java.util.IdentityHashMap;

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
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class ArrayElementStep extends Step {

	private final Ref initialIndexRef;
	private Ref indexRef;
	private ArrayValueStruct arrayStruct;
	private boolean error;
	private RtArrayElement rtElement;
	private IdentityHashMap<Scope, RtArrayElement> rtElements;

	public ArrayElementStep(Ref indexRef) {
		this.initialIndexRef = indexRef;
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
	public Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {
		if (this.error) {
			return null;
		}
		if (this.indexRef == null) {
			this.indexRef = this.initialIndexRef.rescope(start);
		}

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

		final Resolution indexResolution =
				this.indexRef.resolve(start.newResolver(resolver));

		if (indexResolution.isError()) {
			return null;
		}

		final ObjectValue indexValue =
				indexResolution.materialize().value().explicitUseBy(resolver);

		if (indexValue.getValueType() != ValueType.INTEGER) {
			this.error = true;
			path.getLogger().error(
					"non_integer_array_index",
					this.indexRef,
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
				path.getLogger().error(
						"negative_array_index",
						this.indexRef,
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
					path.getLogger().error(
							"invalid_array_index",
							this.indexRef,
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

		final RtArrayElement element = rtElement(start);

		walker.arrayElement(array, this, element);

		return element.getContainer();
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final Ref indexRef =
				this.indexRef.reproduce(reproducer.getReproducer());

		if (indexRef == null) {
			return null;
		}

		final ArrayElementStep step = new ArrayElementStep(indexRef);

		step.error = this.error;

		return reproducedPath(step.toPath());
	}

	@Override
	public PathOp op(PathOp start) {
		return new ArrayElementOp(start, this.arrayStruct, this.indexRef);
	}

	@Override
	public String toString() {
		if (this.initialIndexRef == null) {
			return super.toString();
		}
		return "[" + this.initialIndexRef + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return objectFieldDefinition(path, distributor);
	}

	private RtArrayElement rtElement(Scope start) {
		if (this.rtElement == null) {

			final Ref indexRef = this.indexRef.upgradeScope(start);

			return this.rtElement = new RtArrayElement(indexRef);
		}
		if (start == this.rtElement.getEnclosingScope()) {
			return this.rtElement;
		}
		if (this.rtElements == null) {
			this.rtElements = new IdentityHashMap<Scope, RtArrayElement>();
		} else {

			final RtArrayElement cachedElement = this.rtElements.get(start);

			if (cachedElement != null) {
				return cachedElement;
			}
		}

		final RtArrayElement element = this.rtElement.propagateTo(start);

		this.rtElements.put(start, element);

		return element;
	}

}
