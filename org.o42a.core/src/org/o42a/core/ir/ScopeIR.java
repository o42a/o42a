/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ir;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectScopeIRBase;


public abstract class ScopeIR extends ObjectScopeIRBase {

	private final Generator generator;
	private final Scope scope;
	private HostOp op;

	public ScopeIR(Generator generator, Scope scope) {
		this.generator = generator;
		this.scope = scope;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public Scope getScope() {
		return this.scope;
	}

	public HostOp op(CodeBuilder builder, Code code) {

		final HostOp op = this.op;

		if (op != null && op.getBuilder() == builder) {
			return op;
		}

		return this.op = createOp(builder, code);
	}

	@Override
	public String toString() {
		return this.scope + " IR";
	}

	public abstract void allocate();

	protected abstract HostOp createOp(CodeBuilder builder, Code code);

}
