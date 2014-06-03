/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.link.impl;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.value.link.impl.LinkInterface.linkInterfaceOf;

import java.util.HashMap;

import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.AbstractObjectStoreOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.Link;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.ID;


public class DereferenceStep extends Step {

	private ObjectStepUses uses;
	private HashMap<Scope, RtLink> rtLinks;
	private Obj iface;

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
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return linkInterfaceOf(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
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

		final TypeParameters<?> typeParameters =
				linkObject.type().getParameters();
		final LinkValueType linkType =
				typeParameters.getValueType().toLinkType();

		if (this.iface == null) {
			this.iface = linkType.interfaceRef(typeParameters).getType();
		}

		assert linkType != null :
			linkObject + " is not a link object";

		final Value<?> value = linkObjectValue.getValue();
		final Link link;

		if (!value.getKnowledge().isKnownToCompiler()) {
			link = rtLink(resolver);
		} else if (value.getKnowledge().isFalse()) {
			link = rtLink(resolver);
		} else if (!value.getKnowledge().isFalse()) {
			link = linkType.cast(value.getCompilerValue());
		} else {
			return null;
		}

		if (resolver.isFullResolution()) {
			link.resolveAll(
					resolver.getStart().resolver()
					.fullResolver(resolver, resolver.refUsage()));
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
	protected HostOp op(HostOp host) {
		return new DereferenceOp(host, this);
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		return defaultTargetIR(refIR);
	}

	RtLink rtLink(LocationInfo location, Scope enclosing) {
		if (this.rtLinks == null) {
			this.rtLinks = new HashMap<>(1);
		} else {

			final RtLink existing = this.rtLinks.get(enclosing);

			if (existing != null) {
				return existing;
			}
		}

		final RtLink rtLink = new RtLink(location, this, enclosing);

		this.rtLinks.put(enclosing, rtLink);

		return rtLink;
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private RtLink rtLink(StepResolver resolver) {
		return rtLink(resolver.getPath(), resolver.getStart());
	}

	private void normalizeDeref(PathNormalizer normalizer) {

		final Prediction nextPrediction = normalizer.nextPrediction();

		if (nextPrediction == null) {
			normalizer.cancel();
			return;
		}

		final Obj linkObject = nextPrediction.getScope().toObject();
		final LinkValueType linkType =
				linkObject.type().getValueType().toLinkType();

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

	private final class DereferenceOp extends StepOp<DereferenceStep> {

		DereferenceOp(HostOp host, DereferenceStep step) {
			super(host, step);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public HostTargetOp target() {
			return pathTargetOp();
		}

		@Override
		public TargetOp pathTarget(CodeDirs dirs) {

			final ObjHolder holder = tempObjHolder(dirs.getAllocator());

			return dereference(dirs, holder);
		}

		@Override
		protected TargetStoreOp allocateStore(ID id, Code code) {
			return new DereferenceStoreOp(id, code, this);
		}

		private ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
			return host().target().dereference(dirs, holder);
		}

	}

	private final class DereferenceStoreOp extends AbstractObjectStoreOp {

		private final DereferenceOp op;

		DereferenceStoreOp(ID id, Code code, DereferenceOp op) {
			super(id, code);
			this.op = op;
		}

		@Override
		public Obj getWellKnownType() {
			return DereferenceStep.this.iface;
		}

		@Override
		protected ObjectOp object(CodeDirs dirs) {
			return this.op.dereference(
					dirs,
					tempObjHolder(getAllocator()));
		}

	}

}
