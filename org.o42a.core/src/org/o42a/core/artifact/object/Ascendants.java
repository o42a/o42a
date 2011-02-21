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

import java.util.Arrays;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.*;
import org.o42a.core.member.Member;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.util.ArrayUtil;


public final class Ascendants {

	private static final Sample[] NO_SAMPLES = new Sample[0];
	private static final byte EXPLICIT_RUNTIME = 2;

	private final Scope scope;
	private TypeRef explicitAncestor;
	private TypeRef ancestor;
	private Directive directive;
	private Sample[] samples = NO_SAMPLES;
	private Sample[] discardedSamples = NO_SAMPLES;
	private byte runtime;
	private boolean validated;

	public Ascendants(Scope scope) {
		this.scope = scope;
	}

	public Ascendants(Obj object) {
		this.scope = object.getScope();
	}

	public final Scope getScope() {
		return this.scope;
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

	public final Directive getDirective() {
		if (this.directive == null) {

			final TypeRef ancestor = getExplicitAncestor();

			if (ancestor != null) {
				this.directive = ancestor.getType().toDirective();
			}
			if (this.directive == null) {
				this.directive = sampleDirective();
			}
		}

		return this.directive;
	}

	public final Sample[] getSamples() {
		return this.samples;
	}

	public final Sample[] getDiscardedSamples() {
		return this.discardedSamples;
	}

	public Ascendants setAncestor(TypeRef explicitAncestor) {
		getScope().getEnclosingScope().assertDerivedFrom(
				explicitAncestor.getScope());
		this.explicitAncestor =
			explicitAncestor.upgradeScope(getScope().getEnclosingScope());
		return this;
	}

	public boolean isRuntime() {
		if (this.runtime == 0) {

			final TypeRef ancestor = getExplicitAncestor();

			if (ancestor != null && ancestor.getType().isRuntime()) {
				this.runtime = 1;
			} else if (enclosingScopeIsRuntime()) {
				this.runtime = 1;
			} else {
				this.runtime = -1;
			}
		}

		return this.runtime > 0;
	}

	public Ascendants runtime() {
		this.runtime = EXPLICIT_RUNTIME;
		return this;
	}

	public Ascendants addExplicitSample(StaticTypeRef explicitAscendant) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		explicitAscendant.assertCompatible(enclosingScope);

		return addSample(new ExplicitSample(enclosingScope, explicitAscendant));
	}

	public Ascendants addImplicitSample(StaticTypeRef implicitAscendant) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		implicitAscendant.assertCompatible(enclosingScope);

		return addSample(new ImplicitSample(enclosingScope, implicitAscendant));
	}

	public Ascendants addMemberOverride(Member overriddenMember) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		enclosingScope.assertDerivedFrom(overriddenMember.getScope());

		if (overriddenMember.getSubstance().toObject() != null) {
			return addSample(
					new MemberOverride(enclosingScope, overriddenMember));
		}

		throw new UnsupportedOperationException(
				"Can not override non-object field");
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

	void validate() {
		if (this.validated) {
			return;
		}
		this.validated = true;
		if (this.explicitAncestor != null) {
			if (!this.explicitAncestor.validate()) {
				this.explicitAncestor = null;
			} else if (!validateUse(this.explicitAncestor.getArtifact())) {
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
		this.samples = ArrayUtil.prepend(sample, this.samples);
		return this;
	}

	private boolean validateUse(Artifact<?> checkUse) {
		if (checkUse == null) {
			return true;
		}
		return checkUse.accessBy(getScope()).checkPrototypeUse();
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
			if (!validateUse(sample.getType())) {
				return false;
			}
			if (explicitAscendant.getType().isRuntime()) {
				getScope().getLogger().prohibitedRuntimeSample(sample);
				return false;
			}
			if (isRuntime()) {
				getScope().getLogger().prohibitedSampleAtRuntime(sample);
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

	private Directive sampleDirective() {
		for (Sample sample : this.samples) {
			if (sample.isExplicit()) {
				continue;// Directive can not be explicit.
			}

			final Directive directive = sample.toDirective();

			if (directive != null) {
				return directive;
			}
		}

		return null;
	}

	private boolean enclosingScopeIsRuntime() {

		final Container enclosingContainer = getScope().getEnclosingContainer();

		return enclosingContainer != null
		&& enclosingContainer.getScope().isRuntime();
	}

}
