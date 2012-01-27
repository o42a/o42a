/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.AncestorTypeRef.impliedAncestorTypeRef;
import static org.o42a.compiler.ip.ArraySpecVisitor.arrayStructFinder;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.value.ValueStructFinder;


public final class AncestorSpecVisitor
		extends AbstractAscendantSpecVisitor<AncestorTypeRef, Distributor> {

	public static AncestorTypeRef parseAncestor(
			Interpreter ip,
			AscendantsNode ascendantsNode,
			Distributor distributor) {

		final AscendantNode[] ascendantNodes = ascendantsNode.getAscendants();

		if (ascendantNodes.length == 0) {
			return impliedAncestorTypeRef();
		}

		final ValueStructFinder arrayStructFinder =
				arrayStructFinder(ip, ascendantsNode, distributor.getLogger());

		return parseAncestor(
				ip,
				distributor,
				ascendantNodes[0],
				arrayStructFinder);
	}

	public static AncestorTypeRef parseAncestor(
			Interpreter ip,
			Distributor distributor,
			AscendantNode ascendantNode,
			ValueStructFinder arrayStructFinder) {

		final RefNodeVisitor<AncestorTypeRef, Distributor> ancestorVisitor;

		if (ascendantNode.getSeparator() == null) {
			ancestorVisitor = ip.ancestorVisitor(arrayStructFinder);
		} else {
			ancestorVisitor = ip.staticAncestorVisitor(arrayStructFinder);
		}

		final AncestorSpecVisitor specVisitor =
				new AncestorSpecVisitor(ancestorVisitor);

		return ascendantNode.getSpec().accept(specVisitor, distributor);
	}

	private final RefNodeVisitor<AncestorTypeRef, Distributor> ancestorVisitor;

	private AncestorSpecVisitor(
			RefNodeVisitor<AncestorTypeRef, Distributor> ancestorVisitor) {
		this.ancestorVisitor = ancestorVisitor;
	}

	@Override
	protected AncestorTypeRef visitRef(RefNode ref, Distributor p) {
		return ref.accept(this.ancestorVisitor, p);
	}

	@Override
	protected AncestorTypeRef visitAscendantSpec(
			AscendantSpecNode spec,
			Distributor p) {
		p.getLogger().error(
				"invalid_ancestor_spec",
				spec,
				"Invalid ancestor specifier");
		return impliedAncestorTypeRef();
	}

}
