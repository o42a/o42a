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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.Link;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.*;
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
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj linkObject = resolver.getStart().toObject();

		assert linkObject != null :
			resolver + " is not an object";

		final ObjectValue linkObjectValue =
				linkObject.value().explicitUseBy(resolver);

		if (resolver.isFullResolution()) {
			uses().useBy(resolver);
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
		final Link link;

		if (!value.getKnowledge().isKnownToCompiler()) {
			link = new RtLink(resolver.getPath(), resolver.getStart());
		} else if (value.getKnowledge().isFalse()) {
			link = new RtLink(resolver.getPath(), resolver.getStart());
		} else if (!value.getKnowledge().isFalse()) {
			link = linkStruct.cast(value.getCompilerValue());
		} else {
			return null;
		}

		if (resolver.isFullResolution()) {
			link.resolveAll(
					resolver.getStart().resolver()
					.fullResolver(resolver, resolver.getUsage()));
		}

		resolver.getWalker().dereference(linkObject, this, link);

		return link.getTarget();
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
			normalizer.finish();// Can not normalize a variable.
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

		for (Pred replacement : prediction) {
			if (!replacement.isPredicted()) {
				return true;
			}
			if (replacement.getScope() != stepStart) {
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

			return typeRef.getPath().getPath();
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
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
			return target(dirs).dereference(dirs, holder);
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return start().dereference(
					dirs,
					tempObjHolder(dirs.getAllocator()));
		}

	}

}
