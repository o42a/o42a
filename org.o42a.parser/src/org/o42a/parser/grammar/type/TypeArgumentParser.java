/*
    Parser
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.parser.grammar.type;

import static org.o42a.ast.type.TypeArgumentNode.TypeArgumentSeparator.BACKQUOTE;
import static org.o42a.parser.Grammar.type;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.type.TypeArgumentNode;
import org.o42a.ast.type.TypeArgumentNode.TypeArgumentSeparator;
import org.o42a.ast.type.TypeNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class TypeArgumentParser implements Parser<TypeArgumentNode> {

	private final TypeNode argument;

	public TypeArgumentParser(TypeNode argument) {
		this.argument = argument;
	}

	@Override
	public TypeArgumentNode parse(ParserContext context) {
		if (context.next() != '`') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<TypeArgumentSeparator> separator =
				new SignNode<>(start, context.current().fix(), BACKQUOTE);

		final TypeNode type = context.parse(type());

		if (type == null) {
			return null;
		}

		return new TypeArgumentNode(this.argument, separator, type);
	}

}
