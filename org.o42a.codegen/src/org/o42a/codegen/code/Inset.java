/*
    Compiler Code Generator
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;


final class Inset extends Code {

	private final Block block;
	private final CodeWriter writer;

	Inset(Code enclosing, CodeId name) {
		super(enclosing, name);
		this.block = enclosing.getBlock();
		this.writer = enclosing.writer().inset(this);
	}

	@Override
	public final Block getBlock() {
		return this.block;
	}

	@Override
	public boolean created() {
		return this.writer.created();
	}

	@Override
	public boolean exists() {
		return this.writer.exists();
	}

	@Override
	public CodeWriter writer() {
		return this.writer;
	}

}
