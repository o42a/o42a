/*
    Intrinsics
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.core.ScopePlace.TOP_PLACE;

import org.o42a.ast.Node;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.*;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;


public final class Top extends AbstractScope implements Container {

	private final CompilerContext context;
	private IR ir;

	public Top(CompilerContext context) {
		this.context = context;
	}

	@Override
	public CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Node getNode() {
		return null;
	}

	@Override
	public Container getContainer() {
		return this;
	}

	@Override
	public Container getParentContainer() {
		return null;
	}

	@Override
	public boolean isTopScope() {
		return true;
	}

	@Override
	public ScopePlace getPlace() {
		return TOP_PLACE;
	}

	@Override
	public Container getEnclosingContainer() {
		return null;
	}

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	public Member toMember() {
		return null;
	}

	@Override
	public Artifact<?> toArtifact() {
		return null;
	}

	@Override
	public Obj toObject() {
		return null;
	}

	@Override
	public Clause toClause() {
		return null;
	}

	@Override
	public LocalScope toLocal() {
		return null;
	}

	@Override
	public Namespace toNamespace() {
		return null;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return null;
	}

	@Override
	public Path member(ScopeSpec user, MemberId memberId, Obj declaredIn) {
		return null;
	}

	@Override
	public Path findMember(ScopeSpec user, MemberId memberId, Obj declaredIn) {
		return null;
	}

	@Override
	public Path findPath(ScopeSpec user, MemberId memberId, Obj declaredIn) {
		return null;
	}

	@Override
	public boolean derivedFrom(Scope other) {
		return this == other;
	}

	@Override
	public ScopeIR ir(IRGenerator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = new IR(generator, this);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		return "TOP";
	}

	@Override
	public <A extends Artifact<A>> Field<A> propagateField(Field<A> sample) {
		throw new UnsupportedOperationException(
				"Can not propagate " + sample + " to " + this);
	}

	private static final class IR extends ScopeIR {

		IR(IRGenerator generator, Top scope) {
			super(generator, scope);
		}

		@Override
		public String getId() {
			return "";
		}

		@Override
		public String prefix(IRSymbolSeparator separator, String suffix) {
			if (separator == IRSymbolSeparator.DETAIL) {
				return separator + suffix;
			}
			return "." + separator + suffix;
		}

		@Override
		public void allocate() {
		}

		@Override
		protected void targetAllocated() {
		}

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {
			return new Op(builder, getScope());
		}

	}

	private static final class Op implements HostOp {

		private final CodeBuilder builder;
		private final Scope scope;

		public Op(CodeBuilder builder, Scope scope) {
			this.builder = builder;
			this.scope = scope;
		}

		@Override
		public IRGenerator getGenerator() {
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
		public ObjOp materialize(Code code, CodePos exit) {
			throw new UnsupportedOperationException();
		}

	}

}
