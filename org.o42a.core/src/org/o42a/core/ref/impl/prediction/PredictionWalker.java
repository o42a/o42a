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
package org.o42a.core.ref.impl.prediction;

import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.Prediction.scopePrediction;
import static org.o42a.core.ref.Prediction.unpredicted;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


public class PredictionWalker implements PathWalker {

	public static Prediction predictRef(Ref ref, Scope start) {

		final PredictionWalker walker = new PredictionWalker();
		final Resolution resolution =
				ref.resolve(start.walkingResolver(dummyUser(), walker));

		if (resolution.isError()) {
			return unpredicted(resolution.getScope());
		}

		return walker.getPrediction();
	}

	public static Prediction predictRef(Ref ref, Prediction start) {
		if (start.isExact()) {

			final Resolution resolution =
					ref.resolve(start.getScope().dummyResolver());

			if (resolution.isError()) {
				return unpredicted(resolution.getScope());
			}

			return exactPrediction(resolution.getScope());
		}

		final PredictionWalker walker = new PredictionWalker(start);
		final Resolution resolution = ref.resolve(
				start.getScope().walkingResolver(dummyUser(), walker));

		if (resolution.isError()) {
			return unpredicted(resolution.getScope());
		}

		return walker.getPrediction();
	}

	private Prediction prediction;

	public PredictionWalker() {
	}

	public PredictionWalker(Prediction start) {
		this.prediction = start;
	}

	public final Prediction getPrediction() {
		return this.prediction;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		return set(exactPrediction(root));
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		assert this.prediction == null || this.prediction.getScope() == start :
			"Wrong start of the path: " + start + ", but "
			+ this.prediction.getScope() + " expected";

		if (this.prediction == null) {
			this.prediction = scopePrediction(start);
		}

		return this.prediction.isPredicted();
	}

	@Override
	public boolean module(Step step, Obj module) {
		return set(exactPrediction(module.getScope()));
	}

	@Override
	public boolean skip(Step step, Scope scope) {
		return true;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return set(exactPrediction(scope));
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
		if (getPrediction().isExact()) {
			return set(exactPrediction(enclosing.getScope()));
		}
		return set(scopePrediction(enclosing.getScope()));
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return set(member.substance(dummyUser())
				.getScope().predict(getPrediction()));
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		// Array elements not predictable yet.
		return set(unpredicted(element.getScope()));
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {

		final LocalScope local =
				object.getScope().getEnclosingScope().toLocal();

		if (getPrediction().isExact()) {

			final Resolution resolution =
					dependency.resolve(local.dummyResolver());

			if (resolution.isError()) {
				set(unpredicted(resolution.getScope()));
			}

			return set(exactPrediction(resolution.getScope()));
		}

		return set(predictRef(dependency, local));
	}

	@Override
	public boolean object(Step step, Obj object) {
		return set(object.getScope().predict(getPrediction()));
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		this.prediction = unpredicted(last);
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private final boolean set(Prediction prediction) {
		this.prediction = prediction;
		return prediction.isPredicted();
	}

}
