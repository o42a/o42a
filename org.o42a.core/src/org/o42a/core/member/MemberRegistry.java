/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.member;

import static org.o42a.core.member.Inclusions.NO_INCLUSIONS;
import static org.o42a.core.member.MemberId.BROKEN_MEMBER_ID;

import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseFactory;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.Name;


public abstract class MemberRegistry {

	private static final NoDeclarations NO_DECLARATIONS = new NoDeclarations();
	private static final SkipDeclarations SKIP_DECLARATIONS =
			new SkipDeclarations();

	public static MemberRegistry noDeclarations() {
		return NO_DECLARATIONS;
	}

	public static MemberRegistry skipDeclarations() {
		return SKIP_DECLARATIONS;
	}

	private final Inclusions inclusions;
	private final ClauseFactory clauseFactory;

	public MemberRegistry(Inclusions inclusions) {
		this.inclusions = inclusions;
		this.clauseFactory = new ClauseFactory(this);
	}

	public abstract Obj getOwner();

	public final Inclusions inclusions() {
		return this.inclusions;
	}

	public abstract FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition);

	public abstract FieldBuilder newAlias(
			FieldDeclaration declaration,
			Ref ref);

	public ClauseBuilder newClause(
			Statements statements,
			ClauseDeclaration declaration) {
		return clauseFactory().newClause(statements, declaration);
	}

	public abstract void declareMember(Member member);

	public final MemberId tempMemberId() {
		return tempMemberId(MemberIdKind.TEMP_FIELD_NAME);
	}

	public abstract MemberId tempMemberId(MemberIdKind kind);

	public abstract Name anonymousBlockName();

	protected final ClauseFactory clauseFactory() {
		return this.clauseFactory;
	}

	protected final void prohibitedDeclaration(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_declaration",
				location,
				"Declarations prohibited here");
	}

	private static class NoDeclarations extends MemberRegistry {

		NoDeclarations() {
			super(NO_INCLUSIONS);
		}

		@Override
		public Obj getOwner() {
			return null;
		}

		@Override
		public FieldBuilder newField(
				FieldDeclaration declaration,
				FieldDefinition definition) {
			reportDeclaration(declaration);
			return null;
		}

		@Override
		public FieldBuilder newAlias(FieldDeclaration declaration, Ref ref) {
			reportDeclaration(declaration);
			return null;
		}

		@Override
		public ClauseBuilder newClause(
				Statements statements,
				ClauseDeclaration declaration) {
			reportDeclaration(declaration);
			return null;
		}

		@Override
		public void declareMember(Member member) {
			reportDeclaration(member);
		}

		@Override
		public MemberId tempMemberId(MemberIdKind kind) {
			return BROKEN_MEMBER_ID;
		}

		@Override
		public Name anonymousBlockName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "NoDeclarations";
		}

		protected void reportDeclaration(LocationInfo location) {
			prohibitedDeclaration(location);
		}

	}

	private static final class SkipDeclarations extends NoDeclarations {

		@Override
		protected void reportDeclaration(LocationInfo location) {
		}

	}

}
