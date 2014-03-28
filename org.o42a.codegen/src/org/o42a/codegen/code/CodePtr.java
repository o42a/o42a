/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.codegen.data.backend.DataAllocation;


public final class CodePtr extends AbstractPtr implements CodePos {

	private final Block code;

	CodePtr(Block code) {
		super(code.getId(), true, false);
		this.code = code;
	}

	@Override
	public final Block code() {
		return this.code;
	}

	public final CodePos head() {
		if (!code().created()) {
			return this;
		}
		return pos();
	}

	public final CodePos pos() {
		return this.code.writer().head();
	}

	@Override
	public String toString() {
		return this.code.toString();
	}

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return this.code.getGenerator().getFunctions().codeToAny(this);
	}

}
