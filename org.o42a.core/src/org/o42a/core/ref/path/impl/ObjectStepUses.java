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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.ref.RefUsage.NON_DEREF_USAGES;
import static org.o42a.core.ref.RefUsage.NON_VALUE_REF_USAGES;
import static org.o42a.core.ref.RefUsage.usable;

import org.o42a.analysis.use.Usable;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Pred;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;


public class ObjectStepUses {

	public static boolean definitionsChange(Obj object, Prediction prediction) {
		for (Pred pred : prediction) {
			if (!pred.isPredicted()) {
				return true;
			}
			if (pred.getScope()
					.toObject()
					.value()
					.getDefinitions()
					.updatedSince(object)) {
				// Definitions may change in descendant.
				// Can not in-line object.
				return true;
			}
		}
		return false;
	}

	private final Usable<RefUsage> uses;

	public ObjectStepUses(Object used) {
		this.uses = usable(used);
	}

	public ObjectStepUses(String name, Object used) {
		this.uses = usable(name, used);
	}

	public final Usable<RefUsage> uses() {
		return this.uses;
	}

	public final RefUsage usage(StepResolver resolver) {

		final int nextIdx = resolver.getIndex() + 1;
		final BoundPath path = resolver.getPath();

		if (path.length() == nextIdx) {
			return resolver.refUsage();
		}

		final Step nextStep = path.getSteps()[nextIdx];

		return nextStep.getObjectUsage();
	}

	public final void useBy(StepResolver resolver) {

		final RefUsage usage = usage(resolver);

		if (usage != null) {
			uses().useBy(resolver, usage);
		}
	}

	public final boolean onlyValueUsed(PathNormalizer normalizer) {
		if (!uses().hasUses()) {
			return false;
		}
		if (!uses().hasUses(NON_VALUE_REF_USAGES)) {
			return true;
		}
		if (!normalizer.isNested()) {
			return false;
		}
		if (!normalizer.getNested().onlyValueUsed()) {
			return false;
		}

		return normalizer.getStepIndex() + 1 == normalizer.getPath().length();
	}

	public final boolean onlyDereferenced(PathNormalizer normalizer) {
		if (!uses().hasUses()) {
			return false;
		}
		if (!uses().hasUses(NON_DEREF_USAGES)) {
			return true;
		}
		if (!normalizer.isNested()) {
			return false;
		}
		if (!normalizer.getNested().onlyDereferenced()) {
			return false;
		}

		return normalizer.getStepIndex() + 1 == normalizer.getPath().length();
	}

	public final NestedNormalizer nestedNormalizer(PathNormalizer normalizer) {
		return new Nested(this, normalizer);
	}

	@Override
	public String toString() {
		if (this.uses == null) {
			return super.toString();
		}
		return this.uses.toString();
	}

	private static final class Nested implements NestedNormalizer {

		private final ObjectStepUses uses;
		private final PathNormalizer normalizer;

		Nested(ObjectStepUses uses, PathNormalizer normalizer) {
			this.uses = uses;
			this.normalizer = normalizer;
		}

		@Override
		public boolean onlyValueUsed() {
			return this.uses.onlyValueUsed(this.normalizer);
		}

		@Override
		public boolean onlyDereferenced() {
			return this.uses.onlyDereferenced(this.normalizer);
		}

	}

}
