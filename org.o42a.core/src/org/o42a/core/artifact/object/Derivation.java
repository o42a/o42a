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


public enum Derivation {

	SAME() {

		@Override
		Derivation[] implied() {
			return IMPLIED_NONE;
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

	},

	INHERITANCE() {

		@Override
		Derivation[] implied() {
			return IMPLIED_NONE;
		}

		@Override
		Derivation traverseSample(Sample sample) {
			return this;
		}

	},

	MEMBER_OVERRIDE() {

		@Override
		Derivation[] implied() {
			return IMPLIED_BY_IMPLICIT;
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

	},

	EXPLICIT_SAMPLE() {

		@Override
		Derivation[] implied() {
			return IMPLIED_BY_SAMPLE;
		}

		@Override
		Derivation traverseSample(Sample sample) {
			return PROPAGATION;
		}

	},

	IMPLICIT_SAMPLE() {

		@Override
		Derivation[] implied() {
			return IMPLIED_BY_IMPLICIT;
		}

		@Override
		Derivation traverseSample(Sample sample) {
			if (sample.isExplicit()) {
				return PROPAGATION;
			}
			return IMPLICIT_PROPAGATION;
		}

	},

	IMPLICIT_PROPAGATION() {

		@Override
		Derivation[] implied() {
			return IMPLIED_BY_SAMPLE;
		}

		@Override
		Derivation traverseSample(Sample sample) {
			if (sample.isExplicit()) {
				return PROPAGATION;
			}
			return IMPLICIT_PROPAGATION;
		}

	},

	PROPAGATION() {

		@Override
		Derivation[] implied() {
			return IMPLIED_NONE;
		}

		@Override
		Derivation traverseSample(Sample sample) {
			return this;
		}

	};

	private static final Derivation[] IMPLIED_NONE = new Derivation[0];

	private static final Derivation[] IMPLIED_BY_IMPLICIT = {
		IMPLICIT_PROPAGATION,
		PROPAGATION,
	};

	private static final Derivation[] IMPLIED_BY_SAMPLE = {
		PROPAGATION,
	};

	boolean match(ObjectType type, ObjectType ascendant) {
		return type.getObject() == ascendant.getObject();
	}

	abstract Derivation[] implied();

	abstract Derivation traverseSample(Sample sample);

}
