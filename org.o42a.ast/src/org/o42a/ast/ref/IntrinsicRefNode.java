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
package org.o42a.ast.ref;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;


public class IntrinsicRefNode extends AbstractRefNode {

	private final SignNode<Boundary> prefix;
	private final NameNode name;
	private final SignNode<Boundary> suffix;

	public IntrinsicRefNode(
			SignNode<Boundary> prefix,
			NameNode name,
			SignNode<Boundary> suffix) {
		super(prefix.getStart(), suffix.getEnd());
		this.prefix = prefix;
		this.name = name;
		this.suffix = suffix;
	}

	public final SignNode<Boundary> getPrefix() {
		return this.prefix;
	}

	public final NameNode getName() {
		return this.name;
	}

	public final SignNode<Boundary> getSuffix() {
		return this.suffix;
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitIntrinsicRef(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.name.printContent(out);
		this.suffix.printContent(out);
	}

	public enum Boundary implements SignType {

		DOLLAR() {

			@Override
			public String getSign() {
				return "$";
			}

		}

	}

}
