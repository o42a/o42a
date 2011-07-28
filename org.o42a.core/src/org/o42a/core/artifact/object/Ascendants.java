/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.util.use.User.dummyUser;

import java.util.Arrays;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Role;
import org.o42a.core.artifact.object.impl.sample.ExplicitSample;
import org.o42a.core.artifact.object.impl.sample.ImplicitSample;
import org.o42a.core.artifact.object.impl.sample.MemberOverride;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;
import org.o42a.util.Lambda;
import org.o42a.util.use.UserInfo;


public class Ascendants
		implements AscendantsBuilder<Ascendants>, Cloneable {

	private static final Sample[] NO_SAMPLES = new Sample[0];

	private final Obj object;
	private TypeRef explicitAncestor;
	private TypeRef ancestor;
	private Sample[] samples = NO_SAMPLES;
	private Sample[] discardedSamples = NO_SAMPLES;
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

	@Override
	public Ascendants setAncestor(TypeRef explicitAncestor) {
		getScope().getEnclosingScope().assertDerivedFrom(
				explicitAncestor.getScope());

		final Ascendants clone = clone();

		clone.explicitAncestor =
			explicitAncestor.upgradeScope(getScope().getEnclosingScope());

		return clone;
	}

	public final Sample[] getSamples() {
		return this.samples;
	}

	public final Sample[] getDiscardedSamples() {
		return this.discardedSamples;
	}

	public ConstructionMode getConstructionMode() {
		if (this.constructionMode != null) {
			return this.constructionMode;
		}

		final ConstructionMode enclosingMode = enclosingConstructionMode();

		if (enclosingMode.isRuntime()) {
			return enclosingMode;
		}

		final TypeRef ancestor = getExplicitAncestor();

		if (ancestor != null) {

			final ConstructionMode ancestorMode =
				ancestor.getConstructionMode();

			if (!ancestorMode.isProhibited()) {
				return ancestorMode;
			}
		}

		return enclosingMode;
	}

	public final boolean isEmpty() {
		if (getExplicitAncestor() != null) {
			return false;
		}
		return getSamples().length == 0;
	}

	@Override
	public Ascendants addExplicitSample(StaticTypeRef explicitAscendant) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		explicitAscendant.assertCompatible(enclosingScope);

		return addSample(new ExplicitSample(explicitAscendant, this));
	}

	@Override
	public Ascendants addImplicitSample(StaticTypeRef implicitAscendant) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		implicitAscendant.assertCompatible(enclosingScope);

		return addSample(new ImplicitSample(implicitAscendant, this));
	}

	@Override
	public Ascendants addMemberOverride(Member overriddenMember) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		enclosingScope.assertDerivedFrom(overriddenMember.getScope());
		assert overriddenMember.substance(dummyUser()).toObject() != null :
			"Can not override non-object member " + overriddenMember;

		return addSample(new MemberOverride(overriddenMember, this));
	}

	public void resolveAll() {
		validate();

		final UserInfo user = getObject().type();
		final TypeRef ancestor = getExplicitAncestor();
		final Resolver resolver =
			getScope().getEnclosingScope().newResolver(user);

		if (ancestor != null) {
			ancestor.type(user);
			ancestor.resolveAll(resolver);
		}
		for (Sample sample : getSamples()) {
			sample.type(user);
			sample.resolveAll(resolver);
		}
	}

	public final Ascendants declareMember() {
		return declareField(null);
	}

	public Ascendants declareField(Lambda<Ascendants, Ascendants> explicit) {

		final Member member = getScope().toMember();

		if (member.isOverride()) {
			return overrideMember(member, explicit);
		}

		return declareMember(member, explicit);
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
		if (this.samples.length != 0) {
			if (comma) {
				out.append(' ');
			}
			out.append("samples=");
			out.append(Arrays.toString(this.samples));
			comma = true;
		}
		if (this.discardedSamples.length != 0) {
			if (comma) {
				out.append(' ');
			}
			out.append("discarded=");
			out.append(Arrays.toString(this.discardedSamples));
		}
		out.append(']');

		return out.toString();
	}

	@Override
	protected Ascendants clone() {
		try {
			return (Ascendants) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	void validate() {
		if (this.validated) {
			return;
		}
		this.validated = true;
		if (this.explicitAncestor != null) {
			if (!this.explicitAncestor.validate()) {
				this.explicitAncestor = null;
			} else if (!validateUse(this.explicitAncestor)) {
				this.explicitAncestor = null;
			} else if (this.explicitAncestor
					.getConstructionMode().isProhibited()) {
				this.explicitAncestor.getLogger().error(
						"cant_inherit",
						this.explicitAncestor,
						"Can not be inherited");
				this.explicitAncestor = null;
			}
		}
		for (int i = this.samples.length - 1; i >= 0 ; --i) {

			final Sample sample = this.samples[i];

			if (!validateSample(sample, i)) {
				this.samples = ArrayUtil.remove(this.samples, i);
			}
		}
	}

	private Ascendants addSample(Sample sample) {
		assert (!sample.isExplicit()
				|| this.samples.length == 0
				|| !this.samples[0].isExplicit()) :
			"Implicit sample should be added before explicit one: " + sample;

		final Ascendants clone = clone();

		clone.samples = ArrayUtil.prepend(sample, this.samples);

		return clone;
	}

	private Ascendants declareMember(
			Member member,
			Lambda<Ascendants, Ascendants> explicit) {

		Ascendants ascendants = this;
		final Scope enclosingScope = getScope().getEnclosingScope();
		final AdapterId adapterId = member.getId().getAdapterId();

		if (adapterId != null) {
			ascendants = ascendants.addExplicitSample(
					adapterId.adapterType(enclosingScope));
		}
		if (explicit != null) {
			ascendants = explicit.get(ascendants);
		}
		if (!ascendants.isEmpty()) {
			return ascendants;
		}

		return ascendants.setAncestor(ValueType.VOID.typeRef(
				member,
				enclosingScope));
	}

	private Ascendants overrideMember(
			Member member,
			Lambda<Ascendants, Ascendants> explicit) {

		Ascendants ascendants = this;
		final ObjectType containerType =
				member.getContainer().toObject().type();
		final TypeRef ancestor = containerType.getAncestor();

		if (ancestor != null) {

			final Member overridden =
					ancestor.typeObject(dummyUser()).member(member.getKey());

			if (overridden != null) {
				ascendants = ascendants.addMemberOverride(overridden);
			}
		}

		final Sample[] containerSamples = containerType.getSamples();

		for (int i = containerSamples.length - 1; i >= 0; --i) {

			final Member overridden =
					containerSamples[i].type(dummyUser())
					.getObject().member(member.getKey());

			if (overridden != null) {
				ascendants = ascendants.addMemberOverride(overridden);
			}
		}

		if (explicit == null) {
			return ascendants;
		}

		return explicit.get(ascendants);
	}

	private boolean validateUse(TypeRef checkUse) {
		if (checkUse == null) {
			return true;
		}
		return Role.PROTOTYPE.checkUseBy(
				getObject(),
				checkUse.getRef(),
				checkUse.getRescoper().rescope(checkUse.getScope()));
	}

	private TypeRef sampleAncestor() {

		TypeRef result = null;

		for (Sample sample : this.samples) {

			final TypeRef ancestor = sample.getAncestor();

			if (ancestor == null) {
				continue;
			}
			if (result == null) {
				result = ancestor;
				continue;
			}
			result = result.commonDerivative(ancestor);
		}

		return result;
	}

	private boolean validateSample(Sample sample, int index) {
		if (!sample.getTypeRef().validate()) {
			return false;
		}

		final StaticTypeRef explicitAscendant = sample.getExplicitAscendant();

		if (explicitAscendant != null) {
			if (!validateUse(sample.getTypeRef())) {
				return false;
			}

			final ConstructionMode sampleConstructionMode =
				explicitAscendant.getConstructionMode();

			if (sampleConstructionMode.isStrict()) {
				getScope().getLogger().error(
						"prohibited_sample",
						sample,
						"Can not be used as sample");
				return false;
			}

			final ConstructionMode constructionMode = getConstructionMode();

			if (constructionMode.isStrict()) {
				getScope().getLogger().error(
						"prohibited_strict_sample",
						sample,
						"Strictly constructed object can not have a sample");
				return false;
			}
		}

		final TypeRef sampleAncestor = sample.getAncestor();

		if (!sampleAncestor.validate()) {
			return false;
		}

		final boolean explicit = sample.isExplicit();
		final TypeRef ancestor = getExplicitAncestor();

		if (ancestor != null) {

			final TypeRef first;
			final TypeRef second;

			if (explicit) {
				first = ancestor;
				second = sampleAncestor;
			} else {
				first = sampleAncestor;
				second = ancestor;
			}

			final TypeRelation relation =
				first.relationTo(second).revert(!explicit);

			if (!relation.isDerivative()) {
				if (!relation.isError()) {
					ancestor.relationTo(sampleAncestor);
					getScope().getLogger().error(
							"unexpected_ancestor",
							sample,
							"Wrong ancestor: %s, but expected: %s",
							second,
							first);
				}
				if (explicit) {
					return discardSample(sample);
				}
				this.explicitAncestor = null;
				return true;
			}
		}

		for (int i = index + 1; i < this.samples.length; ++i) {

			final Sample s = this.samples[i];

			if (!explicit) {

				final TypeRelation relation =
					sample.getTypeRef().relationTo(s.getTypeRef(), false);

				if (relation.isAscendant()) {
					return discardSample(sample);
				}
				if (relation.isDerivative()) {
					removeSample(i);
				}

				continue;
			}

			final TypeRelation relation =
				s.getTypeRef().relationTo(sample.getTypeRef(), false);

			if (relation.isDerivative()) {
				return discardSample(sample);
			}
			if (relation.isAscendant()) {
				removeSample(i);
			}
		}

		return true;
	}

	private boolean discardSample(Sample sample) {
		this.discardedSamples = ArrayUtil.append(this.discardedSamples, sample);
		return false;
	}

	private void removeSample(int index) {
		discardSample(this.samples[index]);
		this.samples = ArrayUtil.remove(this.samples, index);
	}

	private ConstructionMode enclosingConstructionMode() {

		final Scope enclosingScope =
			getScope().getEnclosingScope();

		if (enclosingScope == null) {
			return ConstructionMode.FULL_CONSTRUCTION;
		}

		return enclosingScope.getConstructionMode();
	}

}
