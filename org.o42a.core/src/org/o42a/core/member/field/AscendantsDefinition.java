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
package org.o42a.core.member.field;

import static org.o42a.core.member.field.DefinitionTarget.defaultDefinition;
import static org.o42a.core.member.field.DefinitionTarget.definitionTarget;
import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.impl.AscendantsFieldDefinition;
import org.o42a.core.object.type.AscendantsBuilder;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.ArrayUtil;


public class AscendantsDefinition extends Contained implements Cloneable {

	private static final StaticTypeRef[] NO_SAMPLES = new StaticTypeRef[0];

	private TypeRef ancestor;
	private ObjectTypeParameters typeParameters;
	private StaticTypeRef[] samples = NO_SAMPLES;
	private boolean stateful;

	public AscendantsDefinition(
			LocationInfo location,
			Distributor distributor) {
		super(location, distributor);
	}

	public AscendantsDefinition(
			LocationInfo location,
			Distributor distributor,
			TypeRef ancestor,
			StaticTypeRef... samples) {
		super(location, distributor);
		this.ancestor = ancestor;
		this.samples = samples;
	}

	public final boolean isEmpty() {
		return this.ancestor == null && this.samples.length == 0;
	}

	public final DefinitionTarget getDefinitionTarget() {
		if (isEmpty()) {
			return defaultDefinition();
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {

			final TypeParameters<?> typeParameters = ancestor.getParameters();

			if (!typeParameters.getValueType().isVoid()) {
				return definitionTarget(typeParameters);
			}
		}

		for (StaticTypeRef sample : getSamples()) {

			final TypeParameters<?> typeParameters = sample.getParameters();

			if (!typeParameters.getValueType().isVoid()) {
				return definitionTarget(typeParameters);
			}
		}

		return objectDefinition();

	}

	public final TypeRef getAncestor() {
		return this.ancestor;
	}

	public AscendantsDefinition setAncestor(TypeRef ancestor) {
		if (ancestor != null) {
			ancestor.assertCompatibleScope(this);
		}

		final AscendantsDefinition clone = clone();

		clone.ancestor = ancestor;

		return clone;
	}

	public final ObjectTypeParameters getTypeParameters() {
		return this.typeParameters;
	}

	public final AscendantsDefinition setTypeParameters(
			ObjectTypeParameters typeParameters) {

		final AscendantsDefinition clone = clone();

		clone.typeParameters = typeParameters;

		return clone;
	}

	public final StaticTypeRef[] getSamples() {
		return this.samples;
	}

	public AscendantsDefinition addSample(StaticTypeRef sample) {
		sample.assertCompatibleScope(this);

		final AscendantsDefinition clone = clone();

		clone.samples = ArrayUtil.append(this.samples, sample);

		return clone;
	}

	public AscendantsDefinition addSamples(StaticTypeRef... samples) {
		for (StaticTypeRef sample : samples) {
			sample.assertCompatibleScope(this);
		}

		final AscendantsDefinition clone = clone();

		clone.samples = ArrayUtil.append(this.samples, samples);

		return clone;
	}

	public boolean isStateful() {
		return this.stateful;
	}

	public AscendantsDefinition setStateful(boolean stateful) {
		if (this.stateful == stateful) {
			return this;
		}

		final AscendantsDefinition clone = clone();

		clone.stateful = stateful;

		return clone;
	}

	public StaticTypeRef getAscendant() {
		if (this.samples.length != 0) {
			return this.samples[this.samples.length - 1];
		}
		if (this.ancestor != null) {
			return this.ancestor.toStatic();
		}
		return null;
	}

	public <A extends AscendantsBuilder<A>> A updateAscendants(A ascendants) {

		A result;

		if (this.ancestor == null) {
			result = ascendants;
		} else {
			result = ascendants.setAncestor(this.ancestor);
		}
		if (this.typeParameters != null) {
			result = result.setParameters(this.typeParameters);
		}
		for (StaticTypeRef sample : this.samples) {
			result = result.addExplicitSample(sample);
		}

		return result;
	}

	public final AscendantsDefinition toStatic() {
		return create(
				this,
				distribute(),
				this.ancestor.toStatic(),
				this.samples);
	}

	public AscendantsDefinition prefixWith(PrefixPath prefix) {

		final TypeRef ancestor;

		if (this.ancestor == null) {
			ancestor = null;
		} else {
			ancestor = this.ancestor.prefixWith(prefix);
		}

		final StaticTypeRef[] samples = new StaticTypeRef[this.samples.length];

		for (int i = 0; i < samples.length; ++i) {
			samples[i] = this.samples[i].prefixWith(prefix);
		}

		return create(
				this,
				distributeIn(prefix.getStart().getContainer()),
				ancestor,
				samples);
	}

	public AscendantsDefinition reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final TypeRef ancestor;

		if (this.ancestor == null) {
			ancestor = null;
		} else {
			ancestor = this.ancestor.reproduce(reproducer);
			if (ancestor == null) {
				return null;
			}
		}

		final StaticTypeRef[] samples = new StaticTypeRef[this.samples.length];

		for (int i = 0; i < samples.length; ++i) {

			final StaticTypeRef sample = this.samples[i].reproduce(reproducer);

			if (sample == null) {
				return null;
			}

			samples[i] = sample;
		}

		return create(this, reproducer.distribute(), ancestor, samples);
	}

	public final FieldDefinition fieldDefinition(
			LocationInfo location,
			BlockBuilder definition) {
		return new AscendantsFieldDefinition(
				location,
				distribute(),
				this,
				definition != null
				? definition : emptyBlock(location));
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (this.ancestor != null) {
			out.append(this.ancestor);
		} else {
			out.append('*');
		}
		for (StaticTypeRef sample : this.samples) {
			out.append(" & ").append(sample);
		}

		return out.toString();
	}

	protected AscendantsDefinition create(
			LocationInfo location,
			Distributor distributor,
			TypeRef ancestor,
			StaticTypeRef[] samples) {
		return new AscendantsDefinition(
				location,
				distributor,
				ancestor,
				samples);
	}

	@Override
	protected AscendantsDefinition clone() {
		try {
			return (AscendantsDefinition) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
