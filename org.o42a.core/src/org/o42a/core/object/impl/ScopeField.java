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
package org.o42a.core.object.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.object.type.Derivation.IMPLICIT_PROPAGATION;

import org.o42a.analysis.use.UserInfo;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.scope.ScopeFld;
import org.o42a.core.ir.field.scope.ScopeFldOp;
import org.o42a.core.ir.local.LclOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.common.ObjectField;
import org.o42a.core.ref.path.Path;


public final class ScopeField extends ObjectField {

	private final ScopeField overridden;

	public ScopeField(Obj owner, MemberId memberId) {
		super(
				owner.toMemberOwner(),
				fieldDeclaration(
						owner,
						owner.distributeIn(owner),
						memberId)
				.setVisibility(Visibility.PROTECTED));
		this.overridden = null;
		setScopeObject(owner.getScope().getEnclosingContainer().toObject());
	}

	private ScopeField(MemberField member, ScopeField overridden) {
		super(member);
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
	public Obj toObject() {

		final Obj object = getScopeObject();

		if (object != null) {
			return object;
		}

		final UserInfo user = dummyUser();
		final Obj newObject;
		final Obj newOwner = getEnclosingContainer().toObject();
		final ObjectType newOwnerType = newOwner.type();
		final Obj ancestor = newOwnerType.getAncestor().typeObject();
		final Member ancestorMember = ancestor.member(getKey());

		if (ancestorMember != null) {
			// Scope field present in ancestor.
			// Preserve an ancestor`s scope.
			newObject = ancestorMember.substance(user).toObject();
		} else {

			final ObjectType origin = getKey().getOrigin().toObject().type();

			if (newOwnerType.derivedFrom(origin, IMPLICIT_PROPAGATION)) {
				// Scope field declared in implicit sample.
				// Update owner with an actual one.
				newObject = newOwner.getEnclosingContainer().toObject();
			} else {
				// In the rest of the cases preserve an old scope.
				newObject = this.overridden.toObject();
			}
		}

		setScopeObject(newObject);

		return newObject;
	}

	@Override
	protected ObjectField propagate(MemberField member) {
		return new ScopeField(member, this);
	}

	@Override
	protected FieldIR createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends FieldIR {

		IR(Generator generator, Field field) {
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
					getField().toObject().ir(getGenerator()).getMainBodyIR());

			return fld;
		}

		@Override
		protected LclOp allocateLocal(
				CodeBuilder builder,
				AllocationCode code) {
			throw new UnsupportedOperationException();
		}

	}

}
