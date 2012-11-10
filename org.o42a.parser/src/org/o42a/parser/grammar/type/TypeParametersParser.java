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
package org.o42a.parser.grammar.type;

import static org.o42a.parser.grammar.type.InterfaceParser.INTERFACE;

import org.o42a.ast.type.InterfaceNode;
import org.o42a.ast.type.TypeNode;
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class TypeParametersParser implements Parser<TypeParametersNode> {

	private final TypeNode type;

	public TypeParametersParser(TypeNode type) {
		this.type = type;
	}

	@Override
	public TypeParametersNode parse(ParserContext context) {
		if (context.next() != '(') {
			return null;
		}

		final InterfaceNode parameters = context.parse(INTERFACE);

		if (parameters == null) {
			return null;
		}

		return new TypeParametersNode(this.type, parameters);
	}

}
