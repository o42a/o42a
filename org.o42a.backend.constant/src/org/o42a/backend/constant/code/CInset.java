/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code;

import org.o42a.codegen.code.Code;


abstract class CInset<C extends Code> extends CCode<C> {

	private final CBlock<?> block;
	private final CInsetPart<C> part;
	private final CCodePart<?> enclosingPart;

	CInset(CCode<?> enclosing, C code) {
		super(enclosing.getBackend(), enclosing.getFunction(), code);
		this.block = enclosing.block();
		this.part = new CInsetPart<>(this);
		this.enclosingPart = enclosing.inset(this);
	}

	public final CCodePart<?> getEnclosingPart() {
		return this.enclosingPart;
	}

	@Override
	public final CBlock<?> block() {
		return this.block;
	}

	@Override
	public final boolean created() {
		return !this.part.isNoOp();
	}

	@Override
	public final boolean exists() {
		return created();
	}

	@Override
	public CInsetPart<C> nextPart() {
		return this.part;
	}

	protected abstract C createUnderlying(Code enclosingUnderlying);

}
