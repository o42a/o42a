/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.link.LinkUsage.*;

import org.o42a.analysis.use.Usable;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.link.LinkUsage;
import org.o42a.core.object.type.Sample;


public class LinkUses {

	private final ObjectType type;
	private final Usable<LinkUsage> uses;

	LinkUses(ObjectType type) {
		this.type = type;
		this.uses = usable(this);
	}

	public final Obj getObject() {
		return type().getObject();
	}

	public final ObjectType type() {
		return this.type;
	}

	@Override
	public String toString() {
		if (this.type == null) {
			return super.toString();
		}
		return "LinkUses[" + this.type + ']';
	}

	void useAsAncestor(Obj derived) {
		explicitlyDerivedBy(derived);
	}

	void useAsSample(Sample sample) {

		final Obj derived = sample.getDerivedObject();

		if (sample.isExplicit()) {
			explicitlyDerivedBy(derived);
			return;
		}

		deriveFieldChanges(derived);

		final Usable<LinkUsage> derivedUses = derived.type().linkUses().uses();

		uses().useBy(
				derivedUses.selectiveUser(LINK_FIELD_CHANGES),
				LINK_FIELD_CHANGES);

	}

	void fieldChanged(MemberField field) {
		if (field.field(dummyUser()).isScopeField()) {
			// Scope fields not considered.
			return;
		}

		final Obj linkTypeObject =
				getObject()
				.value()
				.getValueType()
				.typeObject(getObject().getContext().getIntrinsics());

		if (getObject() == linkTypeObject) {
			// Link type object.
			return;
		}
		if (field.isClone()
				&& field.getKey().getOrigin() == linkTypeObject.getScope()) {
			// Field is declared in link type object.
			return;
		}

		uses().useBy(
				field.field(dummyUser()).getArtifact().content(),
				LINK_FIELD_CHANGES);
	}

	private final Usable<LinkUsage> uses() {
		return this.uses;
	}

	private void explicitlyDerivedBy(Obj derived) {
		deriveFieldChanges(derived);
		uses().useBy(derived.content(), LINK_DERIVATION);
	}

	private void deriveFieldChanges(Obj derived) {
		derived.type().linkUses().uses().useBy(
				uses().selectiveUser(DERIVED_LINK_USE_SELECTOR),
				DERIVED_LINK_FIELD_CHANGES);
	}

}
