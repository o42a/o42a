/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array.impl;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;


public final class ArrayItemIR extends ScopeIR {

	private final CodeId id;

	public ArrayItemIR(Generator generator, ArrayItem item) {
		super(generator, item);

		final Scope enclosingScope = item.getEnclosingScope();

		assert !enclosingScope.isTopScope() :
			"Can not build IR for " + item;

		this.id = enclosingScope.ir(generator).nextAnonymousId();
	}

	@Override
	public CodeId getId() {
		return this.id;
	}

	@Override
	public void allocate() {

		final Obj target = getScope().getArtifact().materialize();

		target.ir(getGenerator()).allocate();
	}

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {
		return new Op(builder, this);
	}

	@Override
	protected void targetAllocated() {

		final Container enclosingContainer =
				getScope().getEnclosingContainer();

		if (enclosingContainer == null) {
			return;
		}

		enclosingContainer.getScope().ir(getGenerator()).allocate();
	}

	private static final class Op implements HostOp {

		private final CodeBuilder builder;
		private final ArrayItemIR ir;

		Op(CodeBuilder builder, ArrayItemIR ir) {
			this.builder = builder;
			this.ir = ir;
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
			return this.ir.getScope().getContext();
		}

		@Override
		public ObjectOp toObject(CodeDirs dirs) {
			return null;
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
		public ObjectOp materialize(CodeDirs dirs) {

			final Obj target = this.ir.getScope().getArtifact().materialize();

			return target.ir(getGenerator()).op(getBuilder(), dirs.code());
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {

			final Link itemLink = this.ir.getScope().getArtifact().toLink();

			assert itemLink.isVariable() :
				itemLink.getScope().getEnclosingScope() + " is constant array";

			// TODO implement array item assignment
		}

	}

}
