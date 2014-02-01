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
package org.o42a.ast.field;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.MembershipSign;


public class DeclarableAdapterNode
		extends AbstractNode
		implements DeclarableNode {

	private final SignNode<MembershipSign> prefix;
	private final MemberRefNode member;

	public DeclarableAdapterNode(
			SignNode<MembershipSign> prefix,
			MemberRefNode member) {
		super(prefix.getStart(), member.getEnd());
		this.prefix = prefix;
		this.member = member;
	}

	public final SignNode<MembershipSign> getPrefix() {
		return this.prefix;
	}

	public final MemberRefNode getMember() {
		return this.member;
	}

	@Override
	public final <R, P> R accept(DeclarableNodeVisitor<R, P> visitor, P p) {
		return visitor.visitDeclarableAdapter(this, p);
	}

	@Override
	public final <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitDeclarableAdapter(this, p);
	}

	@Override
	public final MemberRefNode toMemberRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		this.member.printContent(out);
	}

}
