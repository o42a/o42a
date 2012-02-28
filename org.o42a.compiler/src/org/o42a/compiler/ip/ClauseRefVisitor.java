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

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.clauseObjectPath;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.DEFAULT_OWNER_FACTORY;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.NEVER_DEREF_OWNER_FACTORY;
import static org.o42a.core.ref.Ref.errorRef;

import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Location;


final class ClauseRefVisitor extends RefVisitor {

	private final boolean clauseDefinition;

	ClauseRefVisitor(boolean clauseDefinition) {
		super(
				clauseDefinition
				? DEFAULT_OWNER_FACTORY
				: NEVER_DEREF_OWNER_FACTORY);
		this.clauseDefinition = clauseDefinition;
	}

	@Override
	protected Ref objectIntrinsic(IntrinsicRefNode ref, Distributor p) {

		final Location location = location(p, ref);
		final Path path = clauseObjectPath(location, p.getScope());

		if (path == null) {
			return errorRef(location, p);
		}

		return path.bind(location, p.getScope()).target(p);
	}

	@Override
	protected RefNodeVisitor<Ref, Distributor> adapterTypeVisitor() {
		return this.clauseDefinition ? this : PLAIN_IP.refVisitor();
	}

}
