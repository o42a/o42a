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
import static org.o42a.core.object.LinkUses.linkUsesFor;
import static org.o42a.core.object.impl.ObjectResolution.NOT_RESOLVED;
import static org.o42a.core.value.TypeParameters.typeParameters;
import static org.o42a.util.fn.CondInit.condInit;
import static org.o42a.util.fn.Init.init;

import java.util.*;
import java.util.function.BiFunction;

import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Scope;
import org.o42a.core.object.impl.ObjectResolution;
import org.o42a.core.object.type.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.*;
import org.o42a.util.fn.CondInit;
import org.o42a.util.fn.Init;


public final class ObjectType implements UserInfo {

	private final Obj object;

	private final DerivationUses derivationUses =
			new DerivationUses(this);
	private final Init<Obj> lastDefinition =
			init(this::findLastDefinition);
	private final Init<Obj> sampleDeclaration =
			init(this::findSampleDeclaration);
	private final Init<TypeParameters<?>> parameters =
			init(this::buildParameters);
	private final Init<LinkUses> linkUses =
			init(() -> linkUsesFor(this));
	private final Init<ValueType<?>> valueType =
			init(() -> getParameters().getValueType());

	private ObjectResolution resolution = NOT_RESOLVED;
	private Ascendants ascendants;

	private CondInit<java.lang.Void, Map<Scope, Derivation>> allAscendants =
			condInit(
					m -> getResolution().typeResolved(),
					() -> getResolution().typeResolved()
					? unmodifiableMap(buildAllAscendants())
					: buildAllAscendants());

	private ArrayList<Derivative> allDerivatives;
	private ArrayList<Derivative> updatedDerivatives;

	ObjectType(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Obj getLastDefinition() {
		return this.lastDefinition.get();
	}

	public final Obj getSampleDeclaration() {
		return this.sampleDeclaration.get();
	}

	/**
	 * Whether the object expected to be constructed at run time.
	 *
	 * <p>This is so e.g. for clones, variable values, or for some
	 * {@link #isRuntimeEager() eager objects}.</p>
	 *
	 * @return <code>true</code> if object will be constructed at run time,
	 * or <code>false</code> if it can be constructed at compile time.
	 */
	public final boolean isRuntimeConstructed() {

		final Obj object = getObject();

		if (object.getConstructionMode().isRuntime()) {
			return true;
		}
		if (isRuntimeEager()) {
			return true;
		}
		if (!object.meta().isUpdated()) {
			return true;// Clones are preferably constructed at run time.
		}

		return false;
	}

	/**
	 * Whether an eager object should be constructed at run time only.
	 *
	 * <p>Eager object can be constructed at compile time only if its value
	 * is known at compile time.</p>
	 *
	 * @return <code>false</code> if the object is not eager, or can be
	 * constructed at compile time, or <code>true</code> otherwise.
	 */
	public final boolean isRuntimeEager() {
		if (!getObject().value().getStatefulness().isEager()) {
			return false;
		}

		final Value<?> value = getObject().value().getValue();

		return !value.getKnowledge().isInitiallyKnown();
	}

	@Override
	public final User<?> toUser() {
		return derivationUses().toUser();
	}

	public final Ascendants getAscendants() {
		resolve(false);
		return this.ascendants;
	}

	public TypeRef getAncestor() {
		return getAscendants().getAncestor();
	}

	public final Sample getSample() {
		return getAscendants().getSample();
	}

	public final ValueType<?> getValueType() {
		return this.valueType.get();
	}

	public final TypeParameters<?> getParameters() {
		return this.parameters.get();
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
		return this.allAscendants.apply(null);
	}

	public final List<Derivative> allDerivatives() {
		if (this.allDerivatives == null) {
			return emptyList();
		}
		return this.allDerivatives;
	}

	public final List<Derivative> updatedDerivatives() {
		if (this.updatedDerivatives == null) {
			return emptyList();
		}
		return this.updatedDerivatives;
	}

	public final <T> T eachDerivative(T t, BiFunction<DerivedObject, T, T> f) {
		return eachDerivativeOf(new DerivedObject(getObject()), t, f);
	}

	public final <T> T eachOverrider(T t, BiFunction<DerivedObject, T, T> f) {
		return eachOverriderOf(new DerivedObject(getObject()), t, f);
	}

	public final ObjectType useBy(UserInfo user) {
		derivationUses().useBy(user);
		return this;
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
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
		return this.linkUses.get();
	}

	public final void wrapBy(ObjectType type) {
		derivationUses().wrapBy(type.derivationUses());
	}

	public void resolveAll() {
		getAscendants().resolveAll();
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
		this.valueType.set(valueType);
	}

	final ValueType<?> getKnownValueType() {
		return this.valueType.getKnown();
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

		parameters = applySampleParameters(parameters);
		parameters = applyAncestorParameters(parameters);

		return applyExplicitParameters(parameters);
	}

	void registerDerivative(Derivative derivative) {
		if (this.allDerivatives == null) {
			this.allDerivatives = new ArrayList<>();
		}
		this.allDerivatives.add(derivative);
		if (getObject().isWrapper()) {
			getObject().getWrapped().type().registerDerivative(derivative);
		}
	}

	void registerUpdatedDerivative(Derivative derivative) {
		if (this.updatedDerivatives == null) {
			this.updatedDerivatives = new ArrayList<>();
		}
		this.updatedDerivatives.add(derivative);
		if (!getObject().meta().isUpdated()) {
			// Clone is explicitly derived.
			// Update the derivation tree.
			final Sample sample = getSample();

			if (sample != null) {
				sample.getObject().type().registerUpdatedDerivative(sample);
			}
		}
	}

	private Obj findLastDefinition() {

		final Obj object = getObject();
		final Obj cloneOf = object.getCloneOf();

		return cloneOf != null ? cloneOf : object;
	}

	private Obj findSampleDeclaration() {

		Obj declaration = getObject();

		for (;;) {

			final Sample sample =
					declaration.type().getAscendants().getSample();

			if (sample == null) {
				break;
			}

			declaration = sample.getObject();
		}

		return  declaration;
	}

	private TypeParameters<?> buildParameters() {

		final TypeParameters<?> parameters =
				getObject().determineTypeParameters();

		parameters.assertSameScope(getObject());

		return parameters.declaredIn(getObject());
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

		addSamplesAscendants(allAscendants);

		return allAscendants;
	}

	private void addSamplesAscendants(
			HashMap<Scope, Derivation> allAscendants) {

		final Sample sample = getSample();

		if (sample == null) {
			return;
		}

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

	private TypeParameters<?> applyAncestorParameters(
			TypeParameters<?> parameters) {

		final TypeRef explicitAncestor =
				getAscendants().getExplicitAncestor();

		if (explicitAncestor == null) {
			return parameters;
		}

		return explicitAncestor.getType()
				.type()
				.getParameters()
				.upgradeScope(getObject().getScope())
				.refine(parameters);
	}

	private TypeParameters<?> applySampleParameters(
			TypeParameters<?> parameters) {

		final Sample sample = getSample();

		if (sample == null) {
			return parameters;
		}

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

	private <T> T eachDerivativeOf(
			DerivedObject object,
			T t,
			BiFunction<DerivedObject, T, T> f) {

		T result = f.apply(object, t);

		if (object.isDone()) {
			return t;
		}
		for (Derivative d : object.getDerivedObject().type().allDerivatives()) {

			final Obj derived = d.getDerivedObject();

			if (!derived.meta().isUpdated()) {
				continue;
			}
			result = eachDerivativeOf(object.set(derived), result, f);
			if (object.isDone()) {
				break;
			}
		}

		return t;
	}

	private <T> T eachOverriderOf(
			DerivedObject object,
			T t,
			BiFunction<DerivedObject, T, T> f) {

		T result = f.apply(object, t);

		if (object.isDone()) {
			return t;
		}
		for (Derivative d : object.getDerivedObject().type().allDerivatives()) {
			if (!d.isSample()) {
				continue;
			}

			final Obj derived = d.getDerivedObject();

			if (!derived.meta().isUpdated()) {
				continue;
			}
			result = eachOverriderOf(object.set(derived), result, f);
			if (object.isDone()) {
				break;
			}
		}

		return t;
	}

}
