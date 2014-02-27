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
import org.o42a.ast.atom.SignType;
import org.o42a.util.io.SourcePosition;


public class AlternativeNode extends AbstractNode {

	private final SignNode<Separator> separator;
	private final SerialNode[] conjunction;

	public AlternativeNode(
			SignNode<Separator> separator,
			SerialNode[] conjunction) {
		super(separator, firstNode(conjunction), lastNode(conjunction));
		this.separator = separator;
		this.conjunction = conjunction;
	}

	public AlternativeNode(SourcePosition start, SourcePosition end) {
		super(start, end);
		this.separator = null;
		this.conjunction = new SerialNode[0];
	}

	public SignNode<Separator> getSeparator() {
		return this.separator;
	}

	public SerialNode[] getConjunction() {
		return this.conjunction;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.separator != null) {
			this.separator.printContent(out);
		}
		for (SerialNode statement : this.conjunction) {
			statement.printContent(out);
		}
	}

	public enum Separator implements SignType {

		ALTERNATIVE() {

			@Override
			public String getSign() {
				return ";";
			}

		}

	}

}
