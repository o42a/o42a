/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.Prediction.exactPrediction;
import static org.o42a.core.ref.Prediction.startPrediction;
import static org.o42a.core.ref.Prediction.unpredicted;
import static org.o42a.core.ref.impl.prediction.DerefPrediction.derefPrediction;
import static org.o42a.core.ref.impl.prediction.EnclosingPrediction.enclosingPrediction;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class PredictionWalker implements PathWalker {

	public static Prediction predictRef(Prediction start, Ref ref) {
		if (start.isExact()) {

			final Resolution resolution =
					ref.resolve(start.getScope().resolver());

			if (resolution.isError()) {
				return unpredicted(resolution.getScope());
			}

			return exactPrediction(start, resolution.getScope());
		}

		final PredictionWalker walker = new PredictionWalker(start);
		final Scope startFrom;

		if (start.getScope().derivedFrom(ref.getScope())) {
			startFrom = start.getScope();
		} else {
			startFrom = ref.getScope();
		}

		final Resolution resolution =
				ref.resolve(startFrom.walkingResolver(walker));

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

		final Prediction basePrediction;

		if (this.prediction == null) {
			basePrediction = new InitialPrediction(root);
		} else {
			basePrediction = this.prediction;
		}

		return set(exactPrediction(basePrediction, root));
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		assert this.prediction == null || this.prediction.getScope().is(start) :
			"Wrong start of the path: " + start + ", but "
			+ this.prediction.getScope() + " expected";
		if (this.prediction == null) {
			this.prediction = startPrediction(start);
		}
		return this.prediction.isPredicted();
	}

	@Override
	public boolean module(Step step, Obj module) {
		return set(exactPrediction(getPrediction(), module.getScope()));
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return set(exactPrediction(getPrediction(), scope));
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return set(enclosingPrediction(
				getPrediction(),
				enclosing.getScope(),
				step.toPath(),
				reversePath));
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return set(member.substance(dummyUser())
				.getScope().predict(getPrediction()));
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return set(derefPrediction(getPrediction(), link));
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return set(predictRef(getPrediction(), local.ref()));
	}

	@Override
	public boolean dep(Obj object, Dep dep) {

		final Scope enclosingScope = dep.enclosingScope(object.getScope());

		if (getPrediction().isExact()) {

			final Resolution resolution =
					dep.ref().resolve(enclosingScope.resolver());

			if (resolution.isError()) {
				return set(unpredicted(resolution.getScope()));
			}

			return set(exactPrediction(getPrediction(), resolution.getScope()));
		}

		final Path enclosingPath = object.getScope().getEnclosingScopePath();
		final Step[] steps = enclosingPath.getSteps();

		assert steps.length == 1 :
			"Wrong object owner path";

		final ReversePath ownerStep = (ReversePath) steps[0];

		return set(predictRef(
				enclosingPrediction(
						getPrediction(),
						enclosingScope.getScope(),
						enclosingPath,
						ownerStep),
				dep.ref()));
	}

	@Override
	public boolean object(Step step, Obj object) {
		return set(object.getScope().predict(getPrediction()));
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
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
