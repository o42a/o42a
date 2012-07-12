/*
    Abstract Syntax Tree
    Copyright (C) 2012 Ruslan Lopatin

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


public class MetaExpressionNode extends AbstractExpressionNode {

	private final SignNode<Prefix> prefix;
	private final ExpressionNode macro;

	public MetaExpressionNode(SignNode<Prefix> prefix, ExpressionNode macro) {
		super(prefix.getStart(), end(prefix, macro));
		this.prefix = prefix;
		this.macro = macro;
	}

	public final SignNode<Prefix> getPrefix() {
		return this.prefix;
	}

	public final ExpressionNode getMacro() {
		return this.macro;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitMetaExpression(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.prefix.printContent(out);
		if (this.macro != null) {
			this.macro.printContent(out);
		} else {
			out.append("???");
		}
	}

	public enum Prefix implements SignType {

		HASH() {

			@Override
			public String getSign() {
				return "#";
			}

		}

	}

}
