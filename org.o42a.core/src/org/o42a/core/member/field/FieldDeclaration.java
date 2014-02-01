/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.core.member.field.FieldKey.fieldKey;

import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.decl.DeclaredField;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class FieldDeclaration extends Contained implements Cloneable {

	public static FieldDeclaration fieldDeclaration(
			LocationInfo location,
			Distributor distributor,
			MemberId memberId) {
		return new FieldDeclaration(location, distributor, memberId);
	}

	private final MemberId memberId;
	private FieldKey fieldKey;
	private Visibility visibility = Visibility.PUBLIC;
	private boolean macro;
	private boolean override;
	private boolean isAbstract;
	private boolean prototype;
	private StaticTypeRef declaredIn;

	FieldDeclaration(
			LocationInfo location,
			Distributor distributor,
			FieldDeclaration sample) {
		this(location, distributor, sample, sample.getMemberId());
		this.fieldKey = sample.getFieldKey();
	}

	private FieldDeclaration(
			LocationInfo location,
			Distributor distributor,
			MemberId memberId) {
		super(location, distributor);
		this.memberId = memberId;
	}

	private FieldDeclaration(
			LocationInfo location,
			Distributor distributor,
			FieldDeclaration sample,
			MemberId memberId) {
		super(location, distributor);
		this.memberId = memberId;
		this.visibility = sample.getVisibility();
		this.macro = sample.isMacro();
		this.override = sample.isOverride();
		this.isAbstract = sample.isAbstract();
		this.prototype = sample.isPrototype();
		this.declaredIn = sample.getDeclaredIn();
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final String getDisplayName() {
		return this.memberId.toString();
	}

	public FieldKey getFieldKey() {
		if (this.fieldKey != null) {
			return this.fieldKey;
		}
		return fieldKey(this);
	}

	public final StaticTypeRef getDeclaredIn() {
		return this.declaredIn;
	}

	public final FieldDeclaration setDeclaredIn(StaticTypeRef declaredIn) {

		final FieldDeclaration clone = clone();

		clone.declaredIn = declaredIn;

		return clone;
	}

	public final boolean isAdapter() {
		return this.memberId.getAdapterId() != null;
	}

	public final Visibility getVisibility() {
		return this.visibility;
	}

	public final FieldDeclaration setVisibility(Visibility visibility) {

		final FieldDeclaration clone = clone();

		clone.visibility = visibility;

		return clone;
	}

	public final boolean isMacro() {
		return this.macro;
	}

	public final FieldDeclaration macro() {

		final FieldDeclaration clone = clone();

		clone.macro = true;

		return clone;
	}

	public final boolean isOverride() {
		return this.override;
	}

	public final FieldDeclaration override() {

		final FieldDeclaration clone = clone();

		clone.override = true;

		return clone;
	}

	public final boolean isAbstract() {
		return this.isAbstract;
	}

	public final FieldDeclaration setAbstract() {

		final FieldDeclaration clone = clone();

		clone.isAbstract = true;

		return clone;
	}

	public final boolean isPrototype() {
		return this.prototype;
	}

	public final FieldDeclaration prototype() {

		final FieldDeclaration clone = clone();

		clone.prototype = true;

		return clone;
	}

	public FieldDeclaration inGroup(MemberId groupId) {
		return new FieldDeclaration(
				this,
				distribute(),
				this,
				groupId.append(getMemberId()));
	}

	public boolean validateVariantDeclaration(DeclaredField field) {

		final FieldDeclaration fieldDeclaration = field.getDeclaration();

		if (fieldDeclaration == this) {
			return true;
		}

		boolean ok = true;

		if (fieldDeclaration.isAdapter() != isAdapter()) {
			if (isAdapter()) {
				field.getLogger().unexpectedAdapter(getLocation());
			} else {
				field.getLogger().notAdapter(getLocation());
			}
			ok = false;
		}

		if (fieldDeclaration.getVisibility() != getVisibility()) {
			field.getLogger().unexpectedVisibility(
					getLocation(),
					getDisplayName(),
					getVisibility(),
					fieldDeclaration.getVisibility());
			ok = false;
		}

		if (fieldDeclaration.isPrototype() != isPrototype()) {
			if (isPrototype()) {
				field.getLogger().unexpectedPrototype(getLocation());
			} else {
				field.getLogger().notPrototype(getLocation());
			}
			ok = false;
		}

		if (fieldDeclaration.isAbstract() != isAbstract()) {
			if (isAbstract()) {
				field.getLogger().unexpectedAbstract(getLocation());
			} else {
				field.getLogger().notAbstract(getLocation());
			}
			ok = false;
		}

		if (field.isOverride() != isOverride()) {
			if (isOverride()) {
				field.getLogger().error(
						"cant_override_declared",
						this,
						"Can not override already declared field '%s'",
						getDisplayName());
			} else {
				field.getLogger().error(
						"cant_declare_overridden",
						this,
						"Can not declare already overridden field '%s'",
						getDisplayName());
			}
			ok = false;
		}

		return ok;
	}

	public FieldDeclaration reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new FieldDeclaration(
				reproducer.getContainer(),
				reproducer.distribute(),
				this,
				getMemberId().reproduceFrom(reproducer.getReproducingScope()));
	}

	@Override
	protected FieldDeclaration clone() {
		try {
			return (FieldDeclaration) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
