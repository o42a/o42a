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


public abstract class Derivation {

	private static final int PROPAGATION_MASK = 0x40;
	private static final int IMPLICIT_MASK = 0x80 | PROPAGATION_MASK;

	public static final Derivation SAME = new Derivation(0x10) {

		@Override
		public Derivation traverseSample(Sample sample) {
			if (sample.getOverriddenMember() != null) {
				return MEMBER_OVERRIDE;
			}
			return IMPLICIT_SAMPLE;
		}

		@Override
		public String toString() {
			return "SAME";
		}

	};

	public static final Derivation INHERITANCE = new Derivation(0x20) {

		@Override
		public Derivation traverseSample(Sample sample) {
			return this;
		}

		@Override
		public String toString() {
			return "INHERITANCE";
		}

	};

	public static final Derivation MEMBER_OVERRIDE = new Derivation(
			IMPLICIT_MASK | 0x01) {

		@Override
		public Derivation traverseSample(Sample sample) {
			if (sample.getOverriddenMember() != null) {
				return this;
			}
			return IMPLICIT_PROPAGATION;
		}

		@Override
		public String toString() {
			return "MEMBER_OVERRIDE";
		}

	};

	public static final Derivation IMPLICIT_SAMPLE = new Derivation(
			IMPLICIT_MASK | 0x04) {

		@Override
		public Derivation traverseSample(Sample sample) {
			return IMPLICIT_PROPAGATION;
		}

		@Override
		public String toString() {
			return "IMPLICIT_SAMPLE";
		}

	};

	public static final Derivation IMPLICIT_PROPAGATION = new Derivation(
			IMPLICIT_MASK) {

		@Override
		public Derivation traverseSample(Sample sample) {
			return IMPLICIT_PROPAGATION;
		}

		@Override
		public String toString() {
			return "IMPLICIT_PROPAGATION";
		}

	};

	public static final Derivation PROPAGATION = new Derivation(
			PROPAGATION_MASK) {

		@Override
		public Derivation traverseSample(Sample sample) {
			return this;
		}

		@Override
		public String toString() {
			return "PROPAGATION";
		}

	};

	private static final Derivation[] ATOMS = {
		SAME,
		INHERITANCE,
		MEMBER_OVERRIDE,
		IMPLICIT_SAMPLE,
		IMPLICIT_PROPAGATION,
		PROPAGATION,
	};

	final int mask;

	Derivation(int mask) {
		this.mask = mask;
	}

	@Override
	public int hashCode() {
		return this.mask;
	}

	public abstract Derivation traverseSample(Sample sample);

	public final Derivation union(Derivation other) {

		final int mask = this.mask | other.mask;

		if (mask == this.mask) {
			return this;
		}
		if (mask == other.mask) {
			return other;
		}

		return new Derivations(mask);
	}

	public final boolean is(Derivation other) {
		return (this.mask & other.mask) == other.mask;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Derivation)) {
			return false;
		}

		final Derivation other = (Derivation) obj;

		return this.mask != other.mask;
	}

	private static final class Derivations extends Derivation {

		Derivations(int mask) {
			super(mask);
			assert mask != 0 :
				"Incorrect derivation";
		}

		@Override
		public Derivation traverseSample(Sample sample) {

			Derivation derivation = null;
			int mask = 0;

			for (Derivation atom : ATOMS) {
				if (!is(atom)) {
					continue;
				}
				if (derivation == null) {
					derivation = atom.traverseSample(sample);
					continue;
				}
				derivation = derivation.union(atom.traverseSample(sample));
				mask |= atom.mask;
				if (mask == this.mask) {
					break;
				}
			}

			return derivation;
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();
			int mask = 0;
			boolean comma = false;

			out.append("Derivation[");

			for (Derivation atom : ATOMS) {
				if (!is(atom)) {
					continue;
				}
				if (comma) {
					out.append(',');
				} else {
					comma = true;
				}
				out.append(atom);
				mask |= atom.mask;
				if (this.mask == mask) {
					break;
				}
			}
			out.append(']');

			return out.toString();
		}

	}

}
