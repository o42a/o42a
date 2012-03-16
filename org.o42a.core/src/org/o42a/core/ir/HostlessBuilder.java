/*
    Compiler Core
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
package org.o42a.core.ir;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;


final class HostlessBuilder extends CodeBuilder {

	private final Op host;

	HostlessBuilder(CompilerContext context, Function<?> function) {
		super(context, function);
		this.host = new Op(this);
	}

	@Override
	public final HostOp host() {
		return this.host;
	}

	@Override
	public ObjectOp owner() {
		throw new UnsupportedOperationException();
	}

	private static final class Op implements HostOp {

		private final HostlessBuilder builder;

		Op(HostlessBuilder builder) {
			this.builder = builder;
		}

		@Override
		public Generator getGenerator() {
			return this.builder.getGenerator();
		}

		@Override
		public CodeBuilder getBuilder() {
			return this.builder;
		}

		@Override
		public CompilerContext getContext() {
			return this.builder.getContext();
		}

		@Override
		public LocalOp toLocal() {
			return null;
		}

		@Override
		public HostOp field(CodeDirs dirs, MemberKey memberKey) {
			throw new IllegalArgumentException("Fake host has no fields");
		}

		@Override
		public ObjectOp materialize(CodeDirs dirs) {
			return null;
		}

		@Override
		public ObjectOp dereference(CodeDirs dirs) {
			return null;
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
