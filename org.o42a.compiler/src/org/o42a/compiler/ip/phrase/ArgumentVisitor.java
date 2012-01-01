/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.TextNode;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.ref.Ref;


final class ArgumentVisitor extends AbstractExpressionVisitor<Phrase, Phrase> {

	public static final ArgumentVisitor ARGUMENT_VISITOR =
			new ArgumentVisitor();

	private ArgumentVisitor() {
	}

	@Override
	public Phrase visitText(TextNode text, Phrase p) {
		if (text.isDoubleQuote()) {
			return super.visitText(text, p);
		}
		return p.string(location(p, text), text.getText()).getPhrase();
	}

	@Override
	protected Phrase visitExpression(ExpressionNode expression, Phrase p) {

		final Ref value =
				expression.accept(p.ip().expressionVisitor(), p.distribute());

		if (value != null) {
			return p.argument(value).getPhrase();
		}

		return p.emptyArgument(location(p, expression)).getPhrase();
	}

}
