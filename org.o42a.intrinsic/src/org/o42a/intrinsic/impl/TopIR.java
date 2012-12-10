/*
    Intrinsics
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.intrinsic.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.ir.*;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;


final class TopIR extends ScopeIR {

	TopIR(Generator generator, Top scope) {
		super(generator, scope);
	}

	@Override
	public void allocate() {
	}

	@Override
	protected void targetAllocated() {
	}

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {
		return new TopOp(builder, getScope());
	}

	private static final class TopOp implements HostOp, HostValueOp {

		private final CodeBuilder builder;
		private final Scope scope;

		public TopOp(CodeBuilder builder, Scope scope) {
			this.builder = builder;
			this.scope = scope;
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
			return this.scope.getContext();
		}

		@Override
		public HostValueOp value() {
			return this;
		}

		@Override
		public LocalOp toLocal() {
			return null;
		}

		@Override
		public HostOp field(CodeDirs dirs, MemberKey memberKey) {
			return null;
		}

		@Override
		public ObjOp materialize(CodeDirs dirs, ObjHolder holder) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			throw new UnsupportedOperationException();
		}

	}

}