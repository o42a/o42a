/*
    Parser
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
package org.o42a.parser.grammar.ref;

import static org.o42a.parser.Grammar.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MacroSubstitutionNode;
import org.o42a.ast.ref.MacroSubstitutionNode.Prefix;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MacroSubstitutionParser implements Parser<MacroSubstitutionNode> {

	public static final MacroSubstitutionParser MACRO_SUBSTITUTION_PARSER =
			new MacroSubstitutionParser();

	private MacroSubstitutionParser() {
	}

	@Override
	public MacroSubstitutionNode parse(ParserContext context) {
		if (context.next() != '#') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<Prefix> prefix = context.acceptComments(
				false,
				new SignNode<MacroSubstitutionNode.Prefix>(
						start,
						context.current().fix(),
						MacroSubstitutionNode.Prefix.HASH));

		final RefNode macro = context.parse(ref());

		if (macro == null) {
			context.getLogger().error(
					"missing_macro",
					prefix,
					"Missing macro reference to substitute");
		}

		return new MacroSubstitutionNode(prefix, macro);
	}

}
