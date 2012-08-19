/*
    Abstract Syntax Tree
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
package org.o42a.ast.atom;

import org.o42a.util.io.SourcePosition;


public class CommentNode extends AbstractAtomNode {

	private final SignNode<CommentBound> opening;
	private final SignNode<CommentBound> closing;
	private final String text;

	public CommentNode(
			SignNode<CommentBound> opening,
			String text,
			SignNode<CommentBound> closing) {
		super(opening.getStart(), closing.getEnd());
		this.opening = opening;
		this.text = text;
		this.closing = closing;
	}

	public CommentNode(
			SignNode<CommentBound> opening,
			String text,
			SourcePosition end) {
		super(opening.getStart(), end);
		this.opening = opening;
		this.text = text;
		this.closing = null;
	}

	public final SignNode<CommentBound> getOpening() {
		return this.opening;
	}

	public final String getText() {
		return this.text;
	}

	public final SignNode<CommentBound> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitComment(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.opening.printContent(out);
		out.append(getText());
		if (this.closing != null) {
			this.closing.printContent(out);
		}
	}

}
