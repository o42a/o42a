/*
    Abstract Syntax Tree
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.ast.expression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.atom.StringNode.Quote;
import org.o42a.ast.clause.ClauseNode;
import org.o42a.ast.clause.ClauseNodeVisitor;


public class TextNode extends AbstractExpressionNode implements ClauseNode {

	private final StringNode[] literals;
	private String text;

	public TextNode(StringNode[] literals) {
		super(literals[0].getStart(), literals[literals.length - 1].getEnd());
		this.literals = literals;
	}

	public final boolean isDoubleQuote() {

		final SignNode<Quote> quotationMark =
				this.literals[0].getOpeningQuotationMark();

		return quotationMark.getType().isDoubleQuote();
	}

	public StringNode[] getLiterals() {
		return this.literals;
	}

	public String getText() {
		if (this.text == null) {
			this.text = constructText();
		}
		return this.text;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitText(this, p);
	}

	@Override
	public <R, P> R accept(ClauseNodeVisitor<R, P> visitor, P p) {
		return visitor.visitText(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		for (int i = 0; i < this.literals.length; ++i) {
			if (i > 0) {
				out.append(' ');
			}
			this.literals[i].printContent(out);
		}
	}

	private String constructText() {
		if (this.literals.length == 1) {
			return this.literals[0].getText();
		}

		final StringBuilder text = new StringBuilder();

		for (StringNode literal : this.literals) {
			text.append(literal.getText());
		}

		return text.toString();
	}

}
