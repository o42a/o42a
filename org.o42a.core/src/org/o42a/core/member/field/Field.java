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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.FieldKind.ORDINAL_FIELD;
import static org.o42a.core.ref.impl.prediction.FieldPrediction.predictField;

import org.o42a.codegen.Generator;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.object.ObjectFieldIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.decl.PropagatedFieldObject;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectScope;
import org.o42a.core.ref.Prediction;
import org.o42a.core.source.Location;
import org.o42a.util.string.ID;


public abstract class Field extends ObjectScope {

	private final MemberField member;
	private Field[] overridden;

	private FieldIR ir;

	public Field(MemberField member) {
		this.member = member;
	}

	@Override
	public final Location getLocation() {
		return this.member.getLocation();
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.member.getContainer();
	}

	@Override
	public final ID getId() {
		return this.member.getId();
	}

	public FieldKind getFieldKind() {
		return ORDINAL_FIELD;
	}

	public final boolean isPropagated() {
		return this.member.isPropagated();
	}

	public final MemberKey getKey() {
		return this.member.getMemberKey();
	}

	public final FieldAnalysis getAnalysis() {
		return toMember().getAnalysis();
	}

	/**
	 * The first field declaration.
	 *
	 * @return field, first declared by <code>:=</code>.
	 */
	public final Field getOriginal() {

		final MemberKey key = getKey();
		final Member member = key.getOrigin().getContainer().member(key);

		return member.toField().field(dummyUser());
	}

	/**
	 * The scope this field's definition were assigned in.
	 *
	 * @return the {@link #getLastDefinition() last definition} scope.
	 */
	public final Scope getDefinedIn() {
		return this.member.getDefinedIn();
	}

	@Override
	public final Field getFirstDeclaration() {
		return this.member.getFirstDeclaration().toField().field(dummyUser());
	}

	/**
	 * The last definition of this field.
	 *
	 * @return the last field's explicit definition or implicit definition
	 * with multiple inheritance.
	 */
	@Override
	public final Field getLastDefinition() {
		return this.member.getLastDefinition().toField().field(dummyUser());
	}

	/**
	 * Checks whether this field is a clone.
	 *
	 * <p>Field is clone if it is not explicitly defined and overrides
	 * only one field.</p>
	 *
	 * @return <code>true</code> if field is a clone or <code>false</code>
	 * otherwise.
	 *
	 * @see #getLastDefinition()
	 */
	@Override
	public final boolean isClone() {
		return toMember().isClone();
	}

	public Field[] getOverridden() {
		if (this.overridden == null) {
			this.overridden = overriddenFields();
		}
		return this.overridden;
	}

	@Override
	public final MemberField toMember() {
		return this.member;
	}

	@Override
	public final Field toField() {
		return this;
	}

	public String getDisplayName() {
		return this.member.getDisplayName();
	}

	public final FieldDeclaration getDeclaration() {
		return this.member.getDeclaration();
	}

	public final Visibility getVisibility() {
		return this.member.getVisibility();
	}

	public final boolean isAdapter() {
		return this.member.isAdapter();
	}

	public final boolean isPrototype() {
		return this.member.isPrototype();
	}

	public final boolean isStatic() {
		return this.member.isStatic();
	}

	public final boolean isAbstract() {
		return this.member.isAbstract();
	}

	public final boolean isOverride() {
		return this.member.isOverride();
	}

	public final boolean isUpdated() {
		if (!isClone()) {
			return true;
		}

		final Obj object = getScopeObject();

		if (object != null) {
			return object.meta().isUpdated();
		}

		final Obj owner = toMember().getMemberOwner();

		if (!owner.meta().isUpdated()) {
			// Field can not be updated without owner to be updated also.
			return false;
		}

		return objectAlreadyUpdated();
	}

	@Override
	public final Prediction predict(Prediction enclosing) {
		return predictField(enclosing, this);
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (is(other)) {
			return true;
		}

		final Obj object1 = toObject();

		if (object1 != null) {

			final Obj object2 = other.toObject();

			if (object2 != null) {
				return object1.type().derivedFrom(object2.type());
			}
		}

		final Field field2 = other.toField();

		if (field2 == null) {
			return false;
		}

		return getKey().equals(field2.getKey());
	}

	@Override
	public final FieldIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = createIR(generator);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return this.member.toString();
	}

	@Override
	protected abstract Obj createObject();

	protected final Obj propagateObject() {
		if (isStatic()) {
			return getOverridden()[0].toObject();
		}
		return new PropagatedFieldObject(this);
	}

	protected FieldIR createIR(Generator generator) {
		return new ObjectFieldIR(generator, this);
	}

	final boolean objectAlreadyUpdated() {

		// Only instantiated object can be updated.
		final Obj object = getScopeObject();

		return object != null && object.meta().isUpdated();
	}

	private Field[] overriddenFields() {

		final Member[] overriddenMembers = this.member.getOverridden();
		final Field[] overridden = new Field[overriddenMembers.length];

		for (int i = 0; i < overridden.length; ++i) {
			overridden[i] =
					overriddenMembers[i]
					.toField()
					.field(dummyUser());
		}

		return overridden;
	}

}
