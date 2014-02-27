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
package org.o42a.ast.file;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.SentenceNode;


public class SectionTypeDefinitionNode extends AbstractNode {

	private final SentenceNode[] content;
	private final SignNode<HashLine> separator;

	public SectionTypeDefinitionNode(
			SentenceNode[] content,
			SignNode<HashLine> separator) {
		super(start(firstNode(content), separator), separator.getEnd());
		this.content = content;
		this.separator = separator;
	}

	public final SentenceNode[] getContent() {
		return this.content;
	}

	public final SignNode<HashLine> getSeparator() {
		return this.separator;
	}

	@Override
	public void printContent(StringBuilder out) {
		for (SentenceNode sentence : this.content) {
			sentence.printContent(out);
			out.append('\n');
		}
		this.separator.printContent(out);
		out.append('\n');
	}

}
