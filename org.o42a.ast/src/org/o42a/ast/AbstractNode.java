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
package org.o42a.ast;


import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.util.io.Source;
import org.o42a.util.log.LogReason;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableVisitor;


public abstract class AbstractNode implements Node, Cloneable {

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

	public static final FixedPosition start(Node... nodes) {
		return firstNode(nodes).getStart();
	}

	public static final FixedPosition end(Node... nodes) {
		return lastNode(nodes).getEnd();
	}

	private static final CommentNode[] NO_COMMENTS = new CommentNode[0];

	private final FixedPosition start;
	private final FixedPosition end;
	private CommentNode[] comments = NO_COMMENTS;
	private LogReason reason;

	public AbstractNode(Position start, Position end) {
		this.start = start.fix();
		this.end = end.fix();
	}

	public AbstractNode(Node...nodes) {
		this(start(nodes), end(nodes));
	}

	@Override
	public FixedPosition getStart() {
		return this.start;
	}

	@Override
	public FixedPosition getEnd() {
		return this.end;
	}

	@Override
	public CommentNode[] getComments() {
		return this.comments;
	}

	@Override
	public Loggable getLoggable() {
		return this;
	}

	@Override
	public LogReason getReason() {
		return this.reason;
	}

	@Override
	public AbstractNode setReason(LogReason reason) {
		if (reason == null) {
			return this;
		}

		final AbstractNode clone = clone();

		if (this.reason == null) {
			clone.reason = reason;
		} else {
			clone.reason = this.reason.setNext(reason);
		}

		return clone;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {
		return visitor.visitRange(this, p);
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
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.end.hashCode();
		result = prime * result + this.start.hashCode();

		return result;
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

		final AbstractNode other = (AbstractNode) obj;

		if (!this.end.equals(other.end)) {
			return false;
		}
		if (!this.start.equals(other.start)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		printNode(out);

		return out.toString();
	}

	@Override
	public void printRange(StringBuilder out) {

		final Source src1 = this.start.source();
		final Source src2 = this.end.source();
		final boolean withSource;

		if (src1 == null) {
			withSource = src2 != null;
		} else {
			withSource = !src1.equals(src2);
		}

		this.start.print(out, true);
		out.append("..");
		this.end.print(out, withSource);
	}

	@Override
	public void printNode(StringBuilder out) {
		out.append(nodeType());
		out.append('[');
		printRange(out);
		out.append("] ");
		printContent(out);
	}

	@Override
	protected AbstractNode clone() {
		try {
			return (AbstractNode) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
