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
package org.o42a.core.object.type;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.DefinitionTarget.defaultDefinition;
import static org.o42a.core.member.field.DefinitionTarget.definitionTarget;
import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;
import static org.o42a.core.object.ConstructionMode.DYNAMIC_CONSTRUCTION;
import static org.o42a.core.object.ConstructionMode.STATIC_CONSTRUCTION;
import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;

import org.o42a.core.Scope;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.DefinitionTarget;
import org.o42a.core.object.*;
import org.o42a.core.object.type.impl.ImplicitSample;
import org.o42a.core.object.type.impl.MemberOverride;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.RefUser;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


public class Ascendants
		implements AscendantsBuilder<Ascendants>, Cloneable {

	private final Obj object;
	private TypeRef explicitAncestor;
	private TypeRef implicitAncestor;
	private ObjectTypeParameters explicitParameters;
	private TypeRef ancestor;
	private Sample sample;
	private ConstructionMode constructionMode;
	private boolean validated;

	public Ascendants(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Scope getScope() {
		return this.object.getScope();
	}

	public TypeRef getAncestor() {
		if (this.ancestor == null) {
			validate();
			if (this.explicitAncestor != null) {
				this.ancestor = this.explicitAncestor;
			} else {
				this.ancestor = sampleAncestor();
			}
		}
		return this.ancestor;
	}

	public final TypeRef getExplicitAncestor() {
		return this.explicitAncestor;
	}

	public final TypeRef getImplicitAncestor() {
		return this.implicitAncestor;
	}

	@Override
	public final Ascendants setAncestor(TypeRef ancestor) {
		return setAncestor(ancestor, false);
	}

	public final ObjectTypeParameters getExplicitParameters() {
		return this.explicitParameters;
	}

	@Override
	public final Ascendants setParameters(
			ObjectTypeParameters typeParameters) {

		final Ascendants clone = clone();

		clone.explicitParameters = typeParameters;

		return clone;
	}

	public final Sample getSample() {
		return this.sample;
	}

	public ConstructionMode getConstructionMode() {
		if (this.constructionMode != null) {
			return this.constructionMode;
		}

		final ConstructionMode enclosingMode = enclosingConstructionMode();
		ConstructionMode constructionMode;

		if (enclosingMode.isRuntime()) {
			return this.constructionMode = enclosingMode;
		}
		if (enclosingMode.isStrict()) {
			constructionMode = enclosingMode;
		} else {
			constructionMode = null;
		}

		final Sample sample = getSample();

		if (sample != null) {

			final ConstructionMode sampleMode =
					sample.getObject().getConstructionMode();

			if (sampleMode.isRuntime()) {
				return this.constructionMode = sampleMode;
			}

			constructionMode =
					constructionMode != null
					? constructionMode.restrictBy(sampleMode)
					: sampleMode;
		}

		final TypeRef ancestor = getExplicitAncestor();

		if (ancestor == null) {
			return this.constructionMode =
					constructionMode != null
					? constructionMode
					: STATIC_CONSTRUCTION;
		}

		final ConstructionMode ancestorMode = ancestor.getConstructionMode();

		if (ancestorMode.isRuntime()) {
			return this.constructionMode = ancestorMode;
		}
		if (ancestor.isStatic()) {
			return this.constructionMode =
					constructionMode != null
					? constructionMode.restrictBy(STATIC_CONSTRUCTION)
					: STATIC_CONSTRUCTION;
		}

		return this.constructionMode =
				constructionMode != null
				? constructionMode.restrictBy(DYNAMIC_CONSTRUCTION)
				: DYNAMIC_CONSTRUCTION;
	}

	public final boolean isEmpty() {
		if (getExplicitAncestor() != null) {
			return false;
		}
		if (getExplicitParameters() != null) {
			return false;
		}
		return getSample() == null;
	}

	public final DefinitionTarget getDefinitionTarget() {
		if (isEmpty()) {
			return defaultDefinition();
		}

		final TypeRef ancestor = getExplicitAncestor();

		if (ancestor != null) {

			final TypeParameters<?> parameters = ancestor.getParameters();

			if (!parameters.getValueType().isVoid()) {
				return definitionTarget(parameters);
			}
		}

		final Sample sample = getSample();

		if (sample != null) {

			final TypeParameters<?> typeParameters =
					sample.getObject().type().getParameters();

			if (!typeParameters.getValueType().isVoid()) {
				return definitionTarget(typeParameters);
			}
		}

		return objectDefinition();
	}

	@Override
	public Ascendants addImplicitSample(
			StaticTypeRef implicitAscendant,
			TypeRef overriddenAncestor) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		implicitAscendant.assertCompatible(enclosingScope);

		return addSample(new ImplicitSample(
				this,
				implicitAscendant,
				overriddenAncestor));
	}

	@Override
	public Ascendants addMemberOverride(Member overriddenMember) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		enclosingScope.assertDerivedFrom(overriddenMember.getScope());
		assert overriddenMember.substance(dummyUser()).toObject() != null :
			"Can not override non-object member " + overriddenMember;

		return addSample(new MemberOverride(overriddenMember, this));
	}

	public void resolveAll(ObjectType objectType) {
		validate();
		validateAncestors(objectType);

		final RefUser user = getObject().type().refUser();
		final TypeRef ancestor = getExplicitAncestor();
		final FullResolver resolver =
				getScope()
				.getEnclosingScope()
				.resolver()
				.fullResolver(user, TYPE_REF_USAGE);

		if (ancestor != null) {
			ancestor.resolveAll(resolver);
		}

		final Sample sample = getSample();

		if (sample != null) {
			sample.resolveAll(resolver);
		}
	}

	public Ascendants declareField(FieldAscendants fieldAscendants) {

		final Member member = getScope().toMember();

		if (member.isOverride()) {
			return overrideMember(member, fieldAscendants);
		}

		return declareMember(member, fieldAscendants);
	}

	public void validate() {
		if (this.validated) {
			return;
		}
		this.validated = true;
		if (this.explicitAncestor != null) {
			if (!validateAncestor(this.explicitAncestor)) {
				if (this.implicitAncestor == this.explicitAncestor) {
					this.implicitAncestor = null;
				}
				this.explicitAncestor = null;
			}
		}
		if (this.implicitAncestor != null
				&& this.implicitAncestor != this.explicitAncestor) {
			if (!validateAncestor(this.implicitAncestor)) {
				this.implicitAncestor = null;
			}
		}
		if (!validateSample()) {
			this.sample = null;
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("Ascendants[");
		if (this.ancestor != null) {
			out.append("ancestor=");
			out.append(this.ancestor);
			comma = true;
		} else if (this.explicitAncestor != null) {
			out.append("ancestor=");
			out.append(this.explicitAncestor);
			comma = true;
		}
		if (this.sample != null) {
			if (comma) {
				out.append(' ');
			}
			out.append("sample=");
			out.append(this.sample);
		}
		out.append(']');

		return out.toString();
	}

	@Override
	protected Ascendants clone() {
		try {

			final Ascendants clone = (Ascendants) super.clone();

			clone.ancestor = null;
			clone.validated = false;

			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	private Ascendants setAncestor(TypeRef ancestor, boolean implicit) {
		getScope().getEnclosingScope().assertDerivedFrom(ancestor.getScope());

		final Ascendants clone = clone();

		if (implicit) {
			clone.implicitAncestor = ancestor;
			if (this.explicitAncestor != null) {
				return clone;
			}
		}

		clone.explicitAncestor =
				ancestor.upgradeScope(getScope().getEnclosingScope());

		return clone;
	}

	private Ascendants addSample(Sample sample) {
		assert this.sample == null :
			"Redundant sample: " + sample;

		final Ascendants clone = clone();

		clone.sample = sample;

		return clone;
	}

	private Ascendants declareMember(
			Member member,
			FieldAscendants fieldAscendants) {

		Ascendants ascendants = this;
		final Scope enclosingScope = getScope().getEnclosingScope();
		final AdapterId adapterId = member.getMemberId().getAdapterId();

		if (adapterId != null && !fieldAscendants.isLinkAscendants()) {
			ascendants = ascendants.setAncestor(
					adapterId.adapterType(enclosingScope),
					true);
		}
		ascendants = fieldAscendants.updateAscendants(ascendants);
		if (!ascendants.isEmpty()) {
			return ascendants;
		}

		return ascendants.setAncestor(ValueType.VOID.typeRef(
				member,
				enclosingScope));
	}

	private Ascendants overrideMember(
			Member member,
			FieldAscendants fieldAscendants) {

		Ascendants ascendants = this;

		for (Member overridden : member.getOverridden()) {
			ascendants = ascendants.addMemberOverride(overridden);
			break;
		}

		return fieldAscendants.updateAscendants(ascendants);
	}

	private boolean validateAncestor(TypeRef ancestor) {
		if (!ancestor.isValid()) {
			return false;
		}
		if (!validateUse(ancestor)) {
			return false;
		}
		if (ancestor.getConstructionMode().isProhibited()) {
			ancestor.getLogger().error(
					"cant_inherit",
					ancestor,
					"Can not be inherited");
			return false;
		}
		return true;
	}

	private boolean validateUse(TypeRef checkUse) {
		if (checkUse == null) {
			return true;
		}
		return Role.PROTOTYPE.checkUseBy(
				getObject(),
				checkUse.getRef(),
				checkUse.getScope());
	}

	private TypeRef sampleAncestor() {

		final Sample sample = getSample();

		if (sample == null) {
			return null;
		}

		return sample.getAncestor();
	}

	private void validateAncestors(ObjectType objectType) {

		final TypeRef ancestor = objectType.getAncestor();

		if (ancestor == null) {
			return;
		}

		validateImplicitAncestor(ancestor);
		validateSampleAncestor(objectType, ancestor);
	}

	private boolean validateSample() {

		final Sample sample = getSample();

		if (sample == null) {
			return true;
		}
		if (!sample.getTypeRef().isValid()) {
			return false;
		}
		if (!sample.getAncestor().isValid()) {
			return false;
		}

		return true;
	}

	private void validateImplicitAncestor(TypeRef ancestor) {
		if (this.implicitAncestor == null) {
			return;
		}
		if (!ancestor.derivedFrom(this.implicitAncestor)) {
			getScope().getLogger().error(
					"unexpected_ancestor",
					ancestor,
					"Wrong ancestor: %s, but expected: %s",
					ancestor,
					this.implicitAncestor);
			this.explicitAncestor = null;
		}
	}

	private void validateSampleAncestor(
			ObjectType objectType,
			TypeRef ancestor) {

		final Sample sample = getSample();

		if (sample == null) {
			return;
		}

		final TypeRef parameterizedAncestor =
				parameterizedAncestor(objectType, ancestor);
		final TypeRef sampleAncestor =
				sample.overriddenAncestor()
				.rescope(parameterizedAncestor.getScope());
		final TypeRelation relation =
				parameterizedAncestor.relationTo(sampleAncestor)
				.check(getScope().getLogger());

		if (!relation.isDerivative()) {
			if (!relation.isError()) {
				getScope().getLogger().error(
						"unexpected_ancestor",
						sample,
						"Wrong ancestor: %s, but expected: %s",
						parameterizedAncestor,
						sampleAncestor);
			}
			this.explicitAncestor = null;
			return;
		}

		if (!getConstructionMode().canUpgradeAncestor()
				&& !relation.ignoreParameters().isSame()) {
			relation.ignoreParameters().isSame();
			getScope().getLogger().error(
					"prohibited_ancestor_upgrade",
					getAncestor(),
					"Ancestor can no be upgraded");
		}
	}

	private TypeRef parameterizedAncestor(
			ObjectType objectType,
			TypeRef ancestor) {

		final TypeParameters<?> parameters = objectType.getParameters();

		if (parameters.isEmpty()) {
			return ancestor;
		}

		return ancestor.rescope(parameters.getScope())
				.setParameters(parameters);
	}

	private ConstructionMode enclosingConstructionMode() {

		final Scope enclosingScope = getScope().getEnclosingScope();

		if (enclosingScope == null) {
			return STATIC_CONSTRUCTION;
		}

		return enclosingScope.getConstructionMode();
	}

}
