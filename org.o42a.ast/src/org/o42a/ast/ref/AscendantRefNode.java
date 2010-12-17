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
package org.o42a.ast.ref;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;


public class AscendantRefNode extends AbstractRefNode {

	private final RefNode overridden;
	private final SignNode<Boundary> prefix;
	private final RefNode type;
	private final SignNode<Boundary> suffix;

	public AscendantRefNode(
			RefNode overridden,
			SignNode<Boundary> prefix,
			RefNode type,
			SignNode<Boundary> suffix) {
		super(start(overridden, prefix), end(prefix, type, suffix));
		this.overridden = overridden;
		this.prefix = prefix;
		this.type = type;
		this.suffix = suffix;
	}

	public final RefNode getOverridden() {
		return this.overridden;
	}

	public final SignNode<Boundary> getPrefix() {
		return this.prefix;
	}

	public final RefNode getType() {
		return this.type;
	}

	public final SignNode<Boundary> getSuffix() {
		return this.suffix;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.overridden != null) {
			this.overridden.printContent(out);
		}
		this.prefix.printContent(out);
		if (this.type != null) {
			this.type.printContent(out);
			this.suffix.printContent(out);
		}
	}

	@Override
	public <R, P> R accept(RefNodeVisitor<R, P> visitor, P p) {
		return visitor.visitAscendantRef(this, p);
	}

	public enum Boundary implements SignType {

		CIRCUMFLEX() {

			@Override
			public String getSign() {
				return "^";
			}

		}

	}

}
