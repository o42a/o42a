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
package org.o42a.ast.type;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.ref.RefNode;


public class AscendantNode extends AbstractNode {

	private final SignNode<Separator> separator;
	private final RefNode spec;

	public AscendantNode(
			SignNode<Separator> separator,
			RefNode spec) {
		super(separator, spec);
		this.separator = separator;
		this.spec = spec;
	}

	public final SignNode<Separator> getSeparator() {
		return this.separator;
	}

	public final RefNode getSpec() {
		return this.spec;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitSample(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.separator != null) {
			this.separator.printContent(out);
		}
		if (this.spec != null) {
			this.spec.printContent(out);
		}
	}

	public enum Separator implements SignType {

		SAMPLE() {
			@Override
			public String getSign() {
				return "&";
			}
		}

	}

}
