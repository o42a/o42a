/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;
import static org.o42a.core.object.value.ValueUsage.EXPLICIT_VALUE_USAGE;
import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.use.Usable;
import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.type.DerivationUsage;
import org.o42a.core.object.type.Inheritor;
import org.o42a.core.object.type.Sample;
import org.o42a.core.object.value.ObjectValueDefs;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.Init;


final class DerivationUses implements UserInfo {

	private final ObjectType type;
	private final Init<Usable<DerivationUsage>> uses = init(this::createUses);

	DerivationUses(ObjectType type) {
		this.type = type;
	}

	public final Obj getObject() {
		return this.type.getObject();
	}

	public final void useBy(UserInfo user) {
		uses().useBy(user, DERIVATION_USAGE);
	}

	@Override
	public final User<DerivationUsage> toUser() {
		return uses().toUser();
	}

	public final void wrapBy(DerivationUses other) {
		uses().useBy(other, DERIVATION_USAGE);
	}

	public void resolveAll() {
		registerInAncestor();
		registerSamples();
		ensureEagerConstructed();
	}

	@Override
	public final String toString() {
		if (this.type == null) {
			return super.toString();
		}
		return "DerivationUses[" + getObject() + ']';
	}

	void useAsAncestor(Obj derived) {
		getObject().type().registerDerivative(new Inheritor(derived));
		uses().useBy(derived.content(), DERIVATION_USAGE);
		uses().useBy(derived.type().derivation(), DERIVATION_USAGE);
		trackUpdatesByAncestor(derived);
	}

	void useAsSample(Sample sample) {

		final Obj derived = sample.getDerivedObject();

		getObject().type().registerDerivative(sample);
		uses().useBy(derived.content(), DERIVATION_USAGE);
		uses().useBy(derived.type().derivation(), DERIVATION_USAGE);

		trackUpdatesBySample(sample);
		trackSampleDerivation(sample);
	}

	private Obj getOwner() {

		final Scope enclosingScope =
				getObject().getScope().getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			return enclosingObject;
		}

		return enclosingScope.toMember().getMemberOwner();
	}

	private final Usable<DerivationUsage> uses() {
		return this.uses.get();
	}

	private Usable<DerivationUsage> createUses() {

		final Obj object = getObject();

		if (!object.meta().isUpdated()) {
			return object.getCloneOf().type().derivationUses().uses();
		}

		final Usable<DerivationUsage> uses =
				DerivationUsage.usable("DerivationOf", object);
		final Member member = object.toMember();

		if (member != null) {
			detectFieldDerivation(uses, member);
		} else {
			detectStandaloneObjectDerivation(uses);
		}

		return uses;
	}

	private void detectFieldDerivation(
			Usable<DerivationUsage> uses,
			Member member) {
		// Detect derivation mode by member.
		final MemberField field = member.toField();

		if (field == null) {
			return;
		}

		uses.useBy(field.getAnalysis().derivation(), DERIVATION_USAGE);
	}

	private void detectStandaloneObjectDerivation(
			Usable<DerivationUsage> uses) {
		if (getObject().getScope().getEnclosingScope().isTopScope()) {
			return;
		}
		// Stand-alone object is derived if its owner's value is ever used.
		uses.useBy(getOwner().value().uses(), DERIVATION_USAGE);
	}

	private void trackUpdatesByAncestor(Obj derived) {
		if (!derived.meta().isUpdated()) {
			return;
		}

		trackAscendantDefsUsage(derived);

		final LinkUses linkUses = this.type.linkUses();

		if (linkUses != null) {
			linkUses.useAsAncestor(derived);
		}
		if (derived.getWrapped() == null) {
			this.type.registerUpdatedDerivative(new Inheritor(derived));
		}
	}

	private void trackUpdatesBySample(Sample sample) {

		final Obj derived = sample.getDerivedObject();

		if (!derived.meta().isUpdated()) {
			return;
		}

		trackAscendantDefsUsage(derived);
		trackAncestorDefsUpdates(derived);

		final LinkUses linkUses = this.type.linkUses();

		if (linkUses != null) {
			linkUses.useAsSample(sample);
		}
		if (derived.getWrapped() == null) {
			this.type.registerUpdatedDerivative(sample);
		}
	}

	private void trackSampleDerivation(Sample sample) {
		// Run time derivation of sample means that
		// the owner object's value can be constructed at run time.
		final Obj derived = sample.getDerivedObject();
		final Obj owner = sample.getScope().toObject();

		owner.value().uses().useBy(
				derived.type().derivation(),
				EXPLICIT_VALUE_USAGE);
	}

	private void trackAscendantDefsUsage(Obj derived) {

		final Obj ancestor = getObject();
		final ObjectValue ascendantValue = getObject().value();
		final ObjectValue derivedValue = derived.value();

		trackAscendantPartUsage(
				ancestor,
				ascendantValue,
				derivedValue);
	}

	private void trackAscendantPartUsage(
			final Obj ancestor,
			final ObjectValue ascendantValue,
			final ObjectValue derivedValue) {

		final ObjectValueDefs derivedPart = derivedValue.valueDefs();

		if (derivedPart.getDefs().presentIn(ancestor)) {
			ascendantValue.valueDefs().accessBy(derivedPart);
		}
	}

	private void trackAncestorDefsUpdates(Obj since) {

		final TypeRef newAncestor = since.type().getAncestor();

		if (newAncestor == null) {
			return;
		}

		final TypeRef oldAncestor = this.type.getAncestor();

		if (oldAncestor == null) {
			return;
		}

		final ObjectValue newAncestorValue = newAncestor.getType().value();
		final Obj oldAncestorObject = oldAncestor.getType();

		trackAncestorPartUpdates(
				since,
				newAncestorValue,
				oldAncestorObject);
	}

	private void trackAncestorPartUpdates(
			final Obj since,
			final ObjectValue newAncestorValue,
			final Obj oldAncestorObject) {

		final ObjectValueDefs sampleValuePart =
				getObject().value().valueDefs();
		final ObjectValueDefs sinceValuePart =
				since.value().valueDefs();

		sampleValuePart.updateAncestorDefsBy(
				sinceValuePart.ancestorDefsUpdatesUser());
		if (newAncestorValue.valueDefs().getDefs().updatedSince(
				oldAncestorObject)) {
			sampleValuePart.updateAncestorDefsBy(sinceValuePart);
		}
	}

	private void registerInAncestor() {

		final TypeRef ancestor = this.type.getAncestor();

		if (ancestor == null || !ancestor.isValid()) {
			return;
		}
		ancestor.getType().type().derivationUses().useAsAncestor(getObject());
	}

	private void registerSamples() {

		final Sample sample = this.type.getSample();

		if (sample == null) {
			return;
		}

		final TypeRef sampleTypeRef = sample.getTypeRef();

		if (!sampleTypeRef.isValid()) {
			return;
		}

		sampleTypeRef.getType().type().derivationUses().useAsSample(sample);
	}

	private void ensureEagerConstructed() {
		if (getObject().type().isRuntimeEager()) {
			uses().useBy(getObject().value().toUser(), DERIVATION_USAGE);
		}
	}

}
