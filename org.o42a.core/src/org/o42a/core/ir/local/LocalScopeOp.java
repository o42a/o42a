/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.source.CompilerContext;


public final class LocalScopeOp implements HostOp {

	private final CodeBuilder builder;
	private final LocalScope scope;

	public LocalScopeOp(CodeBuilder builder, LocalScope scope) {
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
	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	@Override
	public HostValueOp value() {
		throw new UnsupportedOperationException("Local scope has no value");
	}

	@Override
	public final LocalScopeOp toLocalScope() {
		return this;
	}

	public final ObjectOp owner() {
		return getBuilder().owner();
	}

	@Override
	public LclOp field(CodeDirs dirs, MemberKey memberKey) {

		final Field field =
				getScope().member(memberKey).toField().field(dummyUser());
		final FieldIR fieldIR = field.ir(getGenerator());

		return fieldIR.getLocal();
	}

	@Override
	public ObjOp materialize(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

}