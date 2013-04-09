/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.sentence.Declaratives;


final class InlineDefiners {

	static InlineDefiners inlineDefiners(
			RootNormalizer rootNormalizer,
			Normalizer normalizer,
			Scope origin,
			Declaratives declaratives) {
		throw new UnsupportedOperationException();

		/*final List<Definer> definers = declaratives.getImplications();
		final InlineEval[] inlines = new InlineEval[definers.size()];
		int i = 0;

		for (Definer definer : definers) {

			final InlineEval inline;

			if (normalizer != null) {
				inline = definer.inline(normalizer, origin);
				if (inline == null) {
					normalizer.cancelAll();
				}
			} else {
				inline = definer.normalize(rootNormalizer, origin);
			}

			inlines[i++] = inline;
		}

		if (normalizer != null && normalizer.isCancelled()) {
			return null;
		}

		return new InlineDefiners(inlines);*/
	}

	private final InlineEval[] definers;

	private InlineDefiners(InlineEval[] definers) {
		this.definers = definers;
	}

	public final InlineEval get(int index) {
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
