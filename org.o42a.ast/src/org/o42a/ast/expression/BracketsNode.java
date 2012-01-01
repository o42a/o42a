/*
    Abstract Syntax Tree
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.ast.expression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.field.InterfaceNode;


public class BracketsNode extends AbstractExpressionNode implements ClauseNode {

	private final SignNode<Bracket> opening;
	private final InterfaceNode iface;
	private final ArgumentNode[] arguments;
	private final SignNode<Bracket> closing;

	public BracketsNode(
			SignNode<Bracket> opening,
			InterfaceNode iface,
			ArgumentNode[] arguments,
			SignNode<Bracket> closing) {
		super(
				opening.getStart(),
				end(opening, iface, lastNode(arguments), closing));
		this.opening = opening;
		this.iface = iface;
		this.arguments = arguments;
		this.closing = closing;
	}

	public final SignNode<Bracket> getOpening() {
		return this.opening;
	}

	public final InterfaceNode getInterface() {
		return this.iface;
	}

	public final ArgumentNode[] getArguments() {
		return this.arguments;
	}

	public final SignNode<Bracket> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBrackets(this, p);
	}

	@Override
	public <R, P> R accept(ClauseNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBrackets(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append('[');
		if (this.iface != null) {
			this.iface.printContent(out);
			if (this.iface.getOpening() != null) {
				out.append(' ');
			}
		}
		for (ArgumentNode argument : this.arguments) {
			argument.printContent(out);
		}
		out.append(']');
	}

	public enum Bracket implements SignType {

		OPENING_BRACKET() {

			@Override
			public String getSign() {
				return "[";
			}

		},

		CLOSING_BRACKET() {

			@Override
			public String getSign() {
				return "]";
			}

		}

	}

}
