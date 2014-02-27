/*
    Abstract Syntax Tree
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.ast.sentence;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;


public class ContinuationNode extends AbstractNode {

	private final NameNode label;
	private final SignNode<SentenceType> period;

	public ContinuationNode(NameNode label, SignNode<SentenceType> period) {
		super(label.getStart(), end(label, period));
		this.label = label;
		this.period = period;
	}

	public final NameNode getLabel() {
		return this.label;
	}

	public final SignNode<SentenceType> getPeriod() {
		return this.period;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.label.printContent(out);
		if (this.period != null) {
			this.period.printContent(out);
		}
	}

}
