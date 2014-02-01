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
package org.o42a.core.ref.impl.prediction;

import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.RefUsage.TEMP_REF_USAGE;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.impl.prediction.PredictionWalker.predictRef;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Pred;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.collect.ReadonlyIterator;
import org.o42a.util.collect.SubIterator;


final class ObjectImplementations extends SubIterator<Pred, Pred> {

	public static ReadonlyIterator<Pred> objectImplementations(Pred object) {
		if (!object.isPredicted()) {
			return object.iterator();
		}
		return new ObjectImplementations(object);
	}

	private final Pred object;

	private ObjectImplementations(Pred object) {
		super(object.iterator());
		this.object = object;
	}

	@Override
	protected Iterator<? extends Pred> nestedIterator(Pred base) {
		if (!base.isPredicted()) {
			return base.iterator();
		}
		return new RevertedAncestors(
				ancestorPrediction(base).iterator(),
				this.object);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectImplementations[" + this.object + ']';
	}

	private static Prediction ancestorPrediction(Pred base) {

		final Scope scope = base.getScope();
		final Obj object = scope.toObject();
		final TypeRef ancestor = object.type().getAncestor();
		final Prediction objectPrediction = base.toScopePrediction();

		if (ancestor == null || ancestor.isStatic()) {
			return exactPrediction(objectPrediction, scope);
		}

		final Ref ancestorRef = ancestorRef(scope, ancestor);

		return predictRef(objectPrediction, ancestorRef);
	}

	private static Ref ancestorRef(Scope scope, TypeRef ancestor) {

		final Ref ancestorRef = ancestor.getRef().prefixWith(
				scope.getEnclosingScopePath().toPrefix(scope));

		// Fully resolve as temporary reference to prevent assertions failures
		// and to not create extra entities such as Deps.
		ancestorRef.resolveAll(
				scope.resolver().fullResolver(dummyRefUser(), TEMP_REF_USAGE));

		return ancestorRef;
	}

	private static final class RevertedAncestors
			extends SubIterator<Pred, Pred> {

		private final Pred object;

		RevertedAncestors(Iterator<Pred> ancestors, Pred object) {
			super(ancestors);
			this.object = object;
		}

		@Override
		protected Iterator<? extends Pred> nestedIterator(Pred ancestor) {
			if (!ancestor.isPredicted()) {
				return ancestor.iterator();
			}

			final Scope reverted = ancestor.revert();

			if (!reverted.derivedFrom(this.object.getScope())) {
				// The reverted ancestor may resolve to incompatible scope.
				// Report the object itself.
				return this.object.iterator();
			}

			final Obj nextObject = reverted.toObject();

			return this.object.setScope(nextObject.getScope()).iterator();
		}

	}

}
