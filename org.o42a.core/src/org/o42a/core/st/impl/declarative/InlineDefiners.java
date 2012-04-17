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
package org.o42a.core.st.impl.declarative;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.Definer;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.value.ValueStruct;


final class InlineDefiners {

	static InlineDefiners inlineDefiners(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin,
			Declaratives declaratives) {

		final List<Definer> definers = declaratives.getImplications();
		final InlineValue[] inlines = new InlineValue[definers.size()];
		int i = 0;

		for (Definer definer : definers) {

			final InlineValue inline = definer.inline(
					normalizer,
					valueStruct,
					origin);

			if (inline == null) {
				normalizer.cancelAll();
			}

			inlines[i++] = inline;
		}

		if (normalizer.isCancelled()) {
			return null;
		}

		return new InlineDefiners(inlines);
	}

	private final InlineValue[] definers;

	private InlineDefiners(InlineValue[] definers) {
		this.definers = definers;
	}

	public final InlineValue get(int index) {
		return this.definers[index];
	}

	@Override
	public String toString() {
		if (this.definers == null) {
			return super.toString();
		}
		if (this.definers.length == 0) {
			return "";
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.definers[0]);
		for (int i = 1; i < this.definers.length; ++i) {
			out.append(", ").append(this.definers[i]);
		}

		return out.toString();
	}

}
