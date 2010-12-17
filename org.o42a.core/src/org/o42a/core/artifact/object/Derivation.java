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
package org.o42a.core.artifact.object;


public enum Derivation {

	NONE() {

		@Override
		boolean match(Obj object, Obj ascendant) {
			return false;
		}

		@Override
		boolean acceptAncestor() {
			return false;
		}

		@Override
		boolean acceptsSamples() {
			return false;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return false;
		}

	},

	SAME() {

		@Override
		boolean acceptAncestor() {
			return false;
		}

		@Override
		boolean acceptsSamples() {
			return false;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return false;
		}

	},

	INHERITANCE() {

		@Override
		boolean acceptAncestor() {
			return true;
		}

		@Override
		boolean acceptsSamples() {
			return false;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return false;
		}

	},

	MEMBER_OVERRIDE() {

		@Override
		boolean acceptAncestor() {
			return false;
		}

		@Override
		boolean acceptsSamples() {
			return true;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return sample.getOverriddenMember() != null;
		}

	},

	EXPLICIT_SAMPLE() {

		@Override
		boolean acceptAncestor() {
			return false;
		}

		@Override
		boolean acceptsSamples() {
			return true;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return sample.getExplicitAscendant() != null;
		}

	},

	IMPLICIT_SAMPLE() {

		@Override
		boolean acceptAncestor() {
			return false;
		}

		@Override
		boolean acceptsSamples() {
			return true;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return sample.getOverriddenMember() == null
			&& sample.getExplicitAscendant() == null;
		}

	},

	PROPAGATION() {

		@Override
		boolean acceptAncestor() {
			return false;
		}

		@Override
		boolean acceptsSamples() {
			return true;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return true;
		}

	},

	ANY() {

		@Override
		boolean acceptAncestor() {
			return true;
		}

		@Override
		boolean acceptsSamples() {
			return true;
		}

		@Override
		boolean acceptSample(Sample sample) {
			return true;
		}

	};

	boolean match(Obj object, Obj ascendant) {
		return object == ascendant;
	}

	abstract boolean acceptAncestor();

	abstract boolean acceptsSamples();

	abstract boolean acceptSample(Sample sample);

}
