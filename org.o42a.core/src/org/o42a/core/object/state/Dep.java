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
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.source.LocationInfo;


public final class Dep extends Step {

	private final Obj object;
	private final String name;
	private final Ref ref;
	private final Obj target;
	private ObjectStepUses uses;
	private boolean disabled;

	public Dep(Obj object, Ref ref, String name) {
		this.object = object;
		this.ref = ref;
		this.name = name;
		this.target = target();
		assert !this.target.getConstructionMode().isRuntime() :
			"Can not find an interface of run-time constructed dependency";
	}

	public final boolean isDisabled() {
		return this.disabled;
	}

	public final void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public final PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final String getName() {
		return this.name;
	}

	public final Object getDepKey() {
		return this.ref.getPath().getPath();
	}

	public final Obj getDepTarget() {
		return this.target;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return "Dep[" + this.ref + " of " + getObject() + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {

		final PrefixPath prefix =
				ref.getPath().cut(1)
				.append(getObject().getScope().getEnclosingScopePath())
				.toPrefix(ref.getScope());

		return getRef().toFieldDefinition()
				.prefixWith(prefix)
				.upgradeScope(ref.getScope());
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Obj object = resolver.getStart().toObject();

		assert object != null :
			"Dependency " + resolver
			+ " should be resolved against object, but were not: "
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
		}

		final Resolution resolution = this.ref.resolve(localResolver);

		resolver.getWalker().dep(object, this, this.ref);

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
		return reproducedPath(
				new Dep(
						reproducer.getScope().toObject(),
						getRef(),
						this.name)
				.toPath());
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

	private Obj target() {

		final Container container =
				this.object.getScope().getEnclosingContainer();
		final LocalScope local = container.toLocal();

		assert local != null :
			this.object + " is not a local object";

		final Obj target = this.ref.resolve(local.resolver()).toObject();

		if (!target.getConstructionMode().isRuntime()) {
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
			ignore();
			return path;
		}

		@Override
		public void ignore() {
			setDisabled(true);
		}

		@Override
		public void cancel() {
			setDisabled(false);
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
