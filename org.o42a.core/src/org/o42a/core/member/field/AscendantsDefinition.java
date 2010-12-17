/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.core.*;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Rescoper;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;


public final class AscendantsDefinition extends Placed implements Cloneable {

	private static final StaticTypeRef[] NO_SAMPLES = new StaticTypeRef[0];

	private TypeRef ancestor;
	private StaticTypeRef[] samples = NO_SAMPLES;

	public AscendantsDefinition(
			LocationSpec location,
			Distributor distributor) {
		super(location, distributor);
	}

	private AscendantsDefinition(
			LocationSpec location,
			Distributor distributor,
			TypeRef ancestor,
			StaticTypeRef[] samples) {
		super(location, distributor);
		this.ancestor = ancestor;
		this.samples = samples;
	}

	public final boolean isEmpty() {
		return this.ancestor == null && this.samples.length == 0;
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

	public StaticTypeRef getAscendant() {
		if (this.samples.length != 0) {
			return this.samples[this.samples.length - 1];
		}
		if (this.ancestor != null) {
			return this.ancestor.toStatic();
		}
		return null;
	}

	public Ascendants updateAscendants(Ascendants ascendants) {
		if (this.ancestor != null) {
			ascendants.setAncestor(this.ancestor);
		}
		for (StaticTypeRef sample : this.samples) {
			ascendants.addExplicitSample(sample);
		}
		return ascendants;
	}

	public Ascendants updateAscendants(Scope scope, Ascendants ascendants) {
		if (this.ancestor != null) {
			ascendants.setAncestor(this.ancestor.rescope(scope));
		}
		for (StaticTypeRef sample : this.samples) {
			ascendants.addExplicitSample(sample.rescope(scope));
		}
		return ascendants;
	}

	public AscendantsDefinition rescope(Rescoper rescoper) {

		final TypeRef ancestor;

		if (this.ancestor == null) {
			ancestor = null;
		} else {
			ancestor = this.ancestor.rescope(rescoper);
		}

		final StaticTypeRef[] samples = new StaticTypeRef[this.samples.length];

		for (int i = 0; i < samples.length; ++i) {
			samples[i] = this.samples[i].rescope(rescoper);
		}

		return new AscendantsDefinition(
				this,
				distributeIn(rescoper.getFinalScope().getContainer()),
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

		return new AscendantsDefinition(
				this,
				reproducer.distribute(),
				ancestor,
				samples);
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

	@Override
	protected AscendantsDefinition clone() {
		try {
			return (AscendantsDefinition) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
