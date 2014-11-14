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

import org.o42a.core.object.Obj;


enum EscapeDetectionMethod {

	ALWAYS_ESCAPE() {

		@Override
		EscapeDetectionMethod ignoreObjectDefinitions() {
			return this;
		}

		@Override
		EscapeFlag overridersEscapeFlag(
				EscapeAnalyzer analyzer,
				Obj object,
				DetectEscapeFlag detect) {
			return analyzer.escapePossible();
		}

		@Override
		EscapeFlag derivativesEscapeFlag(
				EscapeAnalyzer analyzer,
				Obj object,
				DetectEscapeFlag detect) {
			return analyzer.escapePossible();
		}

	},

	OBJECT_ESCAPE() {

		@Override
		EscapeDetectionMethod ignoreObjectDefinitions() {
			return this;
		}

		@Override
		EscapeFlag overridersEscapeFlag(
				EscapeAnalyzer analyzer,
				Obj object,
				DetectEscapeFlag detect) {
			return object.type().eachOverrider(
					analyzer.escapeImpossible(),
					(d, ef) -> {

						final EscapeFlag result =
								detect.apply(analyzer, d.getDerivedObject());

						if (!result.isEscapeImpossible()) {
							d.done();
						}

						return result;
					});
		}

		@Override
		EscapeFlag derivativesEscapeFlag(
				EscapeAnalyzer analyzer,
				Obj object,
				DetectEscapeFlag detect) {
			return object.type().eachDerivative(
					analyzer.escapeImpossible(),
					(d, ef) -> {

						final EscapeFlag result =
								detect.apply(analyzer, d.getDerivedObject());

						if (!result.isEscapeImpossible()) {
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
		EscapeFlag overridersEscapeFlag(
				EscapeAnalyzer analyzer,
				Obj object,
				DetectEscapeFlag detect) {
			// Yes, derivatives!
			return object.type()
					.getAncestor()
					.getType()
					.analysis()
					.derivativesEscapeFlag(analyzer, detect);
		}

		@Override
		EscapeFlag derivativesEscapeFlag(
				EscapeAnalyzer analyzer,
				Obj object,
				DetectEscapeFlag detect) {
			return object.type()
					.getAncestor()
					.getType()
					.analysis()
					.derivativesEscapeFlag(analyzer, detect);
		}

	};

	final boolean alwaysEscapes() {
		return this == ALWAYS_ESCAPE;
	}

	abstract EscapeDetectionMethod ignoreObjectDefinitions();

	abstract EscapeFlag overridersEscapeFlag(
			EscapeAnalyzer analyzer,
			Obj object,
			DetectEscapeFlag detect);

	abstract EscapeFlag derivativesEscapeFlag(
			EscapeAnalyzer analyzer,
			Obj object,
			DetectEscapeFlag detect);

}
