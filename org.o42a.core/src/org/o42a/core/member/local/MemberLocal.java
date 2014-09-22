/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.member.local;

import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.*;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;


public class MemberLocal extends NonAliasMember {

	private final FieldDeclaration declaration;
	private final MemberLocal propagatedFrom;
	private final Init<Visibility> visibility =
			init(() -> getDeclaration().visibilityOf(this));

	public MemberLocal(FieldBuilder builder) {
		super(
				builder,
				builder.distribute(),
				builder.getMemberOwner());
		this.declaration = builder.getDeclaration();
		this.propagatedFrom = null;
	}

	private MemberLocal(Obj owner, MemberLocal propagatedFrom) {
		super(
				propagatedFrom.getLocation().setDeclaration(
						propagatedFrom.getLastDefinition()),
				propagatedFrom.distributeIn(owner),
				owner);
		this.declaration =
				propagatedFrom.getDeclaration().override(this, distribute());
		this.propagatedFrom = propagatedFrom;
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public final MemberId getMemberId() {
		return getDeclaration().getMemberId();
	}

	public final FieldKey getFieldKey() {
		return getDeclaration().getFieldKey();
	}

	@Override
	public final MemberKey getMemberKey() {
		return getFieldKey().getMemberKey();
	}

	@Override
	public final MemberTypeParameter toTypeParameter() {
		return null;
	}

	@Override
	public final MemberField toField() {
		return null;
	}

	@Override
	public final MemberLocal toLocal() {
		return this;
	}

	@Override
	public final MemberClause toClause() {
		return null;
	}

	@Override
	public Container substance(UserInfo user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final Visibility getVisibility() {
		return this.visibility.get();
	}

	@Override
	public final boolean isOverride() {
		return this.propagatedFrom != null;
	}

	@Override
	public final Member getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public MemberLocal propagateTo(Obj owner) {
		return new MemberLocal(owner, this);
	}

	@Override
	public void resolveAll() {
	}

}
