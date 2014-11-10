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

import java.util.function.Function;

import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;


enum EscapeDetectionMethod {

	ALWAYS_ESCAPE() {

		@Override
		EscapeMode overridersEscapeMode(
				Obj object,
				Function<Obj, EscapeMode> f) {
			return ESCAPE_POSSIBLE;
		}

		@Override
		EscapeMode derivativesEscapeMode(
				Obj object,
				Function<Obj, EscapeMode> f) {
			return ESCAPE_IMPOSSIBLE;
		}

	},

	OBJECT_ESCAPE() {

		@Override
		EscapeMode overridersEscapeMode(
				Obj object,
				Function<Obj, EscapeMode> f) {
			return object.type().eachOverrider(
					ESCAPE_IMPOSSIBLE,
					(d, em) -> {

						final EscapeMode result = em.combine(
								f.apply(d.getDerivedObject()));

						if (result.isEscapePossible()) {
							d.done();
						}

						return result;
					});
		}

		@Override
		EscapeMode derivativesEscapeMode(
				Obj object,
				Function<Obj, EscapeMode> f) {
			return object.type().eachDerivative(
					ESCAPE_IMPOSSIBLE,
					(d, em) -> {

						final EscapeMode result = em.combine(
								f.apply(d.getDerivedObject()));

						if (result.isEscapePossible()) {
							d.done();
						}

						return result;
					});
		}

	},

	ANCESTOR_ESCAPE() {

		@Override
		EscapeMode overridersEscapeMode(
				Obj object,
				Function<Obj, EscapeMode> f) {
			// Yes, derivatives!
			return object.type()
					.getAncestor()
					.getType()
					.analysis()
					.derivativesEscapeMode(f);
		}

		@Override
		EscapeMode derivativesEscapeMode(
				Obj object,
				Function<Obj, EscapeMode> f) {
			return object.type()
					.getAncestor()
					.getType()
					.analysis()
					.derivativesEscapeMode(f);
		}

	};

	final boolean alwaysEscapes() {
		return this == ALWAYS_ESCAPE;
	}

	abstract EscapeMode overridersEscapeMode(
			Obj object,
			Function<Obj, EscapeMode> f);

	abstract EscapeMode derivativesEscapeMode(
			Obj object,
			Function<Obj, EscapeMode> f);

}
