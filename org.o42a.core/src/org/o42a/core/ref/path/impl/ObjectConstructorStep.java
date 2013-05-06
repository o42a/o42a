/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.impl.ObjectStepUses.definitionsChange;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Container;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.impl.normalizer.InlineValueStep;
import org.o42a.core.ref.impl.normalizer.SameNormalStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;


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
	public final ObjectConstructor getConstructor() {
		return this.constructor;
	}

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		return getConstructor().valueAdapter(ref, request);
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return "(" + this.constructor.toString() + ')';
	}

	@Override
	protected void rebuild(PathRebuilder rebuilder) {
		if (!rebuilder.getPath().isStatic()) {
			rebuilder.combinePreviousWithConstructor(this, getConstructor());
		}
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {

		final PrefixPath prefix = ref.getPath().cut(1).toPrefix(ref.getScope());

		return getConstructor().ancestor(location, ref).prefixWith(prefix);
	}

	@Override
	protected TypeRef iface(Ref ref) {

		final PrefixPath prefix = ref.getPath().cut(1).toPrefix(ref.getScope());

		return getConstructor().iface(ref).prefixWith(prefix);
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return getConstructor().fieldDefinition(ref);
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj object = getConstructor().resolve(resolver.getStart());

		if (object == null) {
			return null;
		}
		if (resolver.isFullResolution()) {
			object.resolveAll();
			uses().useBy(resolver);
		}
		resolver.getWalker().object(this, object);

		return object;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizeConstructor(normalizer);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizeConstructor(normalizer);
	}

	@Override
	protected void normalizeStep(Analyzer analyzer) {
		getConstructor().getConstructed().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		getConstructor().assertCompatible(reproducer.getReproducingScope());

		final ObjectConstructor reproduced =
				getConstructor().reproduce(reproducer);

		if (reproduced == null) {
			return null;
		}

		return reproducedPath(reproduced.toPath());
	}

	@Override
	protected HostOp op(HostOp host) {
		return getConstructor().op(host);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		return defaultTargetIR(refIR);
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private void normalizeConstructor(PathNormalizer normalizer) {

		final Obj object = getConstructor().resolve(
				normalizer.lastPrediction().getScope());

		if (object.getConstructionMode().isRuntime()) {
			normalizer.finish();
			return;
		}
		if (uses().onlyDereferenced(normalizer)) {
			normalizer.skipToNext(
					object.getScope().predict(normalizer.lastPrediction()));
			return;
		}
		if (!uses().onlyValueUsed(normalizer)) {
			if (!normalizer.isLastStep()) {
				// Not a last step - go on.
				normalizer.skip(
						object.getScope().predict(normalizer.lastPrediction()),
						new SameNormalStep(this));
				return;
			}
			// Can not in-line object used otherwise but by value.
			normalizer.finish();
			return;
		}

		final Prediction prediction =
				object.getScope().predict(normalizer.lastPrediction());

		if (!prediction.isPredicted()) {
			normalizer.finish();
			return;
		}
		if (definitionsChange(object, prediction)) {
			normalizer.finish();
			return;
		}

		final InlineValue inline = object.value().getDefinitions().inline(
				normalizer.getNormalizer());

		if (inline == null) {
			normalizer.finish();
			return;
		}

		normalizer.inline(prediction, new InlineValueStep(inline));
	}

}
