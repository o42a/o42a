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

import static org.o42a.util.func.Cancellation.cancelAll;
import static org.o42a.util.func.Cancellation.cancelUpToNull;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.Definer;
import org.o42a.core.st.InlineCmd;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.func.Cancelable;


final class InlineCommands implements Cancelable {

	static InlineCommands inlineCommands(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin,
			Imperatives imperatives) {

		final List<Definer> definers = imperatives.getDefiners();
		final InlineCmd[] inlines = new InlineCmd[definers.size()];
		int i = 0;

		for (Definer definer : definers) {

			final InlineCmd inline =
					definer.getStatement().inlineImperative(
							normalizer,
							valueStruct,
							origin);
			if (inline == null) {
				cancelUpToNull(inlines);
				return null;
			}

			inlines[i++] = inline;
		}

		return new InlineCommands(inlines);
	}

	private final InlineCmd[] commands;

	private InlineCommands(InlineCmd[] statements) {
		this.commands = statements;
	}

	public final InlineCmd get(int index) {
		return this.commands[index];
	}

	@Override
	public void cancel() {
		cancelAll(this.commands);
	}

	@Override
	public String toString() {
		if (this.commands == null) {
			return super.toString();
		}
		if (this.commands.length == 0) {
			return "";
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.commands[0]);
		for (int i = 1; i < this.commands.length; ++i) {
			out.append(", ").append(this.commands[i]);
		}

		return out.toString();
	}

}
