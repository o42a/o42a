/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.ast.expression.*;


final class ArgumentVisitor
		extends AbstractExpressionVisitor<PhraseBuilder, PhraseBuilder> {

	public static final ArgumentVisitor ARGUMENT_VISITOR =
			new ArgumentVisitor();

	private ArgumentVisitor() {
	}

	@Override
	public PhraseBuilder visitBrackets(BracketsNode brackets, PhraseBuilder p) {
		return p.array(brackets);
	}

	@Override
	public PhraseBuilder visitText(TextNode text, PhraseBuilder p) {
		if (text.isDoubleQuoted()) {
			return super.visitText(text, p);
		}
		return p.string(text);
	}

	@Override
	protected PhraseBuilder visitExpression(
			ExpressionNode expression,
			PhraseBuilder p) {
		return p.argument(
				expression.accept(
						p.ip().targetBuildVisitor(),
						p.distributeAccess()));
	}

}
