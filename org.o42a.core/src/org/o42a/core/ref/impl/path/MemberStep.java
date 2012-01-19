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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.impl.path.ObjectStepUses.definitionsChange;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.impl.normalizer.InlineStep;
import org.o42a.core.ref.impl.normalizer.SameNormalStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public class MemberStep extends AbstractMemberStep {

	private ObjectStepUses uses;

	public MemberStep(MemberKey memberKey) {
		super(memberKey);
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Member member = resolveMember(path, index, start);

		if (member == null) {
			return null;
		}
		if (resolver.isFullResolution()) {
			uses().useBy(resolver, path, index);
		}
		walker.member(start.getContainer(), this, member);

		return member.substance(resolver);
	}

	@Override
	protected Scope revert(Scope target) {
		return target.getEnclosingScope();
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		if (normalizer.isNormalizationStarted()
				&& normalizer.getStepStart().getScope()
				== normalizer.getNormalizedStart()) {
			// Member of non-normalizable scope.
			normalizer.cancel();
			return;
		}

		final Member member = resolveMember(
				normalizer.getPath(),
				normalizer.getStepIndex(),
				normalizer.getStepStart().getScope());

		if (member == null) {
			normalizer.cancel();
			return;
		}
		if (normalizer.getStepStart().getScope() != member.getDefinedIn()) {
			// Require explicitly declared member.
			normalizer.cancel();
			return;
		}

		final MemberField memberField = member.toField();

		if (memberField == null) {
			// Field required
			normalizer.cancel();
			return;
		}

		final Field<?> field = memberField.field(dummyUser());
		final Artifact<?> artifact = field.getArtifact();
		final Prediction prediction = field.predict(normalizer.getStepStart());
		final Link link = artifact.toLink();

		if (link != null) {
			// Append the link target.
			if (linkUpdated(normalizer, prediction)) {
				return;
			}
			normalizer.append(
					link.getTargetRef().getRescopedRef().getPath());
		}
		if (!normalizer.isLastStep()) {
			// Not last object step.
			// Leave the step as is.
			normalizer.add(prediction, new SameNormalStep(this));
			return;
		}

		final Obj object = artifact.toObject();

		if (!uses().onlyValueUsed(normalizer)) {
			// Can not in-line object used otherwise but by value.
			normalizer.cancel();
			return;
		}
		if (definitionsChange(object, prediction)) {
			normalizer.cancel();
			return;
		}

		final InlineValue inline = object.value().getDefinitions().inline(
				normalizer.getNormalizer());

		if (inline == null) {
			normalizer.cancel();
			return;
		}

		normalizer.add(prediction, new InlineStep(this, inline) {
			@Override
			public void cancel() {
			}
		});
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer,
			Scope origin,
			Scope scope) {

		final Member member = origin.getContainer().member(getMemberKey());

		if (origin.getContainer().toClause() == null
				&& member.toClause() == null) {
			// Neither clause, nor member of clause.
			// Return unchanged.
			return unchangedPath(toPath());
		}

		final MemberKey reproductionKey =
				getMemberKey().getMemberId().reproduceFrom(origin).key(scope);

		return reproducedPath(reproductionKey.toPath());
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private boolean linkUpdated(
			PathNormalizer normalizer,
			Prediction prediction) {

		final Scope stepStart = normalizer.getStepStart().getScope();

		for (Scope replacement : prediction) {
			if (replacement.toField().getDefinedIn() != stepStart) {
				normalizer.cancel();
				return true;
			}
		}

		return false;
	}

}
