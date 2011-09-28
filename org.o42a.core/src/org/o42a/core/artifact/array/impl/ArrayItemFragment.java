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

import static org.o42a.core.def.Rescoper.upgradeRescoper;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectValue;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class ArrayItemFragment extends PathFragment {

	private final Ref indexRef;
	private boolean error;

	public ArrayItemFragment(Ref indexRef) {
		this.indexRef = indexRef;
	}

	@Override
	public boolean isArtifact() {
		return true;
	}

	@Override
	public PathFragment materialize() {
		return MATERIALIZE;
	}

	@Override
	public Container resolve(
			PathResolver resolver,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {
		if (this.error) {
			return null;
		}

		final Obj array = start.toObject();

		assert array != null :
			"Not an array object: " + start;

		final ObjectValue arrayValue = array.value().explicitUseBy(resolver);
		final ArrayValueStruct arrayStruct =
				ArrayValueStruct.class.cast(arrayValue.getValueStruct());
		final Resolution indexResolution =
				this.indexRef.resolve(start.newResolver(resolver));

		if (indexResolution.isError()) {
			return null;
		}

		final ObjectValue indexValue =
				indexResolution.materialize().value().explicitUseBy(resolver);

		if (indexValue.getValueType() != ValueType.INTEGER) {
			this.error = true;
			resolver.getLogger().error(
					"non_integer_array_index",
					this.indexRef,
					"Array index should be integer");
			return null;
		}

		final Value<Long> arrayIndexVal =
				ValueType.INTEGER.cast(indexValue.getValue());

		if (arrayIndexVal.isDefinite()) {

			final long itemIdx = arrayIndexVal.getDefiniteValue();

			if (itemIdx < 0) {
				resolver.getLogger().error(
						"negative_array_index",
						this.indexRef,
						"Negative array index");
				return null;
			}

			final Value<ArrayItem[]> arrayVal =
					arrayStruct.cast(arrayValue.getValue());

			if (arrayVal.isDefinite()) {

				final ArrayItem[] items = arrayVal.getDefiniteValue();

				if (items.length >= itemIdx) {
					resolver.getLogger().error(
							"invalid_array_index",
							this.indexRef,
							"Array index %d is too big."
							+ " Array has only %d elements",
							itemIdx,
							items.length);
					return null;
				}

				final ArrayItem item = items[(int) itemIdx];

				walker.arrayItem(array, this, item);

				return item.getContainer();
			}
		}

		final Ref indexRef = this.indexRef.rescope(
				upgradeRescoper(this.indexRef.getScope(), start));
		final RuntimeArrayItem item = new RuntimeArrayItem(indexRef);

		walker.arrayItem(array, this, item);

		return item.getContainer();
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {

		final Ref indexRef = this.indexRef.reproduce(reproducer);

		if (indexRef == null) {
			return null;
		}

		final ArrayItemFragment fragment = new ArrayItemFragment(indexRef);

		fragment.error = this.error;

		return reproducedPath(fragment.toPath());
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "[" + this.indexRef + ']';
	}

}
