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
package org.o42a.ast;

import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;


public abstract class AbstractNode implements Node {

	public static Node lastNode(Node... nodes) {
		if (nodes == null || nodes.length == 0) {
			return null;
		}
		for (int i = nodes.length - 1; i >= 0; --i) {

			final Node node = nodes[i];

			if (node != null) {
				return node;
			}
		}
		return null;
	}

	public static Node firstNode(Node... nodes) {
		if (nodes == null || nodes.length == 0) {
			return null;
		}
		for (Node node : nodes) {
			if (node != null) {
				return node;
			}
		}
		return null;
	}

	public static final SourcePosition start(Node... nodes) {
		return firstNode(nodes).getStart();
	}

	public static final SourcePosition end(Node... nodes) {
		return lastNode(nodes).getEnd();
	}

	private static final CommentNode[] NO_COMMENTS = new CommentNode[0];

	private SourceRange range;
	private CommentNode[] comments = NO_COMMENTS;

	public AbstractNode(SourcePosition start, SourcePosition end) {
		this.range = new SourceRange(start, end);
	}

	public AbstractNode(Node...nodes) {
		this(start(nodes), end(nodes));
	}

	@Override
	public final SourcePosition getStart() {
		return this.range.getStart();
	}

	@Override
	public final SourcePosition getEnd() {
		return this.range.getEnd();
	}

	@Override
	public final CommentNode[] getComments() {
		return this.comments;
	}

	@Override
	public final SourceRange getLoggable() {
		return this.range;
	}

	@Override
	public void addComments(CommentNode... comments) {
		if (comments == null || comments.length == 0) {
			return;
		}
		if (this.comments.length == 0) {
			this.comments = comments;
		} else {

			final CommentNode[] newComments =
					new CommentNode[this.comments.length + comments.length];

			System.arraycopy(
					this.comments,
					0,
					newComments,
					0,
					this.comments.length);
			System.arraycopy(
					comments,
					0,
					newComments,
					this.comments.length,
					comments.length);

			this.comments = newComments;
		}
	}

	@Override
	public final void addComments(SeparatorNodes separators) {
		if (separators == null) {
			return;
		}
		this.comments = separators.appendCommentsTo(this.comments);
	}

	@Override
	public String nodeType() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		printNode(out);

		return out.toString();
	}

	@Override
	public void printNode(StringBuilder out) {
		out.append(nodeType());
		out.append('[');
		getLoggable().print(out);
		out.append("] ");
		printContent(out);
	}

	@Override
	public abstract void printContent(StringBuilder out);

}
