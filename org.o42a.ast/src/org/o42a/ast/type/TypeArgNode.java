/*
    Abstract Syntax Tree
    Copyright (C) 2013,2014 Ruslan Lopatin

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
import org.o42a.ast.atom.SignNode;


public class TypeArgNode extends AbstractNode {

	private final TypeArgumentNode argument;
	private final SignNode<TypeArgumentSuffix> suffix;

	public TypeArgNode(
			TypeArgumentNode argument,
			SignNode<TypeArgumentSuffix> suffix) {
		super(argument.getStart(), suffix.getEnd());
		this.argument = argument;
		this.suffix = suffix;
	}

	public final TypeArgumentNode getArgument() {
		return this.argument;
	}

	public final SignNode<TypeArgumentSuffix> getSuffix() {
		return this.suffix;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.argument.printContent(out);
		this.suffix.printContent(out);
	}

}
