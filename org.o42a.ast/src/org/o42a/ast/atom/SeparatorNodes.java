/*
    Abstract Syntax Tree
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
package org.o42a.ast.atom;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;

import org.o42a.util.ArrayUtil;


public final class SeparatorNodes {

	private final List<CommentNode> comments;
	private final boolean continuation;

	public SeparatorNodes(boolean continuation, List<CommentNode> comments) {
		this.comments = comments;
		this.continuation = continuation;
	}

	public SeparatorNodes(boolean continuation) {
		this.comments = emptyList();
		this.continuation = continuation;
	}

	public final boolean lineContinuation() {
		return this.continuation;
	}

	public final boolean haveComments() {
		return !this.comments.isEmpty();
	}

	public final CommentNode[] getComments() {
		return this.comments.toArray(new CommentNode[this.comments.size()]);
	}

	public final CommentNode[] appendCommentsTo(CommentNode[] comments) {
		return ArrayUtil.append(comments, this.comments);
	}

	public final void appendCommentsTo(Collection<CommentNode> comments) {
		comments.addAll(this.comments);
	}

	public void printContent(StringBuilder out) {

		boolean separate = false;

		if (this.continuation) {
			out.append('_');
			separate = true;
		}
		for (CommentNode comment : this.comments) {
			if (separate) {
				out.append(' ');
			} else {
				separate = true;
			}
			comment.printContent(out);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		printContent(out);

		return out.toString();
	}

}
