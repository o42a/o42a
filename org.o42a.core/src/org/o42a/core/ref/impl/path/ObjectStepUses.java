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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.RefUsage.NON_VALUE_REF_USAGES;
import static org.o42a.core.ref.RefUsage.usable;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.ref.path.Step;
import org.o42a.util.use.Usable;
import org.o42a.util.use.UseCaseInfo;


public class ObjectStepUses {

	public static boolean definitionsChange(Obj object, Prediction prediction) {
		for (Scope scope : prediction) {
			if (scope.toObject().value().getDefinitions()
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

	public final void useBy(PathResolver resolver, BoundPath path, int index) {
		if (!resolver.isFullResolution()) {
			return;
		}

		final int nextIdx = index + 1;

		if (path.length() == nextIdx) {
			uses().useBy(resolver, resolver.getUsage());
			return;
		}

		final Step nextStep = path.getSteps()[nextIdx];
		final RefUsage usage = nextStep.getObjectUsage();

		if (usage != null) {
			uses().useBy(resolver, usage);
		}
	}

	public final boolean onlyValueUsed(UseCaseInfo useCase) {
		return !uses().isUsed(useCase, NON_VALUE_REF_USAGES);
	}

	@Override
	public String toString() {
		if (this.uses == null) {
			return super.toString();
		}
		return this.uses.toString();
	}

}
