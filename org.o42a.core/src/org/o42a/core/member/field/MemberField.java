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
package org.o42a.core.member.field;

import static java.util.Collections.emptyList;
import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.FieldUsage.FIELD_ACCESS;

import java.util.ArrayList;
import java.util.List;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.source.LocationInfo;


public abstract class MemberField extends Member implements FieldReplacement {

	private final FieldDeclaration declaration;
	private Field field;

	private FieldAnalysis analysis;
	private ArrayList<FieldReplacement> allReplacements;

	public MemberField(Obj owner, FieldDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	protected MemberField(
			LocationInfo location,
			Obj owner,
			MemberField propagatedFrom) {
		super(
				location,
				propagatedFrom.distributeIn(owner),
				owner);
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

	public final FieldKey getFieldKey() {
		return getDeclaration().getFieldKey();
	}

	public final Nesting getNesting() {
		return getFieldKey();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public final Visibility getVisibility() {
		return getFieldKey().getVisibility();
	}

	public final boolean isAdapter() {
		return getDeclaration().isAdapter();
	}

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

	public final boolean isUpdated() {
		if (!isClone()) {
			return true;
		}
		if (this.field != null) {
			return this.field.isUpdated();
		}
		if (!getMemberOwner().meta().isUpdated()) {
			// Field can not be updated without owner to be updated also.
			return false;
		}

		// Only instantiated field can be updated.
		return this.field != null && this.field.objectAlreadyUpdated();
	}

	@Override
	public final MemberKey getMemberKey() {
		return getFieldKey().getMemberKey();
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
		if (isUpdated()) {
			return this.analysis = new FieldAnalysis(this);
		}

		final MemberField lastDefinition = getLastDefinition();

		return this.analysis = lastDefinition.getAnalysis();
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
	public final MemberTypeParameter toTypeParameter() {
		return null;
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
	public final Alias toAlias() {
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
	public abstract MemberField propagateTo(Obj owner);

	@Override
	public void resolveAll() {

		final Obj object = object(dummyUser());

		getAnalysis().registerObject(object);
		object.resolveAll();
		if (isOverride() && isUpdated()) {
			registerAsReplacement();
		}
	}

	protected final void setField(UserInfo user, Field field) {
		this.field = field;
		useBy(user);
	}

	protected abstract Field createField();

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
			this.allReplacements = new ArrayList<>();
		}
		this.allReplacements.add(replacement);
		if (!isUpdated()) {
			// Clone replaced by explicit field.
			// Register this clone as a replacement too.
			registerAsReplacement();
		}
	}

}
