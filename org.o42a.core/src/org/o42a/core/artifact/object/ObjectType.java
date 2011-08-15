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
import static org.o42a.core.artifact.object.ObjectResolution.NOT_RESOLVED;
import static org.o42a.util.use.Usable.simpleUsable;
import static org.o42a.util.use.User.dummyUser;

import java.util.HashMap;
import java.util.Map;

import org.o42a.core.Scope;
import org.o42a.core.def.DefKind;
import org.o42a.core.member.Member;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.use.*;


public final class ObjectType implements UserInfo {

	private final Obj object;
	private Obj lastDefinition;
	private Usable uses;
	private Usable runtimeConstructionUses;
	private Usable derivationUses;
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
	public final User toUser() {
		return uses().toUser();
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		if (this.uses == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.uses.getUseBy(useCase);
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
			uses().useBy(user);
		}
		return this;
	}

	public final ObjectType runtimeConstructBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			runtimeConstructionUses().useBy(user);
		}
		return this;
	}

	public final UserInfo runtimeConstruction() {
		return runtimeConstructionUses();
	}

	public final UserInfo derivation() {
		return derivationUses();
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
		runtimeConstructBy(type.runtimeConstructionUses());
	}

	public void resolveAll() {
		detectRuntimeConstruction();
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
		derivationUses().useBy(derived.content());
		trackAscendantDefsUsage(derived);
	}

	protected void useAsSample(Sample sample) {

		final Obj sampleObject =
				sample.getAscendants().getObject();

		derivationUses().useBy(sampleObject.content());
		trackAscendantDefsUsage(sampleObject);
		trackAncestorDefsUpdates(sampleObject);
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

	private final Usable uses() {
		if (this.uses != null) {
			return this.uses;
		}

		this.uses = simpleUsable(this);
		getObject().content().useBy(this.uses);

		return this.uses;
	}

	private final Usable runtimeConstructionUses() {
		if (this.runtimeConstructionUses != null) {
			return this.runtimeConstructionUses;
		}

		this.runtimeConstructionUses =
				simpleUsable("RuntimeConstructionOf", getObject());
		derivationUses().useBy(this.runtimeConstructionUses);

		return this.runtimeConstructionUses;
	}

	private final Usable derivationUses() {
		if (this.derivationUses != null) {
			return this.derivationUses;
		}
		return this.derivationUses = simpleUsable("DerivationOf", getObject());
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

	private void detectRuntimeConstruction() {

		final Obj object = getObject();
		final Member member = object.toMember();

		if (member != null) {
			// Detect run time construction mode by member.
			runtimeConstructBy(member.getAnalysis().runtimeConstruction());
			return;
		}

		// Stand-alone object is always constructed at run time.
		runtimeConstructBy(object.content());

		final Obj cloneOf = object.getCloneOf();

		if (cloneOf != null) {
			cloneOf.type().runtimeConstructBy(runtimeConstructionUses());
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
