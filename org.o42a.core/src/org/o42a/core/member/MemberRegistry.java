/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseFactory;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalFactory;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


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
	private final LocalFactory localFactory;

	public MemberRegistry(Inclusions inclusions) {
		this.inclusions = inclusions;
		this.clauseFactory = new ClauseFactory(this);
		this.localFactory = new LocalFactory(this);
	}

	public abstract Obj getOwner();

	public abstract MemberOwner getMemberOwner();

	public final Inclusions inclusions() {
		return this.inclusions;
	}

	public abstract FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition);

	public ClauseBuilder newClause(ClauseDeclaration declaration) {
		return clauseFactory().newClause(declaration);
	}

	public LocalScope newLocalScope(
			LocationInfo location,
			Distributor distributor,
			String name) {
		return localFactory().newLocalScope(location, distributor, name);
	}

	public LocalScope reproduceLocalScope(
			Reproducer reproducer,
			LocalScope scope) {
		return localFactory().reproduceLocalScope(reproducer, scope);
	}

	public abstract void declareMember(Member member);

	public abstract String anonymousBlockName();

	public MemberRegistry prohibitDeclarations() {
		return new ProhibitDeclarations(this);
	}

	protected final ClauseFactory clauseFactory() {
		return this.clauseFactory;
	}

	protected final LocalFactory localFactory() {
		return this.localFactory;
	}

	private static class NoDeclarations extends MemberRegistry {

		NoDeclarations() {
			super(Inclusions.noDeclarations());
		}

		@Override
		public MemberOwner getMemberOwner() {
			return null;
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
		public MemberRegistry prohibitDeclarations() {
			return this;
		}

		@Override
		public ClauseBuilder newClause(ClauseDeclaration declaration) {
			reportDeclaration(declaration);
			return null;
		}

		@Override
		public void declareMember(Member member) {
			reportDeclaration(member);
		}

		@Override
		public String anonymousBlockName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "NoDeclarations";
		}

		protected void reportDeclaration(LocationInfo location) {
			location.getContext().getLogger().prohibitedDeclaration(
					location);
		}

	}

	private static final class SkipDeclarations extends NoDeclarations {

		@Override
		protected void reportDeclaration(LocationInfo location) {
		}

	}

	private static final class ProhibitDeclarations
			extends NoDeclarations {

		private final MemberRegistry registry;

		ProhibitDeclarations(MemberRegistry registry) {
			this.registry = registry;
		}

		@Override
		public Obj getOwner() {
			return this.registry.getOwner();
		}

		@Override
		public String toString() {
			return "ProhibitDeclarations[" + this.registry + ']';
		}

	}

}
