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
package org.o42a.core.member.field;

import static java.util.Collections.emptyList;
import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.MemberKey.brokenMemberKey;
import static org.o42a.core.member.field.FieldUsage.FIELD_ACCESS;

import java.util.ArrayList;
import java.util.List;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public abstract class MemberField extends Member implements FieldReplacement {

	private final FieldDeclaration declaration;
	private Field field;
	private MemberKey key;
	private Visibility visibility;

	private FieldAnalysis analysis;
	private ArrayList<FieldReplacement> allReplacements;

	public MemberField(MemberOwner owner, FieldDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	protected MemberField(
			LocationInfo location,
			MemberOwner owner,
			MemberField propagatedFrom) {
		super(
				location,
				propagatedFrom.distributeIn(owner.getContainer()), owner);
		this.key = propagatedFrom.getMemberKey();
		this.visibility = propagatedFrom.getVisibility();
		this.declaration =
				new FieldDeclaration(
						propagatedFrom,
						distribute(),
						propagatedFrom.getDeclaration())
				.override();
	}

	@Override
	public final MemberId getMemberId() {
		return getDeclaration().getMemberId();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public final Visibility getVisibility() {
		getMemberKey();
		return this.visibility;
	}

	public final boolean isAdapter() {
		return getDeclaration().isAdapter();
	}

	@Override
	public final boolean isAbstract() {
		return getDeclaration().isAbstract();
	}

	public final boolean isPrototype() {
		return getDeclaration().isPrototype();
	}

	@Override
	public final boolean isOverride() {
		return getDeclaration().isOverride();
	}

	@Override
	public MemberKey getMemberKey() {
		if (this.key != null) {
			return this.key;
		}
		if (getDeclaration().isOverride()) {
			return this.key = overrideField();
		}
		return this.key = declareNewField();
	}

	@Override
	public final MemberField getFirstDeclaration() {
		return super.getFirstDeclaration().toField();
	}

	@Override
	public final MemberField getLastDefinition() {
		return super.getLastDefinition().toField();
	}

	public final FieldAnalysis getAnalysis() {
		if (this.analysis != null) {
			return this.analysis;
		}

		final MemberField lastDefinition = getLastDefinition();

		if (lastDefinition != this) {
			return this.analysis = lastDefinition.getAnalysis();
		}

		return this.analysis = new FieldAnalysis(this);
	}

	public final Field field(UserInfo user) {
		if (this.field != null) {
			useBy(user);
			return this.field;
		}

		setField(user, createField());

		return this.field;
	}

	public final Obj object(UserInfo user) {
		return field(user).toObject();
	}

	@Override
	public final MemberField toField() {
		return this;
	}

	@Override
	public final MemberClause toClause() {
		return null;
	}

	@Override
	public final MemberLocal toLocal() {
		return null;
	}

	@Override
	public final Obj substance(UserInfo user) {
		return field(user).toObject();
	}

	@Override
	public abstract MemberField getPropagatedFrom();

	public final List<FieldReplacement> allReplacements() {
		if (this.allReplacements == null) {
			return emptyList();
		}
		return this.allReplacements;
	}

	@Override
	public abstract MemberField propagateTo(MemberOwner owner);

	@Override
	public void resolveAll() {

		final Obj object = object(dummyUser());

		getAnalysis().registerObject(object);
		object.resolveAll();
		if (isOverride() && !isClone()) {
			registerAsReplacement();
		}
	}

	protected final void setField(UserInfo user, Field field) {
		this.field = field;
		useBy(user);
	}

	protected abstract Field createField();

	private MemberKey overrideField() {

		final Member overridden = overridden();

		if (overridden != null) {
			this.visibility = overridden.getVisibility();
			return overridden.getMemberKey();
		}

		this.visibility = Visibility.PRIVATE;

		return brokenMemberKey();
	}

	private Member overridden() {

		Member overridden = null;
		final StaticTypeRef declaredInRef = getDeclaration().getDeclaredIn();

		if (declaredInRef != null) {
			if (!declaredInRef.isValid()) {
				return null;
			}
			overridden = declaredInRef.getType().member(
					getMemberId(),
					Accessor.INHERITANT);
		} else {

			final ObjectType containerType = getContainer().toObject().type();

			for (Sample sample : containerType.getSamples()) {
				overridden = overridden(
						overridden,
						sample.typeObject(dummyUser()));
			}

			final TypeRef ancestor = containerType.getAncestor();

			if (ancestor != null) {
				overridden = overridden(overridden, ancestor.getType());
			}
		}

		if (overridden == null) {
			getLogger().error(
					"cant_override_unknown",
					this,
					"Can not override unknown field '%s'",
					getDisplayName());
		}

		return overridden;
	}

	private Member overridden(Member overridden, Obj ascendant) {
		if (ascendant == null) {
			return overridden;
		}

		final Member member = ascendant.member(getMemberId(), Accessor.INHERITANT);

		if (member == null) {
			return overridden;
		}
		if (overridden == null) {
			return member;
		}
		if (overridden.definedAfter(member)) {
			return overridden;
		}
		if (member.definedAfter(overridden)) {
			return member;
		}

		return overridden;
	}

	private MemberKey declareNewField() {
		this.visibility = getDeclaration().getVisibility();
		return getMemberId().key(getScope());
	}

	private void useBy(UserInfo user) {
		if (user.toUser().isDummy()) {
			return;
		}
		getAnalysis().uses().useBy(user.toUser(), FIELD_ACCESS);
	}

	private void registerAsReplacement() {
		for (Member overridden : getOverridden()) {
			overridden.toField().registerReplacement(this);
		}
	}

	private void registerReplacement(FieldReplacement replacement) {
		if (this.allReplacements == null) {
			this.allReplacements = new ArrayList<FieldReplacement>();
		}
		this.allReplacements.add(replacement);
		if (isClone()) {
			// Clone replaced by explicit field.
			// Register this clone as a replacement too.
			registerAsReplacement();
		}
	}

}
