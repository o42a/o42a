/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import static org.o42a.core.ScopePlace.TOP_PLACE;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableData;
import org.o42a.util.use.User;


public final class Top extends AbstractScope implements Container {

	private final LoggableData loggableData = new LoggableData(this);
	private final CompilerContext context;
	private final UsableTop user;
	private TopIR ir;

	public Top(CompilerContext context) {
		this.context = context;
		this.user = new UsableTop(this);
	}

	@Override
	public CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Loggable getLoggable() {
		return this.loggableData;
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
	public User toUser() {
		return this.user;
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
	public ConstructionMode getConstructionMode() {
		return ConstructionMode.FULL_CONSTRUCTION;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return null;
	}

	@Override
	public Path member(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return null;
	}

	@Override
	public Path findMember(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return null;
	}

	@Override
	public Path findPath(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return null;
	}

	@Override
	public boolean derivedFrom(Scope other) {
		return this == other;
	}

	@Override
	public ScopeIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = new TopIR(generator, this);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		return "TOP";
	}

}
