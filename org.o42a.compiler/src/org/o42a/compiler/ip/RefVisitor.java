/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.ref.Ref.errorRef;

import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public class RefVisitor extends AbstractRefVisitor<Ref, Distributor> {

	private final OwnerVisitor ownerVisitor = new OwnerVisitor(this);
	private Interpreter ip;

	public RefVisitor() {
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	protected Ref visitRef(RefNode ref, Distributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.ref() : null;
	}

	protected final void init(Interpreter ip) {
		this.ip = ip;
	}

	protected Ref objectIntrinsic(IntrinsicRefNode ref, Distributor p) {
		p.getLogger().error(
				"prohibited_object_intrinsic",
				ref,
				"$object$ intrinsic is allowed only within clauses");
		return errorRef(location(p, ref), p);
	}

	protected StaticTypeRef declaredIn(RefNode declaredInNode, Distributor p) {
		if (declaredInNode == null) {
			return null;
		}

		final Ref declaredIn = declaredInNode.accept(this, p);

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

	protected RefNodeVisitor<Ref, Distributor> adapterTypeVisitor() {
		return this;
	}

	protected Owner owner(Ref ref) {
		return Owner.defaultOwner(ref);
	}

	protected Owner bodyOwner(Ref ref) {
		return Owner.dontDerefOwner(ref);
	}

}
