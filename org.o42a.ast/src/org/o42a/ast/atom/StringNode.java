/*
    Abstract Syntax Tree
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.util.io.SourcePosition;


public class StringNode extends AbstractAtomNode implements ClauseIdNode {

	private final SignNode<StringBound> openingBound;
	private final String text;
	private final SignNode<StringBound> closingBound;

	public StringNode(
			SignNode<StringBound> openingBound,
			String text,
			SignNode<StringBound> closingBound) {
		super(openingBound.getStart(), closingBound.getEnd());
		this.openingBound = openingBound;
		this.text = text;
		this.closingBound = closingBound;
	}

	public StringNode(
			SignNode<StringBound> openingBound,
			String text,
			SourcePosition end) {
		super(openingBound.getStart(), end);
		this.openingBound = openingBound;
		this.text = text;
		this.closingBound = null;
	}

	public final SignNode<StringBound> getOpeningBound() {
		return this.openingBound;
	}

	public final String getText() {
		return this.text;
	}

	public final SignNode<StringBound> getClosingBound() {
		return this.closingBound;
	}

	public final boolean isDoubleQuoted() {
		return getOpeningBound().getType().isDoubleQuoted();
	}

	public final boolean isTextBlock() {
		return getOpeningBound().getType().isBlockBound();
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitString(this, p);
	}

	@Override
	public <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitString(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.openingBound.printContent(out);
		escapeControlChars(out, this.text);
		this.openingBound.printContent(out);
	}

}
