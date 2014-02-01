/*
    Abstract Syntax Tree
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.ast.file;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.AbstractStatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class SubTitleNode extends AbstractStatementNode {

	private final SignNode<DoubleLine> prefix;
	private final MemberRefNode tag;
	private final SignNode<DoubleLine> suffix;

	public SubTitleNode(
			SignNode<DoubleLine> prefix,
			MemberRefNode tag,
			SignNode<DoubleLine> suffix) {
		super(prefix.getStart(), end(prefix, tag, suffix));
		this.prefix = prefix;
		this.tag = tag;
		this.suffix = suffix;
	}

	public final SignNode<DoubleLine> getPrefix() {
		return this.prefix;
	}

	public final MemberRefNode getTag() {
		return this.tag;
	}

	public final SignNode<DoubleLine> getSuffix() {
		return this.suffix;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitSubTitle(this, p);
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final ExpressionNode toExpression() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		if (this.tag != null) {
			this.tag.printContent(out);
		}
		if (this.suffix != null) {
			this.suffix.printContent(out);
		}
	}

}
