/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.type.ascendant;

import org.o42a.ast.ref.AbstractRefVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.compiler.ip.access.AccessDistributor;


public final class AncestorSpecVisitor
		extends AbstractRefVisitor<AncestorTypeRef, AccessDistributor> {

	private final
	RefNodeVisitor<AncestorTypeRef, AccessDistributor> ancestorVisitor;

	public AncestorSpecVisitor(
			RefNodeVisitor<AncestorTypeRef, AccessDistributor> ancestorVisitor) {
		this.ancestorVisitor = ancestorVisitor;
	}

	@Override
	protected AncestorTypeRef visitRef(RefNode ref, AccessDistributor p) {
		return ref.accept(this.ancestorVisitor, p);
	}

}
