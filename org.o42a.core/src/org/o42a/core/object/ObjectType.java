/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static org.o42a.core.object.impl.ObjectResolution.NOT_RESOLVED;
import static org.o42a.core.object.type.DerivationUsage.RUNTIME_DERIVATION_USAGE;
import static org.o42a.core.object.type.DerivationUsage.STATIC_DERIVATION_USAGE;
import static org.o42a.core.object.value.ValueUsage.EXPLICIT_RUNTIME_VALUE_USAGE;
import static org.o42a.core.value.TypeParameters.typeParameters;

import java.util.*;

import org.o42a.analysis.use.Usable;
import org.o42a.analysis.use.User;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.impl.ObjectResolution;
import org.o42a.core.object.type.*;
import org.o42a.core.object.value.ObjectValuePart;
import org.o42a.core.ref.RefUser;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


public final class ObjectType {

	private final Obj object;
	private Obj lastDefinition;
	private TypeParameters<?> parameters;
	private Usable<DerivationUsage> derivationUses;
	private LinkUses linkUses;
	private ObjectResolution resolution = NOT_RESOLVED;
	private Ascendants ascendants;
	private Map<Scope, Derivation> allAscendants;
	private ArrayList<Derivative> allDerivatives;
	private ValueType<?> valueType;

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

	public final boolean isRuntimeConstructed() {

		final Obj object = getObject();

		return object.getConstructionMode().isRuntime()
				|| !object.meta().isUpdated();
	}

	public final RefUser refUser() {
		return new RefUser(derivationUses(), rtDerivation());
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

	public final ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}
		return this.valueType = getParameters().getValueType();
	}

	public final TypeParameters<?> getParameters() {
		if (this.parameters != null) {
			return this.parameters;
		}

		final TypeParameters<?> parameters =
				getObject().determineTypeParameters();

		parameters.assertSameScope(getObject());

		return this.parameters = parameters.declaredIn(getObject());
	}

	public final boolean isResolved() {
		return getResolution().typeResolved();
	}

	public boolean inherits(ObjectType other) {
		if (getObject().is(other.getObject())) {
			return true;
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor == null) {
			return false;
		}

		return ancestor.getType().type().inherits(other);
	}

	public final Map<Scope, Derivation> allAscendants() {
		if (this.allAscendants != null) {
			return this.allAscendants;
		}

		final HashMap<Scope, Derivation> allAscendants = buildAllAscendants();

		if (getResolution().typeResolved()) {
			this.allAscendants = unmodifiableMap(allAscendants);
		}

		return allAscendants;
	}

	public final List<Derivative> allDerivatives() {
		if (this.allDerivatives == null) {
			return emptyList();
		}
		return this.allDerivatives;
	}

	public final ObjectType useBy(RefUser user) {
		if (user.hasRtUser()) {
			derivationUses().useBy(user.rtUser(), RUNTIME_DERIVATION_USAGE);
		}
		return this;
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
	}

	public final User<DerivationUsage> rtDerivation() {
		return derivationUses().usageUser(RUNTIME_DERIVATION_USAGE);
	}

	public final User<DerivationUsage> staticDerivation() {
		return derivationUses().usageUser(STATIC_DERIVATION_USAGE);
	}

	public final boolean derivedFrom(ObjectType other) {
		return allAscendants().containsKey(other.getObject().getScope());
	}

	public final boolean derivedFrom(ObjectType other, Derivation derivation) {

		final Derivation derivations =
				allAscendants().get(other.getObject().getScope());

		return derivations != null && derivations.is(derivation);
	}

	public final LinkUses linkUses() {
		if (this.linkUses != null) {
			return this.linkUses;
		}

		final ValueType<?> valueType = getValueType();

		if (!valueType.isLink()) {
			return null;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			return this.linkUses = cloneOf.type().linkUses();
		}

		return this.linkUses = new LinkUses(this);
	}

	public final void wrapBy(ObjectType type) {
		derivationUses().useBy(type.rtDerivation(), RUNTIME_DERIVATION_USAGE);
	}

	public void resolveAll() {
		getAscendants().resolveAll(this);
		registerInAncestor();
		registerSamples();

		final LinkUses linkUses = linkUses();

		if (linkUses != null) {
			linkUses.determineTargetComplexity();
		}
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
				!derived.meta().isUpdated()
				? RUNTIME_DERIVATION_USAGE : STATIC_DERIVATION_USAGE);
		derivationUses().useBy(
				derived.type().rtDerivation(),
				RUNTIME_DERIVATION_USAGE);
		trackUpdatesByAncestor(derived);
	}

	protected void useAsSample(Sample sample) {

		final Obj derived = sample.getDerivedObject();

		derivationUses().useBy(
				derived.content(),
				!derived.meta().isUpdated()
				? RUNTIME_DERIVATION_USAGE : STATIC_DERIVATION_USAGE);
		derivationUses().useBy(
				derived.type().rtDerivation(),
				RUNTIME_DERIVATION_USAGE);

		trackUpdatesBySample(sample);
		trackImplicitSampleRtDerivation(sample);
	}

	final void setKnownValueType(ValueType<?> valueType) {
		this.valueType = valueType;
	}

	final ValueType<?> getKnownValueType() {
		return this.valueType;
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
				getObject().getLogger().error(
						"recursive_resolution",
						getObject(),
						"Infinite recursion when resolving %s",
						getObject());
			}
			return false;
		}

		return this.resolution.resolved();
	}

	final TypeParameters<?> derivedParameters() {

		TypeParameters<?> parameters =
				typeParameters(getObject(), ValueType.VOID);
		boolean ancestorApplied = getAscendants().getExplicitAncestor() == null;

		for (Sample sample : getSamples()) {
			if (!ancestorApplied && sample.isExplicit()) {
				// Apply an explicit ancestor's parameters after
				// implicit samples, but before the explicit ones.
				ancestorApplied = true;
				parameters = applyAncestorParameters(parameters);
			}
			parameters = applySampleParameters(parameters, sample);
		}
		if (!ancestorApplied) {
			// Apply an ancestor parameters if not applied yet.
			parameters = applyAncestorParameters(parameters);
		}

		return applyExplicitParameters(parameters);
	}

	private final Usable<DerivationUsage> derivationUses() {
		if (this.derivationUses != null) {
			return this.derivationUses;
		}

		final Obj object = getObject();

		if (!object.meta().isUpdated()) {
			return this.derivationUses =
					object.getCloneOf().type().derivationUses();
		}

		this.derivationUses = DerivationUsage.usable("DerivationOf", object);

		final Member member = object.toMember();

		if (member != null) {
			detectFieldRtDerivation(member);
		} else {
			detectStandaloneObjectRtDerivation();
		}

		return this.derivationUses;
	}

	private void detectFieldRtDerivation(final Member member) {

		// Detect run time construction mode by member.
		final MemberField field = member.toField();

		if (field == null) {
			return;
		}

		this.derivationUses.useBy(
				field.getAnalysis().rtDerivation(),
				RUNTIME_DERIVATION_USAGE);
		this.derivationUses.useBy(
				field.getAnalysis().staticDerivation(),
				STATIC_DERIVATION_USAGE);
	}

	private void detectStandaloneObjectRtDerivation() {
		if (getObject().getScope().getEnclosingScope().isTopScope()) {
			return;
		}

		// Stand-alone object is constructed at run time, if it's ever derived.
		this.derivationUses.useBy(
				staticDerivation(),
				RUNTIME_DERIVATION_USAGE);

		// Stand-alone object is constructed at run time
		// if its owner's value is ever used at runtime.
		this.derivationUses.useBy(
				getOwner().value().rtUses(),
				RUNTIME_DERIVATION_USAGE);
	}

	private Obj getOwner() {

		final Scope enclosingScope =
				getObject().getScope().getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			return enclosingObject;
		}

		return enclosingScope.toMember().getMemberOwner().getOwner();
	}

	private HashMap<Scope, Derivation> buildAllAscendants() {

		final HashMap<Scope, Derivation> allAscendants = new HashMap<>();

		allAscendants.put(getObject().getScope(), Derivation.SAME);

		resolve(true);
		if (this.ascendants == null) {
			return allAscendants;
		}

		final TypeRef ancestor = this.ascendants.getAncestor();

		if (ancestor != null) {

			final ObjectType type = ancestor.getType().type();

			for (Scope scope : type.allAscendants().keySet()) {
				allAscendants.put(scope, Derivation.INHERITANCE);
			}
		}

		addSamplesAscendants(allAscendants, this.ascendants.getSamples());
		addSamplesAscendants(
				allAscendants,
				this.ascendants.getDiscardedSamples());

		return allAscendants;
	}

	private void addSamplesAscendants(
			HashMap<Scope, Derivation> allAscendants,
			Sample[] samples) {
		for (Sample sample : samples) {

			final ObjectType type = sample.getObject().type();

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

		if (ancestor != null && ancestor.isValid()) {
			ancestor.getType().type().useAsAncestor(getObject());
		}
	}

	private void registerSamples() {
		for (Sample sample : getSamples()) {

			final TypeRef sampleTypeRef = sample.getTypeRef();

			if (sampleTypeRef.isValid()) {
				sampleTypeRef.getType().type().useAsSample(sample);
			}
		}
	}

	private void trackUpdatesByAncestor(Obj derived) {
		if (!derived.meta().isUpdated()) {
			return;
		}

		trackAscendantDefsUsage(derived);

		final LinkUses linkUses = linkUses();

		if (linkUses != null) {
			linkUses.useAsAncestor(derived);
		}
		if (derived.getWrapped() == null) {
			registerDerivative(new Inheritor(derived));
		}
	}

	private void trackUpdatesBySample(Sample sample) {

		final Obj derived = sample.getDerivedObject();

		if (!derived.meta().isUpdated()) {
			return;
		}

		trackAscendantDefsUsage(derived);
		trackAncestorDefsUpdates(derived);

		final LinkUses linkUses = linkUses();

		if (linkUses != null) {
			linkUses.useAsSample(sample);
		}
		if (derived.getWrapped() == null) {
			registerDerivative(sample);
		}
	}

	private void trackImplicitSampleRtDerivation(Sample sample) {
		if (sample.isExplicit()) {
			return;
		}

		// Run time derivation of implicit sample means
		// the owner object's value can be constructed at run time.
		final Obj derived = sample.getDerivedObject();
		final Obj enclosingObject = sample.getScope().toObject();
		final Obj owner;

		if (enclosingObject != null) {
			owner = enclosingObject;
		} else {
			owner = sample.getScope().toLocal().getOwner();
		}

		owner.value().uses().useBy(
				derived.type().rtDerivation(),
				EXPLICIT_RUNTIME_VALUE_USAGE);
	}

	private void trackAscendantDefsUsage(Obj derived) {

		final Obj ancestor = getObject();
		final ObjectValue ascendantValue = getObject().value();
		final ObjectValue derivedValue = derived.value();

		trackAscendantPartUsage(
				ancestor,
				ascendantValue,
				derivedValue,
				true);
		trackAscendantPartUsage(
				ancestor,
				ascendantValue,
				derivedValue,
				false);
	}

	private void trackAscendantPartUsage(
			final Obj ancestor,
			final ObjectValue ascendantValue,
			final ObjectValue derivedValue,
			final boolean claim) {

		final ObjectValuePart derivedPart = derivedValue.part(claim);

		if (derivedPart.getDefs().presentIn(ancestor)) {
			ascendantValue.part(claim).accessBy(derivedPart);
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

		final ObjectValue newAncestorValue = newAncestor.getType().value();
		final Obj oldAncestorObject = oldAncestor.getType();

		trackAncestorPartUpdates(
				since,
				newAncestorValue,
				oldAncestorObject,
				true);
		trackAncestorPartUpdates(
				since,
				newAncestorValue,
				oldAncestorObject,
				false);
	}

	private void trackAncestorPartUpdates(
			final Obj since,
			final ObjectValue newAncestorValue,
			final Obj oldAncestorObject,
			final boolean claim) {

		final ObjectValuePart sampleValuePart =
				getObject().value().part(claim);
		final ObjectValuePart sinceValuePart =
				since.value().part(claim);

		sampleValuePart.updateAncestorDefsBy(
				sinceValuePart.ancestorDefsUpdatesUser());
		if (newAncestorValue.part(claim).getDefs().updatedSince(
				oldAncestorObject)) {
			sampleValuePart.updateAncestorDefsBy(sinceValuePart);
		}
	}

	private void registerDerivative(Derivative derivative) {
		if (this.allDerivatives == null) {
			this.allDerivatives = new ArrayList<>();
		}
		this.allDerivatives.add(derivative);
		if (!getObject().meta().isUpdated()) {
			// Clone is explicitly derived.
			// Update the derivation tree.
			final Sample[] samples = getSamples();

			if (samples.length != 0) {

				final Sample sample = getSamples()[0];

				sample.getObject().type().registerDerivative(sample);
			}
		}
	}

	private TypeParameters<?> applyAncestorParameters(
			TypeParameters<?> parameters) {
		return getAscendants()
				.getExplicitAncestor()
				.getType()
				.type()
				.getParameters()
				.upgradeScope(getObject().getScope())
				.refine(parameters);
	}

	private TypeParameters<?> applySampleParameters(
			TypeParameters<?> parameters,
			Sample sample) {
		return sample.getObject()
				.type()
				.getParameters()
				.upgradeScope(getObject().getScope())
				.refine(parameters);
	}

	private TypeParameters<?> applyExplicitParameters(
			TypeParameters<?> parameters) {

		final ObjectTypeParameters explicitParameters =
				getAscendants().getExplicitParameters();

		if (explicitParameters == null) {
			return parameters;
		}

		final TypeParameters<?> result =
				explicitParameters.refine(
						getObject(),
						parameters.explicitlyRefineFor(getObject()));

		result.assertSameScope(getObject());

		return result;
	}

}
