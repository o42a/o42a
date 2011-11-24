/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import static java.util.Collections.unmodifiableMap;
import static org.o42a.core.artifact.object.DerivationUsage.RUNTIME_DERIVATION_USAGE;
import static org.o42a.core.artifact.object.DerivationUsage.STATIC_DERIVATION_USAGE;
import static org.o42a.core.artifact.object.ObjectResolution.NOT_RESOLVED;
import static org.o42a.core.artifact.object.TypeUsage.RUNTIME_TYPE_USAGE;
import static org.o42a.core.artifact.object.TypeUsage.STATIC_TYPE_USAGE;
import static org.o42a.util.use.User.dummyUser;

import java.util.HashMap;
import java.util.Map;

import org.o42a.core.Scope;
import org.o42a.core.def.DefKind;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.*;


public final class ObjectType implements UserInfo, Uses<TypeUsage> {

	private final Obj object;
	private Obj lastDefinition;
	private Usable<TypeUsage> uses;
	private Usable<DerivationUsage> derivationUses;
	private ObjectResolution resolution = NOT_RESOLVED;
	private Ascendants ascendants;
	private Map<Scope, Derivation> allAscendants;

	ObjectType(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Obj getLastDefinition() {
		if (this.lastDefinition != null) {
			return this.lastDefinition;
		}

		final Obj object = getObject();
		final Obj cloneOf = object.getCloneOf();

		if (cloneOf != null) {
			return this.lastDefinition = cloneOf;
		}

		return this.lastDefinition = object;
	}

	@Override
	public final User<TypeUsage> toUser() {
		return uses().toUser();
	}

	@Override
	public final AllUsages<TypeUsage> allUsages() {
		return TypeUsage.ALL_TYPE_USAGES;
	}

	@Override
	public final UseFlag selectUse(
			UseCaseInfo useCase,
			UseSelector<TypeUsage> selector) {
		if (this.uses == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.uses.selectUse(useCase, selector);
	}

	@Override
	public final boolean isUsed(
			UseCaseInfo useCase,
			UseSelector<TypeUsage> selector) {
		return selectUse(useCase, selector).isUsed();
	}

	public final Ascendants getAscendants() {
		resolve(false);
		return this.ascendants;
	}

	public TypeRef getAncestor() {
		return getAscendants().getAncestor();
	}

	/**
	 * Object samples in descending precedence order.
	 *
	 * <p>This is an order reverse to their appearance in source code.</p>
	 *
	 * @return array of object samples.
	 */
	public final Sample[] getSamples() {
		return getAscendants().getSamples();
	}

	public boolean inherits(ObjectType other) {
		if (getObject() == other.getObject()) {
			return true;
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor == null) {
			return false;
		}

		return ancestor.type(dummyUser()).inherits(other);
	}

	public final Map<Scope, Derivation> allAscendants() {
		if (this.allAscendants != null) {
			return this.allAscendants;
		}
		return this.allAscendants = unmodifiableMap(buildAllAscendants());
	}

	public final ObjectType useBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			uses().useBy(
					user,
					getObject().isClone()
					? RUNTIME_TYPE_USAGE : STATIC_TYPE_USAGE);
		}
		return this;
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
	}

	public final User<DerivationUsage> rtDerivation() {
		return derivationUses().usageUser(RUNTIME_DERIVATION_USAGE);
	}

	public final boolean derivedFrom(ObjectType other) {
		return allAscendants().containsKey(other.getObject().getScope());
	}

	public final boolean derivedFrom(ObjectType other, Derivation derivation) {

		final Derivation derivations =
				allAscendants().get(other.getObject().getScope());

		return derivations != null && derivations.is(derivation);
	}

	public final void wrapBy(ObjectType type) {
		useBy(type);
		derivationUses().useBy(type.rtDerivation(), RUNTIME_DERIVATION_USAGE);
	}

	public void resolveAll() {
		getAscendants().resolveAll();
		registerInAncestor();
		registerSamples();
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectType[" + this.object + ']';
	}

	protected void useAsAncestor(Obj derived) {
		derivationUses().useBy(
				derived.content(),
				getObject().isClone()
				? RUNTIME_DERIVATION_USAGE : STATIC_DERIVATION_USAGE);
		derivationUses().useBy(
				derived.type().rtDerivation(),
				RUNTIME_DERIVATION_USAGE);
		if (!derived.isClone()) {
			trackAscendantDefsUsage(derived);
		}
	}

	protected void useAsSample(Sample sample) {

		final Obj sampleObject =
				sample.getAscendants().getObject();

		derivationUses().useBy(
				sampleObject.content(),
				getObject().isClone()
				? RUNTIME_DERIVATION_USAGE : STATIC_DERIVATION_USAGE);
		derivationUses().useBy(
				sampleObject.type().rtDerivation(),
				RUNTIME_DERIVATION_USAGE);
		if (!sampleObject.isClone()) {
			trackAscendantDefsUsage(sampleObject);
			trackAncestorDefsUpdates(sampleObject);
		}
	}

	final ObjectResolution getResolution() {
		return this.resolution;
	}

	final void setResolution(ObjectResolution resolution) {
		this.resolution = resolution;
	}

	final boolean resolve(boolean skipIfResolving) {
		if (this.resolution == ObjectResolution.NOT_RESOLVED) {
			try {
				this.resolution = ObjectResolution.RESOLVING_TYPE;
				this.ascendants = getObject().buildAscendants();
			} finally {
				this.resolution = ObjectResolution.NOT_RESOLVED;
			}
			this.resolution = ObjectResolution.TYPE_RESOLVED;
			this.ascendants.validate();
			getObject().postResolve();
			this.resolution = ObjectResolution.POST_RESOLVED;
		} else if (this.resolution == ObjectResolution.RESOLVING_TYPE) {
			if (!skipIfResolving) {
				getObject().getLogger().recursiveResolution(
						getObject(),
						getObject());
			}
			return false;
		}

		return this.resolution.resolved();
	}

	private final Usable<TypeUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			this.uses = cloneOf.type().uses();
		} else {
			this.uses = TypeUsage.usable(this);
		}
		getObject().content().useBy(this.uses);

		return this.uses;
	}

	private final Usable<DerivationUsage> derivationUses() {
		if (this.derivationUses != null) {
			return this.derivationUses;
		}

		final Obj object = getObject();
		final Obj cloneOf = object.getCloneOf();

		if (cloneOf != null) {
			this.derivationUses = cloneOf.type().derivationUses();
		} else {
			this.derivationUses =
					DerivationUsage.usable("DerivationOf", getObject());
		}

		final Member member = object.toMember();

		if (member != null) {
			// Detect run time construction mode by member.
			final MemberField field = member.toField();

			if (field != null) {
				this.derivationUses.useBy(
						field.getAnalysis().rtDerivation(),
						RUNTIME_DERIVATION_USAGE);
			}
		} else {

			final Obj enclosingObject;
			final Scope enclosingScope =
					object.getScope().getEnclosingScope();
			final LocalScope enclosingLocal = enclosingScope.toLocal();

			if (enclosingLocal != null) {
				enclosingObject = enclosingLocal.getOwner();
			} else {
				enclosingObject = enclosingScope.toObject();
			}

			if (enclosingObject != null) {
				// Stand-alone object is constructed at run time,
				// if enclosing object is ever derived.
				this.derivationUses.useBy(
						enclosingObject.type().derivation(),
						RUNTIME_DERIVATION_USAGE);
			} else {
				assert enclosingScope.isTopScope() :
					"No enclosing object of non-top-level object " + object;
			}
			if (getObject().getConstructionMode().isRuntime()) {
				this.derivationUses.useBy(
						getObject().content(),
						RUNTIME_DERIVATION_USAGE);
			}
		}

		return this.derivationUses;
	}

	private HashMap<Scope, Derivation> buildAllAscendants() {

		final HashMap<Scope, Derivation> allAscendants =
				new HashMap<Scope, Derivation>();

		allAscendants.put(getObject().getScope(), Derivation.SAME);

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {

			final ObjectType type = ancestor.type(this);

			for (Scope scope : type.allAscendants().keySet()) {
				allAscendants.put(scope, Derivation.INHERITANCE);
			}
		}

		addSamplesAscendants(allAscendants, getSamples());
		addSamplesAscendants(
				allAscendants,
				getAscendants().getDiscardedSamples());

		return allAscendants;
	}

	private void addSamplesAscendants(
			HashMap<Scope, Derivation> allAscendants,
			Sample[] samples) {
		for (Sample sample : samples) {

			final ObjectType type = sample.type(this);

			for (Map.Entry<Scope, Derivation> e
					: type.allAscendants().entrySet()) {

				final Scope scope = e.getKey();
				final Derivation traversed =
						e.getValue().traverseSample(sample);
				final Derivation derivations = allAscendants.get(scope);

				if (derivations == null) {
					allAscendants.put(scope, traversed);
					continue;
				}
				allAscendants.put(scope, derivations.union(traversed));
			}
		}
	}

	private void registerInAncestor() {

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {

			final ObjectType ancestorType = ancestor.type(dummyUser());

			if (ancestorType != null) {
				ancestorType.useAsAncestor(getObject());
			}
		}
	}

	private void registerSamples() {
		for (Sample sample : getSamples()) {

			final ObjectType sampleType =
					sample.getTypeRef().type(dummyUser());

			if (sampleType != null) {
				sampleType.useAsSample(sample);
			}
		}
	}

	private void trackAscendantDefsUsage(Obj derived) {

		final Obj ancestor = getObject();
		final ObjectValue ascendantValue = getObject().value();
		final ObjectValue derivedValue = derived.value();

		for (DefKind defKind : DefKind.values()) {

			final ObjectValuePart<?, ?> derivedPart =
					derivedValue.part(defKind);

			if (derivedPart.getDefs().presentIn(ancestor)) {
				ascendantValue.part(defKind).accessBy(derivedPart);
			}
		}
	}

	private void trackAncestorDefsUpdates(Obj since) {

		final TypeRef newAncestor = since.type().getAncestor();

		if (newAncestor == null) {
			return;
		}

		final TypeRef oldAncestor = getAncestor();

		if (oldAncestor == null) {
			return;
		}

		final ObjectValue newAncestorValue =
				newAncestor.typeObject(dummyUser()).value();
		final Obj oldAncestorObject =
				oldAncestor.typeObject(dummyUser());

		for (DefKind defKind : DefKind.values()) {

			final ObjectValuePart<?, ?> sampleValuePart =
					getObject().value().part(defKind);
			final ObjectValuePart<?, ?> sinceValuePart =
					since.value().part(defKind);

			sampleValuePart.updateAncestorDefsBy(
					sinceValuePart.ancestorDefsUpdatedBy());
			if (newAncestorValue.part(defKind).getDefs().updatedSince(
					oldAncestorObject)) {
				sampleValuePart.updateAncestorDefsBy(sinceValuePart);
			}
		}
	}

}
