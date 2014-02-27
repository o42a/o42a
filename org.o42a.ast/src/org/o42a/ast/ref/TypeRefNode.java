/*
    Abstract Syntax Tree
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.ast.AbstractNode;
import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;


public abstract class TypeRefNode<S extends SignType> extends AbstractNode {

	private final SignNode<S> prefix;
	private final SignNode<ParenthesisSign> opening;
	private final RefNode type;
	private final SignNode<ParenthesisSign> closing;

	public TypeRefNode(
			SignNode<S> prefix,
			SignNode<ParenthesisSign> opening,
			RefNode type,
			SignNode<ParenthesisSign> closing) {
		super(prefix.getStart(), end(prefix, opening, type, closing));
		this.prefix = prefix;
		this.opening = opening;
		this.type = type;
		this.closing = closing;
	}

	public final SignNode<S> getPrefix() {
		return this.prefix;
	}

	public final SignNode<ParenthesisSign> getOpening() {
		return this.opening;
	}

	public final RefNode getType() {
		return this.type;
	}

	public final SignNode<ParenthesisSign> getClosing() {
		return this.closing;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		if (this.type != null) {
			out.append('(');
			this.type.printContent(out);
			out.append(')');
		}
	}

}
