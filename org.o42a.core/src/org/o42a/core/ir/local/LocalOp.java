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
package org.o42a.core.ir.local;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.CompilerContext;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;


public final class LocalOp implements HostOp {

	private final LocalBuilder builder;
	private final LocalScope scope;

	public LocalOp(LocalBuilder builder, LocalScope scope) {
		this.builder = builder;
		this.scope = scope;
	}

	public final LocalScope getScope() {
		return this.scope;
	}

	@Override
	public final Generator getGenerator() {
		return this.builder.getGenerator();
	}

	@Override
	public final CompilerContext getContext() {
		return this.scope.getContext();
	}

	@Override
	public final LocalBuilder getBuilder() {
		return this.builder;
	}

	@Override
	public final LocalOp toLocal() {
		return this;
	}

	@Override
	public final ObjOp toObject(Code code, CodePos exit) {
		return null;
	}

	@Override
	public LclOp field(Code code, CodePos exit, MemberKey memberKey) {

		final Field<?> field = getScope().member(memberKey).toField();
		final FieldIR<?> fieldIR = field.ir(getGenerator());

		return fieldIR.getLocal();
	}

	@Override
	public ObjOp materialize(Code code, CodePos exit) {
		throw new UnsupportedOperationException();
	}

}
