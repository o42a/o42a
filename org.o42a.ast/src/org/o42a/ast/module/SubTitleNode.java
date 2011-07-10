/*
    Abstract Syntax Tree
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.ast.module;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.AbstractStatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class SubTitleNode extends AbstractStatementNode {

	private final SignNode<DoubleLine> prefix;
	private final MemberRefNode label;
	private final SignNode<DoubleLine> suffix;

	public SubTitleNode(
			SignNode<DoubleLine> prefix,
			MemberRefNode label,
			SignNode<DoubleLine> suffix) {
		super(prefix.getStart(), end(prefix, label, suffix));
		this.prefix = prefix;
		this.label = label;
		this.suffix = suffix;
	}

	public final SignNode<DoubleLine> getPrefix() {
		return this.prefix;
	}

	public final MemberRefNode getLabel() {
		return this.label;
	}

	public final SignNode<DoubleLine> getSuffix() {
		return this.suffix;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitSubTitle(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		if (this.label != null) {
			this.label.printContent(out);
		}
		if (this.suffix != null) {
			this.suffix.printContent(out);
		}
	}

}
