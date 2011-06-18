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


public abstract class Derivation {

	private static final int PROPAGATION_MASK = 0x40;
	private static final int IMPLICIT_MASK = 0x80 | PROPAGATION_MASK;

	public static final Derivation SAME = new Derivation(0x10) {

		@Override
		public String toString() {
			return "SAME";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			if (sample.isExplicit()) {
				return EXPLICIT_SAMPLE;
			}
			if (sample.getOverriddenMember() != null) {
				return MEMBER_OVERRIDE;
			}
			return IMPLICIT_SAMPLE;
		}

	};

	public static final Derivation INHERITANCE = new Derivation(0x20) {

		@Override
		public String toString() {
			return "INHERITANCE";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			return this;
		}

	};

	public static final Derivation MEMBER_OVERRIDE =
		new Derivation(IMPLICIT_MASK | 0x01) {

		@Override
		public String toString() {
			return "MEMBER_OVERRIDE";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			if (sample.isExplicit()) {
				return PROPAGATION;
			}
			if (sample.getOverriddenMember() != null) {
				return this;
			}
			return IMPLICIT_PROPAGATION;
		}

	};

	public static final Derivation IMPLICIT_SAMPLE =
		new Derivation(IMPLICIT_MASK | 0x04) {

		@Override
		public String toString() {
			return "IMPLICIT_SAMPLE";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			if (sample.isExplicit()) {
				return PROPAGATION;
			}
			return IMPLICIT_PROPAGATION;
		}

	};

	public static final Derivation EXPLICIT_SAMPLE =
		new Derivation(PROPAGATION_MASK | 0x02) {

		@Override
		public String toString() {
			return "EXPLICIT_SAMPLE";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			return PROPAGATION;
		}

	};

	public static final Derivation IMPLICIT_PROPAGATION =
		new Derivation(IMPLICIT_MASK) {

		@Override
		public String toString() {
			return "IMPLICIT_PROPAGATION";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			if (sample.isExplicit()) {
				return PROPAGATION;
			}
			return IMPLICIT_PROPAGATION;
		}

	};

	public static final Derivation PROPAGATION =
		new Derivation(PROPAGATION_MASK) {

		@Override
		public String toString() {
			return "PROPAGATION";
		}

		@Override
		Derivation traverseSample(Sample sample) {
			return this;
		}

	};

	private static final Derivation[] ATOMS = {
		SAME,
		INHERITANCE,
		MEMBER_OVERRIDE,
		IMPLICIT_SAMPLE,
		EXPLICIT_SAMPLE,
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

	abstract Derivation traverseSample(Sample sample);

	final Derivation union(Derivation other) {

		final int mask = this.mask | other.mask;

		if (mask == this.mask) {
			return this;
		}
		if (mask == other.mask) {
			return other;
		}

		return new Derivations(mask);
	}

	final boolean is(Derivation other) {
		return (this.mask & other.mask) == other.mask;
	}

	private static final class Derivations extends Derivation {

		Derivations(int mask) {
			super(mask);
			assert mask != 0 :
				"Incorrect derivation";
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

		@Override
		Derivation traverseSample(Sample sample) {

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

	}

}
