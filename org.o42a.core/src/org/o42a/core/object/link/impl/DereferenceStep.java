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
package org.o42a.core.object.link.impl;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.*;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;


public class DereferenceStep extends Step {

	private static final LinkAncestor LINK_ANCESTOR = new LinkAncestor();

	private LinkValueStruct linkStruct;
	private ObjectStepUses uses;

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.DEREF_USAGE;
	}

	@Override
	public String toString() {
		return "->";
	}

	@Override
	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return path.cut(1).append(LINK_ANCESTOR).typeRef(distributor);
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			final PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj linkObject = start.toObject();

		assert linkObject != null :
			start + " is not an object";

		final ObjectValue linkObjectValue =
				linkObject.value().explicitUseBy(resolver);

		if (resolver.isFullResolution()) {
			uses().useBy(resolver, path, index);
			linkObjectValue.resolveAll(resolver);
		}

		final LinkValueStruct linkStruct =
				linkObjectValue.getValueStruct().toLinkStruct();

		assert linkStruct != null :
			linkObject + " is not a link object";

		if (this.linkStruct == null) {
			this.linkStruct = linkStruct;
		}

		final Value<?> value = linkObjectValue.getValue();
		final ObjectLink link;

		if (!value.getKnowledge().isKnownToCompiler()) {
			link = new RtLink(path, start);
		} else if (value.getKnowledge().isFalse()) {
			link = new RtLink(path, start);
		} else {

			final Value<KnownLink> linkValue = linkStruct.cast(value);

			if (linkValue.getKnowledge().isFalse()) {
				return null;
			}
			link = linkValue.getCompilerValue();
		}

		if (resolver.isFullResolution()) {
			link.resolveAll(start.newResolver(resolver));
		}

		walker.dereference(linkObject, this, link);

		return link.getTarget();
	}

	@Override
	protected Scope revert(Scope target) {
		return target.toObject().getDereferencedLink().getScope();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizeDeref(normalizer);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizeDeref(normalizer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(new DereferenceStep().toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		return new Op(start, this);
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private void normalizeDeref(final PathNormalizer normalizer) {

		final Prediction nextPrediction = normalizer.nextPrediction();

		if (nextPrediction == null) {
			normalizer.cancel();
			return;
		}

		final Obj linkObject = nextPrediction.getScope().toObject();
		final LinkValueType linkType =
				linkObject.value().getValueType().toLinkType();

		if (linkType.isVariable()) {
			normalizer.finish();// Can not normalize variables.
			return;
		}

		final DefTarget defTarget =
				linkObject.value().getDefinitions().target();

		if (!defTarget.exists() || defTarget.isUnknown()) {
			normalizer.finish();
			return;
		}
		if (linkUpdated(normalizer)) {
			normalizer.finish();
			return;
		}

		final Ref target = defTarget.getRef();

		normalizer.append(
				target.getPath(),
				uses().nestedNormalizer(normalizer));
	}

	private boolean linkUpdated(PathNormalizer normalizer) {

		final Prediction prediction = normalizer.nextPrediction();
		final Scope stepStart = prediction.getScope();

		for (Scope replacement : prediction) {
			if (replacement != stepStart) {
				return true;
			}
		}

		return false;
	}

	private static final class LinkAncestor extends PathFragment {

		@Override
		public Path expand(PathExpander expander, int index, Scope start) {

			final LinkValueStruct linkStruct =
					start.toObject().value().getValueStruct().toLinkStruct();
			final TypeRef typeRef = linkStruct.getTypeRef();

			return start.getEnclosingScopePath()
					.append(typeRef.getPath().getPath());
		}

		@Override
		public String toString() {
			return "^^";
		}

	}

	private final class Op extends StepOp<DereferenceStep> {

		Op(PathOp start, DereferenceStep step) {
			super(start, step);
		}

		@Override
		public ObjectOp dereference(CodeDirs dirs) {
			throw new UnsupportedOperationException();
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return start().dereference(dirs);
		}

	}

}
