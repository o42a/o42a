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
package org.o42a.core.object.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.impl.ObjectStepUses.definitionsChange;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.impl.normalizer.InlineValueStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.AbstractMemberStep;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.source.LocationInfo;


public final class ParentObjectStep
		extends AbstractMemberStep
		implements ReversePath {

	private final Obj object;
	private ObjectStepUses uses;

	public ParentObjectStep(Obj object, MemberKey memberKey) {
		super(memberKey);
		this.object = object;
	}

	@Override
	public Scope revert(Scope target) {
		return this.object.findIn(target).getScope();
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = start.toObject();

		if (resolver.isFullResolution()) {
			uses().useBy(resolver, path, index);
		} else if (!object.membersResolved()) {

			final Scope self = getMemberKey().getOrigin();

			if (start.is(self)) {

				final Container result = self.getEnclosingContainer();

				walker.up(object, this, result, this);

				return result;
			}
		}

		final Member member = resolveMember(path, index, start);

		if (member == null) {
			return null;
		}

		final Container result = member.substance(resolver);

		walker.up(object, this, result, this);

		return result;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizeParent(normalizer);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizeParent(normalizer);
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer,
			Scope origin,
			Scope scope) {

		final Clause fromClause = origin.getContainer().toClause();

		if (fromClause == null) {
			// Walked out of object, containing clauses.
			if (!reproducer.phraseCreatesObject()) {
				return outOfClausePath(SELF_PATH, toPath());
			}
			return outOfClausePath(
					scope.getEnclosingScopePath(),
					toPath());

		}

		final Clause enclosingClause = fromClause.getEnclosingClause();

		if (enclosingClause == null && !fromClause.requiresInstance()) {
			// Left stand-alone clause without enclosing object.
			return outOfClausePath(
					scope.getEnclosingScopePath(),
					SELF_PATH);
		}

		// Update to actual enclosing scope path.
		return reproducedPath(scope.getEnclosingScopePath());
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private void normalizeParent(PathNormalizer normalizer) {

		final Member member = resolveMember(
				normalizer.getPath(),
				normalizer.getStepIndex(),
				normalizer.lastPrediction().getScope());
		final Container enclosing = member.substance(dummyUser());

		if (!normalizer.up(enclosing.getScope(), toPath(), this)) {
			return;
		}

		final Prediction prediction = normalizer.lastPrediction();
		final Obj object = enclosing.toObject();

		if (!uses().onlyValueUsed(normalizer)) {
			if (!normalizer.isLastStep()) {
				// Do not add parent step,
				// as normalized path start is just reached.
				return;
			}
			// Can not in-line object used otherwise but by value.
			normalizer.finish();
			return;
		}
		if (definitionsChange(object, prediction)) {
			normalizer.finish();
			return;
		}

		final InlineValue inline = object.value().getDefinitions().inline(
				normalizer.getNormalizer());

		if (inline == null) {
			normalizer.finish();
			return;
		}

		normalizer.inline(prediction, new InlineValueStep(inline));
	}

}
