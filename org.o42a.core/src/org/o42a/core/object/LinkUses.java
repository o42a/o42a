/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
import static org.o42a.core.value.link.LinkUsage.*;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.Usable;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.RefUser;
import org.o42a.core.value.link.LinkUsage;


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

	public final boolean simplifiedLink(Analyzer analyzer) {
		return !uses().isUsed(analyzer, ALL_LINK_USAGES);
	}

	public void useBodyBy(RefUser user) {
		uses().useBy(user, COMPLEX_LINK_TARGET);
	}

	@Override
	public String toString() {
		if (this.type == null) {
			return super.toString();
		}
		return "LinkUses[" + this.type + ']';
	}

	void determineTargetComplexity() {
		if (!getObject().value().getDefinitions().target().exists()) {
			uses().useBy(getObject().content(), COMPLEX_LINK_TARGET);
		}
	}

	void useAsAncestor(Obj derived) {
		explicitlyDerivedBy(derived);
	}

	void useAsSample(Sample sample) {

		final Obj derived = sample.getDerivedObject();

		deriveComplexity(derived);

		final Usable<LinkUsage> derivedUses = derived.type().linkUses().uses();

		uses().useBy(
				derivedUses.selectiveUser(LINK_FIELD_CHANGES),
				LINK_FIELD_CHANGES);
		uses().useBy(
				derivedUses.selectiveUser(COMPLEX_LINK_TARGET),
				COMPLEX_LINK_TARGET);
	}

	void fieldChanged(MemberField field) {
		if (field.field(dummyUser()).isScopeField()) {
			// Scope fields not considered.
			return;
		}

		final Obj linkTypeObject =
				getObject()
				.type()
				.getValueType()
				.typeObject(getObject().getContext().getIntrinsics());

		if (getObject().is(linkTypeObject)) {
			// Link type object.
			return;
		}
		if (!field.isUpdated()
				&& field.getMemberKey().getOrigin().is(
						linkTypeObject.getScope())) {
			// Field is declared in link type object.
			return;
		}

		uses().useBy(field.object(dummyUser()).content(), LINK_FIELD_CHANGES);
	}

	final void depAdded() {
		uses().useBy(getObject().content(), LINK_FIELD_CHANGES);
	}

	final void keeperAdded() {
		uses().useBy(getObject().content(), LINK_FIELD_CHANGES);
	}

	private final Usable<LinkUsage> uses() {
		return this.uses;
	}

	private void explicitlyDerivedBy(Obj derived) {
		deriveComplexity(derived);
		uses().useBy(derived.content(), LINK_DERIVATION);
	}

	private void deriveComplexity(Obj derived) {
		derived.type().linkUses().uses().useBy(
				uses().selectiveUser(LINK_COMPLEXITY_SELECTOR),
				DERIVED_LINK_COMPLEXITY);
	}

}
