/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import static org.o42a.core.value.TypeParameters.typeParameters;

import java.util.*;

import org.o42a.analysis.use.User;
import org.o42a.core.Scope;
import org.o42a.core.object.impl.ObjectResolution;
import org.o42a.core.object.type.*;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.RefUser;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


public final class ObjectType {

	private final Obj object;
	private final DerivationUses derivationUses;
	private Obj lastDefinition;
	private TypeParameters<?> parameters;
	private LinkUses linkUses;
	private ObjectResolution resolution = NOT_RESOLVED;
	private Ascendants ascendants;
	private Map<Scope, Derivation> allAscendants;
	private ArrayList<Derivative> allDerivatives;
	private ValueType<?> valueType;

	ObjectType(Obj object) {
		this.object = object;
		this.derivationUses = new DerivationUses(this);
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
		return derivationUses().refUser();
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
		derivationUses().useBy(user);
		return this;
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
	}

	public final User<DerivationUsage> rtDerivation() {
		return derivationUses().rtUser();
	}

	public final User<DerivationUsage> staticDerivation() {
		return derivationUses().staticUser();
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
		derivationUses().wrapBy(type.derivationUses());
	}

	public void resolveAll() {
		getAscendants().resolveAll(this);
		derivationUses().resolveAll();

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

	final DerivationUses derivationUses() {
		return this.derivationUses;
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

	final Statefulness derivedStatefulness() {

		final TypeRef ancestor = getAscendants().getAncestor();

		if (ancestor != null) {

			final Statefulness ancestorStatefulness =
					ancestor.getType().value().getStatefulness();

			if (ancestorStatefulness.isStateful()) {
				return ancestorStatefulness;
			}
		}
		for (Sample sample : getSamples()) {

			final Statefulness sampleStatefulness =
					sample.getObject().value().getStatefulness();

			if (sampleStatefulness.isStateful()) {
				return sampleStatefulness;
			}
		}

		return Statefulness.STATELESS;
	}

	void registerDerivative(Derivative derivative) {
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
