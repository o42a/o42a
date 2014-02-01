/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.data.struct;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public final class CType<S extends StructOp<S>> extends Struct<S> {

	private final ConstBackend backend;
	private final Type<S> original;
	private Type<?>[] dependencies;

	public CType(ConstBackend backend, Type<S> original) {
		super(original.getId());
		this.backend = backend;
		this.original = original;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	@Override
	public TypeAlignment requiredAlignment() {
		return getOriginal().requiredAlignment();
	}

	@Override
	public boolean isReentrant() {
		return false;
	}

	@Override
	public boolean isDebugInfo() {
		return false;
	}

	@Override
	public boolean isDebuggable() {
		return false;
	}

	@Override
	public Type<?>[] getTypeDependencies() {
		if (this.dependencies != null) {
			return this.dependencies;
		}

		final Type<?>[] originalDeps = getOriginal().getTypeDependencies();

		if (originalDeps.length == 0) {
			return this.dependencies = originalDeps;
		}

		final CType<?>[] deps = new CType<?>[originalDeps.length];

		for (int i = 0; i < deps.length; ++i) {
			deps[i] = getBackend().underlying(originalDeps[i]);
		}

		return this.dependencies = deps;
	}

	public final Type<S> getOriginal() {
		return this.original;
	}

	@Override
	public S op(StructWriter<S> writer) {
		return getOriginal().op(writer);
	}

	@Override
	protected void allocate(SubData<S> data) {
		throw new IllegalStateException(
				"Type should be manually constructed: " + this);
	}

	@Override
	protected void fill() {
	}

}
