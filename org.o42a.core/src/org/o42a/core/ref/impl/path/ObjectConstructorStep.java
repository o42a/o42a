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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.impl.path.ObjectStepUses.definitionsChange;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.*;
import org.o42a.core.ref.impl.normalizer.InlineStep;
import org.o42a.core.ref.impl.normalizer.SameNormalStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;


public class ObjectConstructorStep extends Step {

	private final ObjectConstructor constructor;
	private ObjectStepUses uses;

	public ObjectConstructorStep(ObjectConstructor constructor) {
		this.constructor = constructor;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public ValueAdapter valueAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		return this.constructor.valueAdapter(ref, expectedStruct);
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return "(" + this.constructor.toString() + ')';
	}

	@Override
	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return this.constructor.ancestor(location)
				.prefixWith(path.cut(1).toPrefix(distributor.getScope()));
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return this.constructor.fieldDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = this.constructor.resolve(start);

		if (object == null) {
			return null;
		}
		if (resolver.isFullResolution()) {
			object.resolveAll();
			uses().useBy(resolver, path, index);
		}
		walker.object(this, object);

		return object;
	}

	@Override
	protected Scope revert(Scope target) {
		target.assertDerivedFrom(this.constructor.getConstructed().getScope());
		return target.getEnclosingScope();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {

		final Obj object =
				this.constructor.resolve(normalizer.getStepStart().getScope());

		if (object == null) {
			normalizer.cancel();
			return;
		}
		if (!uses().onlyValueUsed(normalizer.getAnalyzer())) {
			if (!normalizer.isLastStep()) {
				// Not a last step - go on.
				normalizer.add(
						object.getScope().predict(normalizer.getStepStart()),
						new SameNormalStep(this));
				return;
			}
			// Can not in-line object used otherwise but by value.
			normalizer.cancel();
			return;
		}

		final Prediction prediction =
				object.getScope().predict(normalizer.getStepStart());

		if (definitionsChange(object, prediction)) {
			normalizer.cancel();
			return;
		}

		final InlineValue inline = object.value().getDefinitions().inline(
				normalizer.getNormalizer());

		if (inline == null) {
			normalizer.cancel();
			return;
		}

		normalizer.inline(prediction, new InlineStep(this, inline) {
			@Override
			public void ignore() {
			}
			@Override
			public void cancel() {
			}
		});
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		this.constructor.assertCompatible(reproducer.getReproducingScope());

		final ObjectConstructor reproduced =
				this.constructor.reproduce(reproducer);

		if (reproduced == null) {
			return null;
		}

		return reproducedPath(reproduced.toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return this.constructor.op(start);
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

}
