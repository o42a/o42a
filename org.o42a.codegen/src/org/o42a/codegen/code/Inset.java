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

import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.util.string.ID;


final class Inset extends Code implements CodeAssetsSource {

	private final Block block;
	private final CodeWriter writer;
	private CodeAssets assets;

	Inset(Code enclosing, ID name) {
		super(enclosing, name);
		this.block = enclosing.getBlock();
		setOpNames(new OpNames.InsetOpNames(this));
		this.assets = enclosing.assets();
		this.writer = enclosing.writer().inset(this);
	}

	@Override
	public final Allocator getClosestAllocator() {
		return getBlock().getClosestAllocator();
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

	@Override
	public final CodeAssets assets() {
		return this.assets;
	}

	@Override
	protected final void updateAssets(CodeAssets assets) {
		assert !getFunction().isDone() :
			"Can not update assets of already built function";
		this.assets = assets;
	}

	@Override
	public final CodeWriter writer() {
		return this.writer;
	}

}
