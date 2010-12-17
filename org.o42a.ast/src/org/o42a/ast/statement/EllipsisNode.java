/*
    Abstract Syntax Tree
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.ast.statement;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;


public class EllipsisNode extends AbstractStatementNode {

	private final SignNode<Mark> mark;
	private final NameNode target;

	public EllipsisNode(SignNode<Mark> mark, NameNode target) {
		super(mark.getStart(), end(mark, target));
		this.mark = mark;
		this.target = target;
	}

	public final SignNode<Mark> getMark() {
		return this.mark;
	}

	public final NameNode getTarget() {
		return this.target;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitEllipsis(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.mark.printContent(out);
		if (this.target != null) {
			this.target.printContent(out);
		}
	}

	public enum Mark implements SignType {

		ELLIPSIS() {

			@Override
			public String getSign() {
				return "...";
			}

		}

	}
}
