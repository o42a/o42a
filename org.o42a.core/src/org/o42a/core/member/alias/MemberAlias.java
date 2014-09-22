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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.MemberIdKind.FIELD_ALIAS;
import static org.o42a.core.member.field.VisibilityMode.PRIVATE_VISIBILITY;
import static org.o42a.core.ref.RefUsage.BODY_REF_USAGE;
import static org.o42a.core.ref.RefUsage.TEMP_REF_USAGE;
import static org.o42a.util.Misc.coalesce;
import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.*;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.fn.Init;


public final class MemberAlias extends Member {

	private final MemberRegistry registry;
	private final FieldDeclaration declaration;
	private final Ref originalRef;
	private final MemberAlias propagatedFrom;
	private final AliasPath memberPath = new AliasPath(this);
	private final Init<AliasRef> aliasRef =
			init(() -> coalesce(detectAliasRef(), this::createAliasField));
	private final Init<Visibility> visibility =
			init(() -> getDeclaration().visibilityOf(this));

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
		this.aliasRef.set(new AliasRef(this, propagatedFrom.getAliasRef()));
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public final Ref getOriginalRef() {
		return this.originalRef;
	}

	public final Ref getRef() {
		return getAliasRef().getRef();
	}

	public final MemberRef getAliasedField() {
		return getAliasRef().getAliasedField();
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
	public final MemberPath getMemberPath() {
		return this.memberPath;
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
	public final boolean isAlias() {
		return true;
	}

	@Override
	public final MemberTypeParameter toTypeParameter() {
		return null;
	}

	@Override
	public final MemberField toField() {

		final MemberRef aliasedField = getAliasedField();

		return aliasedField != null ? aliasedField.getMember().toField() : null;
	}

	@Override
	public final MemberLocal toLocal() {
		return null;
	}

	@Override
	public final MemberClause toClause() {
		return null;
	}

	@Override
	public Container substance(UserInfo user) {

		final Container substance;

		if (user.isDummyUser()) {
			substance = getRef().getResolution().resolve();
		} else {

			final FullResolver fullResolver =
					getScope()
					.resolver()
					.fullResolver(user, BODY_REF_USAGE);

			substance = getRef().resolveAll(fullResolver).resolve();
		}

		return substance;
	}

	@Override
	public MemberAlias propagateTo(Obj owner) {
		return new MemberAlias(owner, this);
	}

	@Override
	public void resolveAll() {
		getRef().resolveAll(
				getScope().resolver().fullResolver(
						dummyUser(),
						TEMP_REF_USAGE));
	}

	private final AliasRef getAliasRef() {
		return this.aliasRef.get();
	}

	private AliasRef detectAliasRef() {

		final AliasTypeDetector detector = new AliasTypeDetector();
		final Resolution resolution =
				getOriginalRef().resolve(getScope().walkingResolver(detector));

		if (!resolution.isResolved()) {
			return null;
		}

		final MemberField field = detector.getField();

		if (field != null) {
			return new AliasRef(this, field);
		}

		return new AliasRef(getOriginalRef());
	}

	private AliasRef createAliasField() {

		final Obj owner = getScope().toObject();
		final MemberId memberId = getMemberId();
		final MemberName memberName = memberId.toMemberName();
		final MemberId aliasId;

		if (memberName != null) {
			aliasId = FIELD_ALIAS.memberName(memberName.getName());
		} else {
			aliasId = this.registry.tempMemberId(FIELD_ALIAS);
		}

		final MemberAliasField alias = new MemberAliasField(
				owner,
				getDeclaration()
				.setMemberId(aliasId)
				.setVisibilityMode(PRIVATE_VISIBILITY),
				getOriginalRef());

		this.registry.declareMember(alias);

		return new AliasRef(this, alias);
	}

	private static final class AliasPath implements MemberPath {

		private final MemberAlias alias;

		AliasPath(MemberAlias alias) {
			this.alias = alias;
		}

		@Override
		public Path pathToMember() {
			return this.alias.getRef().getPath().getPath();
		}

		@Override
		public Member toMember() {
			return this.alias;
		}

		@Override
		public Local toLocal() {
			return null;
		}

		@Override
		public String toString() {
			if (this.alias == null) {
				return super.toString();
			}
			return this.alias.toString();
		}

	}

	private static final class AliasRef {

		private final Ref ref;
		private final MemberRef aliasedField;

		AliasRef(Ref ref) {
			this.ref = ref;
			this.aliasedField = null;
		}

		AliasRef(MemberAlias alias, MemberField aliasedField) {
			this(
					alias,
					new MemberRef(alias.getScope(), aliasedField));
		}

		AliasRef(MemberAlias alias, MemberRef aliasedField) {
			this.ref = aliasedField.getMemberKey()
					.toPath()
					.bind(alias.getDeclaration(), alias.getScope())
					.target(alias.distribute());
			this.aliasedField = aliasedField;
		}

		AliasRef(MemberAlias alias, AliasRef prototype) {

			final MemberRef aliasedField = prototype.getAliasedField();

			if (aliasedField == null) {
				this.aliasedField = null;
			} else {
				this.aliasedField = aliasedField.setOwner(alias.getScope());
			}

			this.ref = prototype.getRef().upgradeScope(alias.getScope());
		}

		public final Ref getRef() {
			return this.ref;
		}

		public final MemberRef getAliasedField() {
			return this.aliasedField;
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}
