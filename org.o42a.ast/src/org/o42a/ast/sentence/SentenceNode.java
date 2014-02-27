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
package org.o42a.ast.sentence;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.atom.SignNode;


public class SentenceNode extends AbstractNode {

	private final AlternativeNode[] disjunction;
	private final SignNode<SentenceType> mark;

	public SentenceNode(
			AlternativeNode[] disjunction,
			SignNode<SentenceType> mark) {
		super(firstNode(disjunction), lastNode(disjunction), mark);
		this.disjunction = disjunction;
		this.mark = mark;
	}

	public final SentenceType getType() {
		if (this.mark == null) {
			return SentenceType.DECLARATION;
		}
		return this.mark.getType();
	}

	public AlternativeNode[] getDisjunction() {
		return this.disjunction;
	}

	public SignNode<SentenceType> getMark() {
		return this.mark;
	}

	@Override
	public void printContent(StringBuilder out) {
		for (AlternativeNode alternative : this.disjunction) {
			alternative.printContent(out);
		}
		if (this.mark != null) {
			this.mark.printContent(out);
		}
	}

}
