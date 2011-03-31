/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.st.sentence.declarative;

import static org.o42a.core.ref.Logical.disjunction;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Conditions;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.value.ValueType;


public final class SentenceConditions extends Conditions {

	private final DeclarativeSentence sentence;

	public SentenceConditions(DeclarativeSentence sentence) {
		this.sentence = sentence;
	}

	@Override
	public Logical prerequisite(Scope scope) {
		return this.sentence.getInitialConditions().prerequisite(scope);
	}

	@Override
	public Logical precondition(Scope scope) {

		final List<Declaratives> alternatives =
			this.sentence.getAlternatives();
		final int size = alternatives.size();

		if (size <= 1) {
			if (size == 0) {
				return this.sentence.getInitialConditions()
				.precondition(scope);
			}
			return alternatives.get(0).getConditions().fullLogical(scope);
		}

		final Logical[] vars = new Logical[size];

		for (int i = 0; i < size; ++i) {
			vars[i] =
				alternatives.get(i).getConditions().fullLogical(scope);
		}

		return disjunction(this.sentence, this.sentence.getScope(), vars);
	}

	@Override
	public String toString() {
		return "(" + this.sentence + ")?";
	}

	@Override
	protected ValueType<?> expectedType() {
		return this.sentence.getBlock()
		.getInitialConditions().getExpectedType();
	}

}
