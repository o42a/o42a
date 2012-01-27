/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import static org.o42a.util.Cancellation.cancelAll;
import static org.o42a.util.Cancellation.cancelUpToNull;

import java.util.List;

import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.Definer;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Cancelable;


public class InlineStatements implements Cancelable {

	public static InlineStatements inlineStatements(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Imperatives imperatives) {

		final List<Definer> definers = imperatives.getDefiners();
		final InlineValue[] inlines = new InlineValue[definers.size()];
		int i = 0;

		for (Definer definer : definers) {

			final InlineValue inline = definer.getStatement().inlineImperative(
					normalizer,
					valueStruct);

			if (inline == null) {
				cancelUpToNull(inlines);
				return null;
			}

			inlines[i++] = inline;
		}

		return new InlineStatements(inlines);
	}

	private final InlineValue[] statements;

	InlineStatements(InlineValue[] statements) {
		this.statements = statements;
	}

	public final InlineValue get(int index) {
		return this.statements[index];
	}

	@Override
	public void cancel() {
		cancelAll(this.statements);
	}

}
