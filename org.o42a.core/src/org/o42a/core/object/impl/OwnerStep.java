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
package org.o42a.core.object.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.impl.ScopeField.objectScope;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.PathReproduction.outOfClausePath;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.impl.ObjectStepUses.definitionsChange;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.ir.op.RefTargetIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.impl.normalizer.InlineValueStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.ref.path.impl.member.AbstractMemberStep;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.string.Name;


public final class OwnerStep
		extends AbstractMemberStep
		implements ReversePath {

	private final Obj object;
	private ObjectStepUses uses;

	public OwnerStep(Obj object, MemberKey memberKey) {
		super(memberKey);
		this.object = object;
	}

	@Override
	public Scope revert(Scope target) {
		return this.object.meta().findIn(target).getScope();
	}

	@Override
	protected void combineWithConstructor(
			PathRebuilder rebuilder,
			Step step,
			ObjectConstructor constructor) {
		if (!constructor.mayContainDeps()) {
			return;
		}

		final Dep dep = replaceWithDep(rebuilder, step, null);

		dep.setSynthetic(constructor);
	}

	@Override
	protected void combineWithLocal(
			PathRebuilder rebuilder,
			Step step,
			Local local) {
		assert !local.isField() :
			"Can not combine with local field";
		replaceWithDep(rebuilder, step, local.getName());
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Scope start = resolver.getStart();
		final Obj object = start.toObject();

		if (resolver.isFullResolution()) {
			uses().useBy(resolver);
		} else if (!object.membersResolved()) {

			final Container result = resolveWhenMembersNotResolved(object);

			if (result != null) {
				return reportToWalker(resolver, object, result);
			}
		}

		final Container result = findParentObject(resolver);

		if (result == null) {
			return result;
		}

		return reportToWalker(resolver, object, result);
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

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		return defaultTargetIR(refIR);
	}

	private Dep replaceWithDep(
			PathRebuilder rebuilder,
			@SuppressWarnings("unused") Step step,
			Name name) {

		final Obj origin =
				rebuilder.cutPath(1).resolve(
						rebuilder.getPath()
						.getOrigin()
						.resolver()
						.toPathResolver())
				.getObject();
		final Container owner;

		if (origin.membersResolved()) {
			owner = origin.member(getMemberKey()).substance(dummyUser());
		} else {
			owner = resolveWhenMembersNotResolved(origin);
		}

		final Ref ref =
				rebuilder.restPath(owner.getScope())
				.target(origin.distributeIn(owner.toObject()));
		final Dep dep = origin.deps().addDep(name, ref);

		rebuilder.replaceRest(dep);

		/* TODO store only next step in the dependency instead of the rest
		 * of the path
		final Ref ref =
				step.toPath()
				.bind(rebuilder, owner.getScope())
				.target(origin.distributeIn(owner));
		final Dep dep = origin.deps().addDep(name, ref);

		rebuilder.replace(dep);
		 */

		return dep;
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private Container resolveWhenMembersNotResolved(Obj object) {

		final ObjectType type = object.type();

		if (isEnclosingScopePath(object)) {
			return object.getScope().getEnclosingScope().getContainer();
		}
		if (type.isResolved()) {
			return objectScope(object, getMemberKey());
		}

		return null;
	}

	private boolean isEnclosingScopePath(Obj start) {
		return equals(
				start.getScope()
				.getEnclosingScopePath()
				.lastStep());
	}

	private Container findParentObject(StepResolver resolver) {

		final Member member = resolveMember(
				resolver.getPath(),
				resolver.getIndex(),
				resolver.getStart());

		if (member == null) {
			return null;
		}

		return member.substance(resolver.refUser());
	}

	private Container reportToWalker(
			StepResolver resolver,
			Obj object,
			Container objectScope) {
		resolver.getWalker().up(object, this, objectScope, this);
		return objectScope;
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
