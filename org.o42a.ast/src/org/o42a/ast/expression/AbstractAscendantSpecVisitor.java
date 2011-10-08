/*
    Abstract Syntax Tree
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.ast.field.ArrayTypeNode;
import org.o42a.ast.ref.AbstractRefVisitor;
import org.o42a.ast.ref.RefNode;


public abstract class AbstractAscendantSpecVisitor<R, P>
		extends AbstractRefVisitor<R, P>
		implements AscendantSpecNodeVisitor<R, P> {

	@Override
	protected R visitRef(RefNode ref, P p) {
		return visitAscendantSpec(ref, p);
	}

	@Override
	public R visitArrayType(ArrayTypeNode arrayType, P p) {
		return visitAscendantSpec(arrayType, p);
	}

	protected abstract R visitAscendantSpec(AscendantSpecNode spec, P p);

}
