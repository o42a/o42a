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
package org.o42a.core.member.field;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Placed;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.Visibility;
import org.o42a.core.st.Reproducer;


public class FieldDeclaration extends Placed implements Cloneable {

	public static FieldDeclaration fieldDeclaration(
			LocationSpec location,
			Distributor distributor,
			MemberId memberId) {
		return new FieldDeclaration(location, distributor, memberId);
	}

	private final MemberId memberId;

	private Visibility visibility = Visibility.PUBLIC;
	private boolean prototype;
	private boolean isAbstract;
	private boolean override;
	private boolean link;
	private boolean variable;
	private StaticTypeRef declaredIn;
	private TypeRef type;

	public FieldDeclaration(
			LocationSpec location,
			Distributor distributor,
			FieldDeclaration sample) {
		this(location, distributor, sample, sample.getMemberId());
	}

	private FieldDeclaration(
			LocationSpec location,
			Distributor distributor,
			MemberId memberId) {
		super(location, distributor);
		this.memberId = memberId;
	}

	private FieldDeclaration(
			LocationSpec location,
			Distributor distributor,
			FieldDeclaration sample,
			MemberId memberId) {
		super(location, distributor);
		this.memberId = memberId;
		this.visibility = sample.visibility;
		this.link = sample.link;
		this.override = sample.override;
		this.variable = sample.variable;
		this.isAbstract = sample.isAbstract();
		this.prototype = sample.isPrototype();
		this.type = sample.type;
		this.declaredIn = sample.declaredIn;
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final String getDisplayName() {
		return this.memberId.toString();
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

	public final boolean isOverride() {
		return this.override;
	}

	public final FieldDeclaration override() {

		final FieldDeclaration clone = clone();

		clone.override = true;

		return clone;
	}

	public final boolean isLink() {
		return this.link;
	}

	public final FieldDeclaration link() {

		final FieldDeclaration clone = clone();

		clone.link = true;
		clone.variable = false;

		return clone;
	}

	public final boolean isVariable() {
		return this.variable;
	}

	public final FieldDeclaration variable() {

		final FieldDeclaration clone = clone();

		clone.link = false;
		clone.variable = true;

		return clone;
	}

	public final TypeRef getType() {
		return this.type;
	}

	public final FieldDeclaration setType(TypeRef type) {

		final FieldDeclaration clone = clone();

		clone.type = type;

		return clone;
	}

	public FieldDeclaration inGroup(MemberId groupId) {
		return new FieldDeclaration(
				this,
				distribute(),
				this,
				groupId.append(getMemberId()));
	}

	public TypeRef type(FieldDefinition definition) {
		if (this.type != null) {
			return this.type;
		}
		if (!isLink() && !isVariable()) {
			return null;
		}

		final AscendantsDefinition ascendants = definition.getAscendants();

		if (ascendants != null && ascendants.getAncestor() != null) {
			return this.type = ascendants.getAncestor();
		}

		return this.type =
			definition.getValue().ancestor(definition.getValue());
	}

	public boolean validateVariantDeclaration(DeclaredField<?> field) {

		final FieldDeclaration fieldDeclaration = field.getDeclaration();

		if (fieldDeclaration == this) {
			return true;
		}

		boolean ok = true;

		if (fieldDeclaration.isAdapter() != isAdapter()) {
			if (isAdapter()) {
				field.getLogger().unexpectedAdapter(this);
			} else {
				field.getLogger().notAdapter(this);
			}
			ok = false;
		}

		if (fieldDeclaration.getVisibility() != getVisibility()) {
			field.getLogger().unexpectedVisibility(
					this,
					getDisplayName(),
					getVisibility(),
					fieldDeclaration.getVisibility());
			ok = false;
		}

		if (fieldDeclaration.isPrototype() != isPrototype()) {
			if (isPrototype()) {
				field.getLogger().unexpectedPrototype(this);
			} else {
				field.getLogger().notPrototype(this);
			}
			ok = false;
		}

		if (fieldDeclaration.isAbstract() != isAbstract()) {
			if (isAbstract()) {
				field.getLogger().unexpectedAbstract(this);
			} else {
				field.getLogger().notAbstract(this);
			}
			ok = false;
		}

		if (field.isOverride() != isOverride()) {
			if (isOverride()) {
				field.getLogger().cantOverrideDeclared(this, getDisplayName());
			} else {
				field.getLogger().cantDeclareOverridden(this, getDisplayName());
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
