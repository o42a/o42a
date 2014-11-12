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
import static org.o42a.util.fn.ArgCache.argCache;
import static org.o42a.util.fn.CondInit.condInit;

import java.util.IdentityHashMap;
import java.util.function.Function;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.local.LocalIROp;
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
import org.o42a.core.st.sentence.LocalRegistry;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.Link;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.fn.ArgCache;
import org.o42a.util.fn.CondInit;
import org.o42a.util.string.ID;


public class DereferenceStep extends Step {

	private final ObjectStepUses uses = new ObjectStepUses(this);
	private final ArgCache<Scope, LocationInfo, RtLink> rtLinks = argCache(
			new IdentityHashMap<>(1),
			(s, l) -> new RtLink(l, this, s));
	private final CondInit<TypeParameters<?>, Obj> iface =
			condInit((tp, o) -> true, DereferenceStep::linkInterface);

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
	protected void localMember(LocalRegistry registry) {
		registry.declareMemberLocal();
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

		assert linkType != null :
			linkObject + " is not a link object";

		if (!this.iface.isInitialized()) {
			this.iface.get(typeParameters);
		}

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

	final RtLink rtLink(LocationInfo location, Scope enclosing) {
		return this.rtLinks.get(enclosing, location);
	}

	private final ObjectStepUses uses() {
		return this.uses;
	}

	private final RtLink rtLink(StepResolver resolver) {
		return rtLink(resolver.getPath(), resolver.getStart());
	}

	private static Obj linkInterface(TypeParameters<?> typeParameters) {
		return typeParameters.getValueType()
				.toLinkType()
				.interfaceRef(typeParameters)
				.getType();
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

		private DereferenceOp(DereferenceOp proto, OpPresets presets) {
			super(proto, presets);
		}

		@Override
		public DereferenceOp setPresets(OpPresets presets) {
			if (presets.is(getPresets())) {
				return this;
			}
			return new DereferenceOp(this, presets);
		}

		@Override
		public HostValueOp value() {
			return pathValueOp();
		}

		@Override
		public ObjectOp pathTarget(CodeDirs dirs) {

			final ObjHolder holder = tempObjHolder(dirs.getAllocator());

			return deref(dirs, holder);
		}

		@Override
		public TargetStoreOp allocateStore(ID id, Code code) {
			return new DereferenceStoreOp(id, code, this);
		}

		@Override
		public TargetStoreOp localStore(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal) {
			return new DereferenceStoreOp(id, getLocal, this);
		}

		private ObjectOp deref(CodeDirs dirs, ObjHolder holder) {
			return host().setPresets(getPresets()).dereference(dirs, holder);
		}

	}

	private final class DereferenceStoreOp extends AbstractObjectStoreOp {

		private final DereferenceOp op;

		DereferenceStoreOp(ID id, Code code, DereferenceOp op) {
			super(id, code);
			this.op = op;
		}

		DereferenceStoreOp(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal,
				DereferenceOp op) {
			super(id, getLocal);
			this.op = op;
		}

		@Override
		public Obj getWellKnownType() {
			return DereferenceStep.this.iface.getKnown();
		}

		@Override
		protected ObjectOp object(CodeDirs dirs, Allocator allocator) {

			final ObjHolder holder = tempObjHolder(
					allocator != null ? allocator : dirs.getAllocator());

			return this.op.deref(dirs, holder);
		}

	}

}
