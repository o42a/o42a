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
package org.o42a.core.ir.local;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.field.Field;


public abstract class LocalFieldIRBase extends ScopeIR {

	private LclOp local;

	public LocalFieldIRBase(Generator generator, Field field) {
		super(generator, field);
	}

	public final Field getField() {
		return getScope().toField();
	}

	public LclOp getLocal() {
		assertLocal();
		assert this.local != null :
			"Local field is not allocated yet: " + this;
		return this.local;
	}

	protected abstract LclOp allocateLocal(CodeBuilder builder, Code code);

	protected final void assertNotLocal() {
		assert !getField().isLocal() :
			this + " is local";
	}

	protected final void assertLocal() {
		assert getField().isLocal() :
			this + " is not local";
	}

	final LclOp allocate(CodeBuilder builder, Code code) {
		assertLocal();
		return this.local = allocateLocal(builder, code);
	}

}
