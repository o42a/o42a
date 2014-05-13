/*
    Abstract Syntax Tree
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.ast.AbstractNode;
import org.o42a.util.io.SourcePosition;


public class CommentNode extends AbstractNode {

	private final SignNode<CommentBound> openingBound;
	private final SignNode<CommentBound> closingBound;
	private final String text;

	public CommentNode(
			SignNode<CommentBound> openingBound,
			String text,
			SignNode<CommentBound> closingBound) {
		super(openingBound.getStart(), closingBound.getEnd());
		this.openingBound = openingBound;
		this.text = text;
		this.closingBound = closingBound;
	}

	public CommentNode(
			SignNode<CommentBound> opening,
			String text,
			SourcePosition end) {
		super(opening.getStart(), end);
		this.openingBound = opening;
		this.text = text;
		this.closingBound = null;
	}

	public final SignNode<CommentBound> getOpeningBound() {
		return this.openingBound;
	}

	public final String getText() {
		return this.text;
	}

	public final SignNode<CommentBound> getClosingBound() {
		return this.closingBound;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.openingBound.printContent(out);
		out.append(getText());
		if (this.closingBound != null) {
			this.closingBound.printContent(out);
		}
	}

}
