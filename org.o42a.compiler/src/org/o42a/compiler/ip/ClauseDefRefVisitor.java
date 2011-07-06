/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.compiler.ip.ref.ClauseObjectRef;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;


final class ClauseDefRefVisitor extends RefVisitor {

	@Override
	protected Ref objectIntrinsic(IntrinsicRefNode ref, Distributor p) {
		return new ClauseObjectRef(location(p, ref), p);
	}

}
