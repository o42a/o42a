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

import static org.o42a.core.member.field.DeclarationMode.DECLARE;
import static org.o42a.core.member.field.DeclarationMode.OVERRIDE;
import static org.o42a.core.member.field.DeclarationMode.OVERRIDE_OR_DECLARE;
import static org.o42a.core.member.field.PrototypeMode.AUTO_PROTOTYPE;
import static org.o42a.core.member.field.PrototypeMode.NOT_PROTOTYPE;
import static org.o42a.core.member.field.PrototypeMode.PROTOTYPE;
import static org.o42a.core.member.field.VisibilityMode.AUTO_VISIBILITY;

import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.Visibility;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class FieldDeclaration extends Contained implements Cloneable {

	private static final int STATIC_MASK = 0x01;
	private static final int ABSTRACT_MASK = 0x02;
	private static final int MACRO_MASK = 0x10;

	public static FieldDeclaration fieldDeclaration(
			LocationInfo location,
			Distributor distributor,
			MemberId memberId) {
		return new FieldDeclaration(location, distributor, memberId);
	}

	private final MemberId memberId;
	private FieldKey fieldKey;
	private DeclarationMode declarationMode = DECLARE;
	private VisibilityMode visibilityMode = AUTO_VISIBILITY;
	private PrototypeMode prototypeMode = NOT_PROTOTYPE;
	private StaticTypeRef declaredIn;
	private int mask;

	private FieldDeclaration(
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
		this.visibilityMode = sample.getVisibilityMode();
		this.prototypeMode = sample.getPrototypeMode();
		this.declaredIn = sample.getDeclaredIn();
		this.mask = sample.mask;
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final FieldDeclaration setMemberId(MemberId memberId) {
		return new FieldDeclaration(this, distribute(), this, memberId);
	}

	public final String getDisplayName() {
		return this.memberId.toString();
	}

	public final FieldKey getFieldKey() {
		if (this.fieldKey != null) {
			return this.fieldKey;
		}
		return this.fieldKey = getDeclarationMode().fieldKey(this);
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

	public final VisibilityMode getVisibilityMode() {
		return this.visibilityMode;
	}

	public final Visibility visibilityOf(Member member) {
		return getVisibilityMode().detectVisibility(member, this);
	}

	public final FieldDeclaration setVisibilityMode(
			VisibilityMode visibilityMode) {

		final FieldDeclaration clone = clone();

		clone.visibilityMode = visibilityMode;

		return clone;
	}

	public final boolean isMacro() {
		return hasMask(MACRO_MASK);
	}

	public final FieldDeclaration macro() {
		return setMask(MACRO_MASK);
	}

	public final PrototypeMode getPrototypeMode() {
		return this.prototypeMode;
	}

	public final FieldDeclaration setPrototypeMode(
			PrototypeMode prototypeMode) {

		final FieldDeclaration clone = clone();

		clone.prototypeMode = prototypeMode;

		return clone;
	}

	public final FieldDeclaration prototype() {
		return setPrototypeMode(PROTOTYPE);
	}

	public final FieldDeclaration autoPrototype() {
		return setPrototypeMode(AUTO_PROTOTYPE);
	}

	public final boolean isStatic() {
		return hasMask(STATIC_MASK);
	}

	public final FieldDeclaration makeStatic() {
		return setMask(STATIC_MASK);
	}

	public final DeclarationMode getDeclarationMode() {
		return this.declarationMode;
	}

	public final boolean isExplicitOverride() {
		return getDeclarationMode().isExplicitOverride(this);
	}

	public final FieldDeclaration override() {

		final FieldDeclaration clone = clone();

		clone.declarationMode = OVERRIDE;

		return clone;
	}

	public final FieldDeclaration overrideOrDeclare() {

		final FieldDeclaration clone = clone();

		clone.declarationMode = OVERRIDE_OR_DECLARE;

		return clone;
	}

	public final boolean isAbstract() {
		return hasMask(ABSTRACT_MASK);
	}

	public final FieldDeclaration makeAbstract() {
		return setMask(ABSTRACT_MASK);
	}

	public final FieldDeclaration override(
			LocationInfo location,
			Distributor distributor) {
		return new FieldDeclaration(location, distributor, this)
				.autoPrototype()
				.override();
	}

	public final FieldDeclaration inGroup(MemberId groupId) {
		return setMemberId(groupId.append(getMemberId()));
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

	private final boolean hasMask(int mask) {
		return (this.mask & mask) != 0;
	}

	private final FieldDeclaration setMask(int mask) {

		final int newMask = this.mask | mask;

		if (newMask == this.mask) {
			return this;
		}

		final FieldDeclaration clone = clone();

		clone.mask = newMask;

		return clone;
	}

}
