/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.object.state;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.StepOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public final class Dep extends Step implements SubID {

	private final Obj declaredIn;
	private final Ref ref;
	private final ID id;
	private final Obj target;
	private ObjectStepUses uses;
	private byte disabled;

	Dep(Obj declaredIn, Ref ref, ID id) {
		this.declaredIn = declaredIn;
		this.ref = ref;
		this.id = id;
		this.target = target();
		assert !this.target.getConstructionMode().isRuntime()
			|| this.target.getConstructionMode().isPredefined():
			"Can not find an interface of run-time constructed dependency";
	}

	public final Obj getDeclaredIn() {
		return this.declaredIn;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final Object getDepKey() {
		return this.ref.getPath().getPath();
	}

	public final Obj getDepTarget() {
		return this.target;
	}

	public final boolean isDisabled() {
		return this.disabled > 0;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	public final ID toID() {
		return this.id;
	}

	@Override
	public final ID toDisplayID() {
		return this.id;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {

		final PrefixPath prefix =
				ref.getPath().cut(1)
				.append(getDeclaredIn().getScope().getEnclosingScopePath())
				.toPrefix(ref.getScope());

		return getRef().toFieldDefinition()
				.prefixWith(prefix)
				.upgradeScope(ref.getScope());
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj object = resolver.getStart().toObject();

		assert object != null :
			"Dependency should be resolved against object, but were not: "
			+ resolver.getStart();

		final LocalScope enclosingLocal =
				object.getScope().getEnclosingContainer().toLocal();

		assert enclosingLocal != null :
			object + " is inside " + object.getScope().getEnclosingContainer()
			+ ", which is not a local scope";

		final LocalResolver localResolver = enclosingLocal.resolver();

		if (resolver.isFullResolution()) {
			uses().useBy(resolver);

			final RefUsage usage;

			if (resolver.isLastStep()) {
				// Resolve only the last value.
				usage = resolver.getUsage();
			} else {
				usage = CONTAINER_REF_USAGE;
			}

			this.ref.resolveAll(
					localResolver.fullResolver(resolver, usage));

			final ObjectDeps deps = getDeclaredIn().deps();

			deps.depResolved(this);
		}

		final Obj resolution = getRef().resolve(localResolver).toObject();

		resolver.getWalker().dep(object, this, getRef());

		return resolution.toObject();
	}

	@Override
	protected final void normalize(PathNormalizer normalizer) {

		final Obj object = normalizer.lastPrediction().getScope().toObject();
		final Scope objectScope = object.getScope();
		final LocalScope enclosingLocal =
				objectScope.getEnclosingContainer().toLocal();

		if (!normalizer.up(
				enclosingLocal,
				objectScope.getEnclosingScopePath(),
				new ReversePath() {
					@Override
					public Scope revert(Scope target) {
						return object.meta().findIn(target).getScope();
					}
				})) {
			return;
		}

		normalizer.skip(normalizer.lastPrediction(), new DepDisabler());
		normalizer.append(
				getRef().getPath(),
				uses().nestedNormalizer(normalizer));
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		throw new UnsupportedOperationException(
				"Dep can not be a part of static path " + normalizer.getPath());
	}

	@Override
	protected Path nonNormalizedRemainder(PathNormalizer normalizer) {
		return getRef().getPath().getPath();
	}

	@Override
	protected void normalizeStep(Analyzer analyzer) {
		getRef().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {

		final Ref ref = getRef().reproduce(reproducer.getReproducer());

		if (ref == null) {
			return null;
		}

		final Dep reproduction =
				reproducer.getScope().toObject().deps().addDep(ref);

		return reproducedPath(reproduction.toPath());
	}

	@Override
	protected final PathOp op(PathOp start) {
		assert !isDisabled() :
			this + " is disabled";
		return new Op(start, this);
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	final void reuseDep() {
		if (this.disabled > 0) {
			this.disabled = 0;
		}
	}

	private void ignoreDep() {
		if (this.disabled == 0) {
			this.disabled = 1;
		}
	}

	private void enableDep() {
		this.disabled = -1;
	}

	private Obj target() {

		final Container container =
				this.declaredIn.getScope().getEnclosingContainer();
		final LocalScope local = container.toLocal();

		assert local != null :
			this.declaredIn + " is not a local object";

		final Obj target = this.ref.resolve(local.resolver()).toObject();

		if (!target.getConstructionMode().isRuntime()
				|| target.getConstructionMode().isPredefined()) {
			return target;
		}

		final Link link = target.getDereferencedLink();

		if (link != null) {
			return link.getValueStruct().getTypeRef().getType();
		}

		return target.type().getAncestor().getType();
	}

	private final class DepDisabler extends NormalAppender {

		@Override
		public Path appendTo(Path path) {
			ignoreDep();
			return path;
		}

		@Override
		public void ignore() {
			ignoreDep();
		}

		@Override
		public void cancel() {
			enableDep();
		}

		@Override
		public String toString() {
			return "-";
		}

	}

	private static final class Op extends StepOp<Dep> {

		Op(PathOp start, Dep step) {
			super(start, step);
		}

		@Override
		public HostValueOp value() {
			return targetValueOp();
		}

		@Override
		public HostOp target(CodeDirs dirs) {
			return start()
					.materialize(dirs, tempObjHolder(dirs.getAllocator()))
					.dep(dirs, getStep());
		}

	}

}
