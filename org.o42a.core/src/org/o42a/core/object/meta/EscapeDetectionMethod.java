/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.object.meta;

import static org.o42a.core.object.def.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;


enum EscapeDetectionMethod {

	ALWAYS_ESCAPE() {

		@Override
		EscapeDetectionMethod ignoreObjectDefinitions() {
			return this;
		}

		@Override
		EscapeMode overridersEscapeMode(
				Obj object,
				DetectEscapeMode detect) {
			return ESCAPE_POSSIBLE;
		}

		@Override
		EscapeMode derivativesEscapeMode(
				Obj object,
				DetectEscapeMode detect) {
			return ESCAPE_IMPOSSIBLE;
		}

	},

	OBJECT_ESCAPE() {

		@Override
		EscapeDetectionMethod ignoreObjectDefinitions() {
			return this;
		}

		@Override
		EscapeMode overridersEscapeMode(
				Obj object,
				DetectEscapeMode detect) {
			return object.type().eachOverrider(
					ESCAPE_IMPOSSIBLE,
					(d, em) -> {

						final EscapeMode result = em.combine(
								detect.apply(d.getDerivedObject()));

						if (result.isEscapePossible()) {
							d.done();
						}

						return result;
					});
		}

		@Override
		EscapeMode derivativesEscapeMode(
				Obj object,
				DetectEscapeMode detect) {
			return object.type().eachDerivative(
					ESCAPE_IMPOSSIBLE,
					(d, em) -> {

						final EscapeMode result = em.combine(
								detect.apply(d.getDerivedObject()));

						if (result.isEscapePossible()) {
							d.done();
						}

						return result;
					});
		}

	},

	ANCESTOR_ESCAPE() {

		@Override
		EscapeDetectionMethod ignoreObjectDefinitions() {
			return OBJECT_ESCAPE;
		}

		@Override
		EscapeMode overridersEscapeMode(
				Obj object,
				DetectEscapeMode detect) {
			// Yes, derivatives!
			return object.type()
					.getAncestor()
					.getType()
					.analysis()
					.derivativesEscapeMode(detect);
		}

		@Override
		EscapeMode derivativesEscapeMode(
				Obj object,
				DetectEscapeMode detect) {
			return object.type()
					.getAncestor()
					.getType()
					.analysis()
					.derivativesEscapeMode(detect);
		}

	};

	final boolean alwaysEscapes() {
		return this == ALWAYS_ESCAPE;
	}

	abstract EscapeDetectionMethod ignoreObjectDefinitions();

	abstract EscapeMode overridersEscapeMode(
			Obj object,
			DetectEscapeMode detect);

	abstract EscapeMode derivativesEscapeMode(
			Obj object,
			DetectEscapeMode detect);

}
