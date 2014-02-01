/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.util.string.ID;


abstract class Inset extends Code {

	private final Block block;

	Inset(Code enclosing, ID name) {
		super(enclosing, name);
		this.block = enclosing.getBlock();
		setOpNames(new OpNames.InsetOpNames(this));
	}

	@Override
	public final Allocator getAllocator() {
		return getBlock().getAllocator();
	}

	@Override
	public final Block getBlock() {
		return this.block;
	}

	@Override
	public final boolean created() {
		return writer().created();
	}

	@Override
	public final boolean exists() {
		return writer().exists();
	}

}
