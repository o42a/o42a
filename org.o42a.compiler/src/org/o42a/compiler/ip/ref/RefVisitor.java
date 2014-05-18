/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.ref.Ref;


final class RefVisitor implements RefNodeVisitor<Ref, AccessDistributor> {

	private final RefInterpreter interpreter;

	RefVisitor(RefInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public Ref visitRef(RefNode ref, AccessDistributor p) {

		final Owner result = ref.accept(this.interpreter.ownerVisitor(), p);

		return result != null ? result.ref() : null;
	}

}
