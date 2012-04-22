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

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.Command;
import org.o42a.core.st.sentence.Imperatives;


final class InlineCommands {

	static InlineCommands inlineCommands(
			Normalizer normalizer,
			Scope origin,
			Imperatives imperatives) {

		final List<Command> commands = imperatives.getImplications();
		final InlineCmd[] inlines = new InlineCmd[commands.size()];
		int i = 0;

		for (Command command : commands) {

			final InlineCmd inline = command.inline(normalizer, origin);

			if (inline == null) {
				normalizer.cancelAll();
			} else {
				inlines[i++] = inline;
			}
		}

		if (normalizer.isCancelled()) {
			return null;
		}

		return new InlineCommands(inlines);
	}

	private final InlineCmd[] commands;

	private InlineCommands(InlineCmd[] commands) {
		this.commands = commands;
	}

	public final InlineCmd get(int index) {
		return this.commands[index];
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
