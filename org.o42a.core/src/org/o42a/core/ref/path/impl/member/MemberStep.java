/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl.member;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.core.ref.path.impl.ObjectStepUses.definitionsChange;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.state.DepIR.Op;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.normalizer.InlineValueStep;
import org.o42a.core.ref.impl.normalizer.SameNormalStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ObjectStepUses;
import org.o42a.core.source.LocationInfo;


final class MemberStep extends AbstractMemberStep {

	private ObjectStepUses uses;

	MemberStep(MemberKey memberKey) {
		super(memberKey);
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return defaultFieldDefinition(ref);
	}

	@Override
	protected Container resolve(StepResolver resolver) {

		final Member member = resolveMember(
				resolver.getPath(),
				resolver.getIndex(),
				resolver.getStart());

		if (member == null) {
			return null;
		}
		if (resolver.isFullResolution()) {
			uses().useBy(resolver);
		}
		resolver.getWalker().member(
				resolver.getStart().getContainer(),
				this,
				member);

		return member.substance(resolver.refUser());
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizeMember(normalizer);
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizeMember(normalizer);
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

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		return new MemberRefTargetIR(this);
	}

	private final ObjectStepUses uses() {
		if (this.uses != null) {
			return this.uses;
		}
		return this.uses = new ObjectStepUses(this);
	}

	private void normalizeMember(PathNormalizer normalizer) {

		final Prediction lastPrediction = normalizer.lastPrediction();
		final Member member = resolveMember(
				normalizer.getPath(),
				normalizer.getStepIndex(),
				lastPrediction.getScope());
		final MemberField memberField = member.toField();

		if (memberField == null) {
			// Field required
			normalizer.cancel();
			return;
		}

		final Field field = memberField.field(dummyUser());
		final Prediction prediction = field.predict(lastPrediction);

		if (!prediction.isPredicted()) {
			normalizer.finish();
			return;
		}

		final Obj object = field.toObject();

		if (uses().onlyDereferenced(normalizer)) {
			normalizer.skipToNext(prediction);
			return;
		}
		if (!uses().onlyValueUsed(normalizer)) {
			if (!normalizer.isLastStep()) {
				// Not last object step.
				// Leave the step as is.
				normalizer.skip(prediction, new SameNormalStep(this));
				return;
			}
			// Can not in-line object used otherwise but by value.
			normalizer.finish();
			return;
		}
		if (object.getConstructionMode().isRuntime()) {
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

	private static final class MemberRefTargetIR
			extends AbstractRefFldTargetIR {

		private final MemberStep member;

		MemberRefTargetIR(MemberStep member) {
			this.member = member;
		}

		@Override
		protected AbstractRefFldTargetOp createOp(Op ptr) {
			return new MemberRefTargetOp(this, ptr, this.member);
		}

		@Override
		public String toString() {
			if (this.member == null) {
				return super.toString();
			}
			return this.member.toString();
		}

	}

	private static final class MemberRefTargetOp
			extends AbstractRefFldTargetOp {

		private final MemberStep member;

		MemberRefTargetOp(
				AbstractRefFldTargetIR ir,
				Op ptr,
				MemberStep member) {
			super(ir, ptr);
			this.member = member;
		}

		@Override
		public final Obj getWellKnownOwner() {
			return this.member.getMemberKey().getOrigin().toObject();
		}

		@Override
		protected TargetOp fldOf(CodeDirs dirs, ObjectOp owner) {
			return owner.field(dirs, this.member.getMemberKey());
		}

	}

}
