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
package org.o42a.ast.ref;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.field.DeclarableNode;


public class ParentRefNode extends AbstractRefNode {

	private final NameNode name;
	private final SignNode<Qualifier> qualifier;

	public ParentRefNode(NameNode name, SignNode<Qualifier> qualifier) {
		super(name.getStart(), qualifier.getEnd());
		this.name = name;
		this.qualifier = qualifier;
	}

	public NameNode getName() {
		return this.name;
	}

	public SignNode<Qualifier> getQualifier() {
		return this.qualifier;
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitParentRef(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.name.printContent(out);
		this.qualifier.printContent(out);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final ScopeRefNode toScopeRef() {
		return null;
	}

	@Override
	public final MemberRefNode toMemberRef() {
		return null;
	}

	@Override
	public final AdapterRefNode toAdapterRef() {
		return null;
	}

	public enum Qualifier implements SignType {

		PARENT() {

			@Override
			public String getSign() {
				return "::";
			}

		}

	}

}
