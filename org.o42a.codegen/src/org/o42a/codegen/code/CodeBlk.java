/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.codegen.code.backend.CodeWriter;


public class CodeBlk extends Code {

	private final Code enclosing;
	CodeWriter writer;

	CodeBlk(Code enclosing, String name) {
		super(enclosing, name);
		this.enclosing = enclosing;
	}

	public final Code getEnclosing() {
		return this.enclosing;
	}

	@Override
	public final boolean exists() {
		return this.writer != null;
	}

	@Override
	public CodeWriter writer() {
		if (this.writer != null) {
			return this.writer;
		}
		return this.writer = getEnclosing().writer().block(this, getName());
	}

}
