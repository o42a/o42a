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
package org.o42a.ast.atom;

import static org.o42a.util.string.StringCodec.escapeControlChars;

import org.o42a.util.io.SourcePosition;


public class CommentNode extends AbstractAtomNode {

	public static CommentNode[] appendComments(
			CommentNode[] original,
			CommentNode... comments) {
		if (original == null || original.length == 0) {
			return comments;
		}
		if (comments == null || comments.length == 0) {
			return original;
		}

		final CommentNode[] result = new CommentNode[original.length + comments.length];

		System.arraycopy(original, 0, result, 0, original.length);
		System.arraycopy(comments, 0, result, original.length, comments.length);

		return result;
	}

	private final boolean multiline;
	private final String text;

	public CommentNode(
			SourcePosition start,
			SourcePosition end,
			boolean multiline,
			String text) {
		super(start, end);
		this.multiline = multiline;
		this.text = text;
	}

	public boolean isMultiline() {
		return this.multiline;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public <R, P> R accept(AtomNodeVisitor<R, P> visitor, P p) {
		return visitor.visitComment(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append(this.multiline ? "/*" : "//");
		escapeControlChars(out, this.text);
		if (this.multiline) {
			out.append("*/");
		}
	}

}
