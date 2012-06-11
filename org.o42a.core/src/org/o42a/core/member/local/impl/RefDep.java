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
package org.o42a.core.member.local.impl;

import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.*;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.source.LocationInfo;


public final class RefDep extends Dep {

	private final String name;
	private final Ref depRef;
	private final Obj target;
	private ObjectStepUses uses;

	public RefDep(Obj object, Ref depRef, String name) {
		super(object, DepKind.REF_DEP);
		this.depRef = depRef;
		this.name = name;

		final Container container =
				object.getScope().getEnclosingContainer();
		final LocalScope local = container.toLocal();

		assert local != null :
			object + " is not a local object";

		this.target = this.depRef.resolve(local.resolver()).toObject();
	}

	@Override
	public final Object getDepKey() {
		return this.depRef.getPath().getPath();
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final Obj getDepTarget() {
		return this.target;
	}

	@Override
	public final Ref getDepRef() {
		return this.depRef;
	}

	@Override
	public String toString() {
		if (this.depRef == null) {
			return super.toString();
		}
		return "Dep[" + this.depRef + " of " + getObject() + ']';
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {

		final PrefixPath prefix =
				path.cut(1)
				.append(getObject().getScope().getEnclosingScopePath())
				.toPrefix(distributor.getScope());

		return getDepRef().toFieldDefinition()
				.prefixWith(prefix)
				.upgradeScope(distributor.getScope());
	}

	@Override
	protected Container resolveDep(
			PathResolver resolver,
			BoundPath path,
			int index,
			Obj object,
			LocalScope enclosingLocal,
			PathWalker walker) {

		final LocalResolver localResolver = enclosingLocal.resolver();

		if (resolver.isFullResolution()) {
			uses().useBy(resolver, path, index);

			final RefUsage usage;

			if (index == path.length() - 1) {
				// Resolve only the last value.
				usage = resolver.getUsage();
			} else {
				usage = CONTAINER_REF_USAGE;
			}

			this.depRef.resolveAll(
					localResolver.fullResolver(resolver, usage));
		}

		final Resolution resolution = this.depRef.resolve(localResolver);

		walker.refDep(object, this, this.depRef);

		return resolution.toObject();
	}

	@Override
	protected void normalizeDep(
			final PathNormalizer normalizer,
			final LocalScope local) {
		normalizer.skip(normalizer.lastPrediction(), new DepDisabler());
		normalizer.append(
				getDepRef().getPath(),
				uses().nestedNormalizer(normalizer));
	}

	@Override
	protected Path nonNormalizedRemainder(PathNormalizer normalizer) {
		return getDepRef().getPath().getPath();
	}

	@Override
	protected void normalizeStep(Analyzer analyzer) {
		getDepRef().normalize(analyzer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return reproducedPath(
				new RefDep(
						reproducer.getScope().toObject(),
						getDepRef(),
						this.name)
				.toPath());
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
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

}
