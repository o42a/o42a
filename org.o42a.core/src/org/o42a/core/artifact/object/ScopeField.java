/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import static org.o42a.core.artifact.object.Derivation.IMPLICIT_PROPAGATION;
import static org.o42a.core.artifact.object.Obj.SCOPE_MEMBER_ID;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.ScopeFld;
import org.o42a.core.ir.field.ScopeFldOp;
import org.o42a.core.ir.local.LclOp;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.OverrideMode;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;
import org.o42a.util.use.UserInfo;


final class ScopeField extends ObjectField {

	private final ScopeField overridden;

	ScopeField(Obj owner) {
		super(
				owner.toMemberOwner(),
				fieldDeclaration(
						owner,
						owner.distributeIn(owner),
						SCOPE_MEMBER_ID)
				.setVisibility(Visibility.PROTECTED));
		this.overridden = null;
		setFieldArtifact(owner.getScope().getEnclosingContainer().toObject());
	}

	private ScopeField(MemberOwner owner, ScopeField overridden) {
		super(owner, overridden, null, OverrideMode.PROPAGATE);
		this.overridden = overridden;
	}

	@Override
	public final boolean isScopeField() {
		return true;
	}

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	public Obj getArtifact() {

		final Obj artifact = getFieldArtifact();

		if (artifact != null) {
			return artifact;
		}

		final UserInfo user = dummyUser();
		final Obj newArtifact;
		final Obj newOwner = getEnclosingContainer().toObject();
		final ObjectType newOwnerType = newOwner.type(user);
		final Obj ancestor = newOwnerType.getAncestor().typeObject(user);
		final org.o42a.core.member.Member ancestorMember =
			ancestor.member(getKey());

		if (ancestorMember != null) {
			// Scope field present in ancestor.
			// Preserve an ancestor`s scope.
			newArtifact = ancestorMember.substance(user).toObject();
		} else {

			final ObjectType origin =
				getKey().getOrigin().toObject().type(user);

			if (newOwnerType.derivedFrom(origin, IMPLICIT_PROPAGATION)) {
				// Scope field declared in implicit sample.
				// Update owner with an actual one.
				newArtifact = newOwner.getEnclosingContainer().toObject();
			} else {
				// In the rest of the cases preserve an old scope.
				newArtifact = this.overridden.getArtifact();
			}
		}

		setFieldArtifact(newArtifact);

		return newArtifact;
	}

	@Override
	protected ScopeField propagate(MemberOwner owner) {
		return new ScopeField(owner, this);
	}

	@Override
	protected FieldIR<Obj> createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends FieldIR<Obj> {

		IR(Generator generator, Field<Obj> field) {
			super(generator, field);
		}

		@Override
		public ScopeFldOp field(Code code, ObjOp host) {
			return (ScopeFldOp) super.field(code, host);
		}

		@Override
		protected ScopeFld declare(SubData<?> data, ObjectBodyIR bodyIR) {

			final ScopeFld fld = new ScopeFld(bodyIR, getField());

			fld.declare(
					data,
					getField().getArtifact().toObject().ir(
							getGenerator()).getMainBodyIR());

			return fld;
		}

		@Override
		protected LclOp allocateLocal(LocalBuilder builder, AllocationCode code) {
			throw new UnsupportedOperationException();
		}

	}

}
