/*
    Abstract Syntax Tree
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.ast.atom;

import static org.o42a.util.string.StringCodec.escapeControlChars;

import org.o42a.ast.Position;


public class StringNode extends AbstractAtomNode {

	public static final Quote SINGLE_QUOTE = new Quote("\'", false);
	public static final Quote DOUBLE_QUOTE = new Quote("\"", true);

	public static final Quote MULTILINE_SINGLE_QUOTE =
		new OpeningMultilineQuote("'", false);
	public static final Quote MULTILINE_DOUBLE_QUOTE =
		new OpeningMultilineQuote("\"", true);


	private final SignNode<Quote> openingQuotationMark;
	private final String text;
	private final SignNode<Quote> closingQuotationMark;

	public StringNode(
			SignNode<Quote> openingQuotationMark,
			String text,
			SignNode<Quote> closingQuotationMark) {
		super(openingQuotationMark.getStart(), closingQuotationMark.getEnd());
		this.openingQuotationMark = openingQuotationMark;
		this.text = text;
		this.closingQuotationMark = closingQuotationMark;
	}

	public StringNode(
			SignNode<Quote> openingQuotationMark,
			String text,
			Position end) {
		super(openingQuotationMark.getStart(), end);
		this.openingQuotationMark = openingQuotationMark;
		this.text = text;
		this.closingQuotationMark = null;
	}

	public SignNode<Quote> getOpeningQuotationMark() {
		return this.openingQuotationMark;
	}

	public String getText() {
		return this.text;
	}

	public SignNode<Quote> getClosingQuotationMark() {
		return this.closingQuotationMark;
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitStringLiteral(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.openingQuotationMark.printContent(out);
		escapeControlChars(out, this.text);
		if (this.closingQuotationMark != null) {
			this.closingQuotationMark.printContent(out);
		} else {
			out.append(
					this.openingQuotationMark
					.getType().getClosing().getSign());
		}
	}

	public static class Quote implements SignType {

		private final String sign;
		private final boolean doubleQuote;

		private Quote(String sign, boolean doubleQuote) {
			this.sign = sign;
			this.doubleQuote = doubleQuote;
		}

		@Override
		public final String getSign() {
			return this.sign;
		}

		public final boolean isDoubleQuote() {
			return this.doubleQuote;
		}

		public boolean isMultiline() {
			return false;
		}

		public Quote getClosing() {
			return this;
		}

		@Override
		public String toString() {
			return this.sign;
		}

		@Override
		public int hashCode() {
			return this.sign.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final Quote other = (Quote) obj;

			return this.sign.equals(other.sign);
		}

	}

	private static final class OpeningMultilineQuote extends Quote {

		private final Quote closing;

		OpeningMultilineQuote(String quote, boolean doubleQuote) {
			super('\\' + quote, doubleQuote);
			this.closing = new ClosingMultilineQuote(quote, doubleQuote);
		}

		@Override
		public boolean isMultiline() {
			return true;
		}

		@Override
		public Quote getClosing() {
			return this.closing;
		}

	}

	private static final class ClosingMultilineQuote extends Quote {

		ClosingMultilineQuote(String quote, boolean doubleQuote) {
			super(quote + '\\', doubleQuote);
		}

		@Override
		public boolean isMultiline() {
			return true;
		}

		@Override
		public Quote getClosing() {
			return null;
		}

	}

}
