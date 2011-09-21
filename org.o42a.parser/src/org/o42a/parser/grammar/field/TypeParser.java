/*
    Parser
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
package org.o42a.parser.grammar.field;

import static org.o42a.parser.Grammar.ascendants;
import static org.o42a.parser.Grammar.ref;
import static org.o42a.parser.Grammar.samples;

import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.field.ArrayTypeNode;
import org.o42a.ast.field.TypeNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class TypeParser implements Parser<TypeNode> {

	public static final TypeParser TYPE = new TypeParser();

	private TypeParser() {
	}

	@Override
	public TypeNode parse(ParserContext context) {

		final TypeNode ancestor;

		if (context.next() == '&') {
			ancestor = context.parse(samples());
			if (ancestor == null) {
				context.getLogger().missingType(context.current());
				return null;
			}
		} else {

			final RefNode ref = context.parse(ref());

			if (ref == null) {
				context.getLogger().missingType(context.current());
				return null;
			}

			final AscendantsNode ascendants = context.parse(ascendants(ref));

			ancestor = context.acceptComments(
					true,
					ascendants != null ? ascendants : ref);
		}

		if (context.next() == '[') {

			final ArrayTypeNode itemType =
					context.parse(new ArrayTypeParser(ancestor));

			if (itemType != null) {
				return itemType;
			}
		}

		return ancestor;
	}

}
