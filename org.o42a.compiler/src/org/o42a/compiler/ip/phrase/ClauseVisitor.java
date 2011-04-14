/*
    Compiler
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
package org.o42a.compiler.ip.phrase;

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.contentBuilder;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.ArgumentVisitor.ARGUMENT_VISITOR;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;
import org.o42a.compiler.ip.StatementVisitor;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.ref.Ref;


final class ClauseVisitor extends AbstractClauseVisitor<Phrase, Phrase> {

	public static final ClauseVisitor CLAUSE_VISITOR = new ClauseVisitor();

	private ClauseVisitor() {
	}

	@Override
	public Phrase visitName(NameNode name, Phrase p) {
		return p.name(location(p, name), name.getName());
	}

	@Override
	public Phrase visitBraces(BracesNode braces, Phrase p) {
		return p.imperative(contentBuilder(
				new StatementVisitor(p.getContext()),
				braces));
	}

	@Override
	public Phrase visitParentheses(ParenthesesNode parentheses, Phrase p) {
		return p.declarations(contentBuilder(
				new StatementVisitor(p.getContext()),
				parentheses));
	}

	@Override
	public Phrase visitBrackets(BracketsNode brackets, Phrase p) {

		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length == 0) {
			return p.emptyArgument(location(p, brackets));
		}

		for (ArgumentNode arg : arguments) {

			final ExpressionNode value = arg.getValue();

			if (value != null) {
				p = value.accept(ARGUMENT_VISITOR, p);
				continue;
			}
			if (arguments.length == 1) {
				return p.emptyArgument(location(p, brackets));
			}
			p = p.emptyArgument(location(p, arg));
		}

		return p;
	}

	@Override
	public Phrase visitText(TextNode text, Phrase p) {
		if (!text.isDoubleQuote()) {
			return p.string(location(p, text), text.getText());
		}

		final Ref value =
			text.accept(EXPRESSION_VISITOR, p.distribute());

		if (value != null) {
			return p.argument(value);
		}

		return p.emptyArgument(location(p, text));
	}

	@Override
	protected Phrase visitClause(ClauseNode clause, Phrase p) {
		p.getLogger().invalidClause(clause);
		return p;
	}

}
