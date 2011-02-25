/*
    Compiler Core
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
package org.o42a.core.artifact.link;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.data.SubData;
import org.o42a.core.CompilerContext;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.local.RefLclOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;


final class LinkFieldIR extends FieldIR<Link> {

	LinkFieldIR(IRGenerator generator, Field<Link> field) {
		super(generator, field);
	}

	@Override
	protected RefFld<?> declare(SubData<?> data, ObjectBodyIR bodyIR) {

		final RefFld<?> fld;
		final Field<Link> field = getField();

		if (field.getArtifact().isVariable()) {
			fld = new VarFld(bodyIR, field);
		} else {
			fld = new LinkFld(bodyIR, field);
		}

		fld.allocate(data, getField().getArtifact().getTypeRef().getType());

		return fld;
	}

	@Override
	protected RefLclOp allocateLocal(LocalBuilder builder, Code code) {
		return code.allocate(getGenerator().refLclType()).op(builder, this);
	}

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {
		return new LinkOp(builder, this);
	}

	private static final class LinkOp implements HostOp {

		private final CodeBuilder builder;
		private final LinkFieldIR fieldIR;

		private LinkOp(CodeBuilder builder, LinkFieldIR fieldIR) {
			this.builder = builder;
			this.fieldIR = fieldIR;
		}

		@Override
		public IRGenerator getGenerator() {
			return this.fieldIR.getGenerator();
		}

		@Override
		public CodeBuilder getBuilder() {
			return this.builder;
		}

		@Override
		public CompilerContext getContext() {
			return this.fieldIR.getField().getContext();
		}

		@Override
		public ObjectOp toObject(Code code, CodePos exit) {
			return null;
		}

		@Override
		public LocalOp toLocal() {
			return null;
		}

		@Override
		public HostOp field(Code code, CodePos exit, MemberKey memberKey) {
			return null;
		}

		@Override
		public ObjectOp materialize(Code code, CodePos exit) {

			final Obj target =
				this.fieldIR.getField().getArtifact().materialize();

			return target.ir(getGenerator()).op(getBuilder(), code);
		}

	}

}
