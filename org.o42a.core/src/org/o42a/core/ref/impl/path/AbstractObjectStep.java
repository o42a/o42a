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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.RefUsage.NON_VALUE_REF_USAGES;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.Usable;


public abstract class AbstractObjectStep extends Step {

	private Usable<RefUsage> uses;

	@Override
	public final RefUsage getObjectUsage() {
		return RefUsage.CONTAINER_REF_USAGE;
	}

	@Override
	protected final Obj resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = resolveObject(path, index, start);

		if (object == null) {
			return null;
		}
		if (resolver.isFullResolution()) {
			object.resolveAll();

			final int nextIdx = index + 1;

			if (path.length() > nextIdx) {

				final Step nextStep = path.getSteps()[nextIdx];
				final RefUsage usage = nextStep.getObjectUsage();

				if (usage != null) {
					uses().useBy(resolver, usage);
				}
			} else {
				uses().useBy(resolver, resolver.getUsage());
			}
		}

		walkToObject(walker, object);

		return object;
	}

	protected abstract Obj resolveObject(
			BoundPath path,
			int index,
			Scope start);

	protected abstract void walkToObject(PathWalker walker, Obj object);

	@Override
	protected void normalize(PathNormalizer normalizer) {
		if (uses().selectUse(
				normalizer,
				NON_VALUE_REF_USAGES).isUsed()) {
			return;
		}

		// Try to inline object which is used only by value.
		if (normalizer.getPath().isStatic()) {
			// Path is static. Object can be inlined.
			inline(normalizer);
			return;
		}

		final TypeRef ancestor = ancestor(
				normalizer.getPath(),
				normalizer.getPath(),
				normalizer.getStepStart().distribute());

		if (ancestor.isStatic()) {
			inline(normalizer);
			return;
		}

		// TODO check whether ancestor is stable enough for object to be
		// inlinable.
	}

	protected void inlineObject(PathNormalizer normalizer) {
		// FIXME Implement object inlining.
	}

	protected final Usable<RefUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = RefUsage.usable(this);
	}

	private void inline(PathNormalizer normalizer) {
		inlineObject(normalizer);
		if (normalizer.isStepNormalized()) {
			// TODO update the use graph once an object inlined.
		}
	}

}
