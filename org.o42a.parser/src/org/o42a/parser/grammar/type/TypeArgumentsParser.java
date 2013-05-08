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

import static org.o42a.ast.type.TypeArgumentSuffix.BACKQUOTE;
import static org.o42a.parser.Grammar.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.ArrayUtil;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.log.LogInfo;


public class TypeArgumentsParser implements Parser<TypeArgumentsNode> {

	private static final TypeArgumentParser TYPE_ARGUMENT =
			new TypeArgumentParser();

	private final TypeArgumentNode argument;

	public TypeArgumentsParser(TypeArgumentNode argument) {
		this.argument = argument;
	}

	@Override
	public TypeArgumentsNode parse(ParserContext context) {

		TypeArgumentNode argument = this.argument;
		TypeArgNode args[] = null;

		for (;;) {

			final SignNode<TypeArgumentSuffix> suffix = parseSuffix(context);

			if (suffix == null) {
				break;
			}
			args = addArgument(args, argument, suffix);

			argument = context.parse(TYPE_ARGUMENT);
			if (argument == null) {
				missingType(context, suffix);
				break;
			}

			final AscendantsNode ascendants =
					parseAscendants(context, argument);

			if (ascendants != null) {
				argument = ascendants;
				break;
			}
		}
		if (args == null) {
			return null;
		}

		return new TypeArgumentsNode(args, argument);
	}

	private SignNode<TypeArgumentSuffix> parseSuffix(ParserContext context) {
		if (context.next() != '`') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.acceptAll();

		final SignNode<TypeArgumentSuffix> suffix =
				new SignNode<>(start, context.current().fix(), BACKQUOTE);

		return context.acceptComments(false, suffix);
	}

	private AscendantsNode parseAscendants(
			ParserContext context,
			TypeArgumentNode arg) {

		final RefNode ref = arg.toRef();

		if (ref == null) {
			return null;
		}

		return context.parse(ascendants(ref));
	}

	private static TypeArgNode[] addArgument(
			TypeArgNode[] args,
			TypeArgumentNode argument,
			SignNode<TypeArgumentSuffix> suffix) {

		final TypeArgNode arg = new TypeArgNode(argument, suffix);

		if (args == null) {
			return new TypeArgNode[] {arg};
		}

		return ArrayUtil.append(args, arg);
	}

	private static void missingType(
			ParserContext context,
			LogInfo location) {
		context.getLogger().error(
				"missing_type",
				location,
				"Type is missing");
	}

	private static final class TypeArgumentParser
			implements Parser<TypeArgumentNode> {

		@Override
		public TypeArgumentNode parse(ParserContext context) {

			final int c = context.next();

			switch (c) {
			case '&':
				return context.parse(samples());
			case '(':
				return context.parse(DECLARATIVE.parentheses());
			default:
				return context.parse(ref());
			}
		}

	}

}
