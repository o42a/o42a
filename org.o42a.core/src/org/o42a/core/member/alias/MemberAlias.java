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
package org.o42a.core.member.alias;

import static org.o42a.core.member.MemberName.aliasName;
import static org.o42a.core.member.field.VisibilityMode.PRIVATE_VISIBILITY;
import static org.o42a.core.ref.RefUsage.TEMP_REF_USAGE;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.util.string.Name.caseSensitiveName;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.*;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.Local;


public class MemberAlias extends Member implements MemberPath {

	private final MemberRegistry registry;
	private final FieldDeclaration declaration;
	private final Ref originalRef;
	private final MemberAlias propagatedFrom;
	private MemberKey aliasedFieldKey;
	private Visibility visibility;
	private Ref ref;

	public MemberAlias(MemberRegistry registry, FieldBuilder builder) {
		super(
				builder,
				builder.distribute(),
				builder.getMemberOwner());
		this.registry = registry;
		this.declaration = builder.getDeclaration();
		this.originalRef = builder.getRef();
		this.propagatedFrom = null;
	}

	private MemberAlias(Obj owner, MemberAlias propagatedFrom) {
		super(
				propagatedFrom.getLocation().setDeclaration(
						propagatedFrom.getLastDefinition()),
				propagatedFrom.distributeIn(owner),
				owner);
		this.registry = null;
		this.propagatedFrom = propagatedFrom;
		this.declaration =
				propagatedFrom.getDeclaration().override(this, distribute());
		this.originalRef =
				propagatedFrom.getOriginalRef().upgradeScope(owner.getScope());
		this.aliasedFieldKey = propagatedFrom.getAliasedFieldKey();
		this.ref = propagatedFrom.getRef().upgradeScope(owner.getScope());
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public final Ref getOriginalRef() {
		return this.originalRef;
	}

	public final Ref getRef() {
		if (this.ref == null) {
			detectAliasType();
		}
		return this.ref;
	}

	public final MemberKey getAliasedFieldKey() {
		getRef();
		return this.aliasedFieldKey;
	}

	@Override
	public MemberId getMemberId() {
		return getDeclaration().getMemberId();
	}

	public final FieldKey getFieldKey() {
		return getDeclaration().getFieldKey();
	}

	@Override
	public MemberKey getMemberKey() {
		return getFieldKey().getMemberKey();
	}

	@Override
	public MemberPath getMemberPath() {
		return this;
	}

	@Override
	public Visibility getVisibility() {
		if (this.visibility != null) {
			return this.visibility;
		}
		return this.visibility =
				getDeclaration().getVisibilityMode().detectVisibility(
						this,
						getDeclaration());
	}

	@Override
	public boolean isOverride() {
		return this.propagatedFrom != null;
	}

	@Override
	public Member getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public boolean isAlias() {
		return true;
	}

	@Override
	public MemberTypeParameter toTypeParameter() {
		return null;
	}

	@Override
	public MemberField toField() {

		final MemberKey aliasedFieldKey = getAliasedFieldKey();

		if (aliasedFieldKey == null) {
			return null;
		}

		return getScope().getContainer().member(aliasedFieldKey).toField();
	}

	@Override
	public MemberClause toClause() {
		return null;
	}

	@Override
	public Container substance(UserInfo user) {
		return getRef().getResolution().resolve();
	}

	@Override
	public Path pathToMember() {
		return getRef().getPath().getPath();
	}

	@Override
	public Member toMember() {
		return this;
	}

	@Override
	public Local toLocal() {
		return null;
	}

	@Override
	public Member propagateTo(Obj owner) {
		return new MemberAlias(owner, this);
	}

	@Override
	public void resolveAll() {
		getRef().resolveAll(
				getScope().resolver().fullResolver(
						dummyRefUser(),
						TEMP_REF_USAGE));
	}

	private void detectAliasType() {

		final AliasTypeDetector detector = new AliasTypeDetector();
		final Resolution resolution =
				getOriginalRef().resolve(getScope().walkingResolver(detector));

		if (resolution.isResolved()) {

			final MemberField field = detector.getField();

			if (field != null) {
				this.aliasedFieldKey = field.getMemberKey();
			}

			this.ref = getOriginalRef();

			return;
		}

		final Obj owner = getScope().toObject();
		final ObjectMembers members = owner.objectMembers();
		final MemberId memberId = getMemberId();
		final MemberName memberName = memberId.toMemberName();
		final MemberId aliasId;

		if (memberName != null) {
			aliasId = aliasName(memberName.getName());
		} else {
			aliasId = aliasName(
					caseSensitiveName(Integer.toString(members.nextId())));
		}

		this.registry.declareMember(
				new MemberAliasField(
						owner,
						getDeclaration()
						.setMemberId(aliasId)
						.setVisibilityMode(PRIVATE_VISIBILITY),
						getOriginalRef()));
	}

}
