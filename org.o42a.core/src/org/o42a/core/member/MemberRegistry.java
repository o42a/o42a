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
package org.o42a.core.member;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.MemberRegistryLocalBase;
import org.o42a.core.source.LocationInfo;


public abstract class MemberRegistry extends MemberRegistryLocalBase {

	private static final NoDeclarations NO_DECLARATIONS = new NoDeclarations();
	private static final SkipDeclarations SKIP_DECLARATIONS =
			new SkipDeclarations();

	private final Inclusions inclusions;

	public static MemberRegistry noDeclarations() {
		return NO_DECLARATIONS;
	}

	public static MemberRegistry skipDeclarations() {
		return SKIP_DECLARATIONS;
	}

	public MemberRegistry(Inclusions inclusions) {
		this.inclusions = inclusions;
	}

	public final Inclusions inclusions() {
		return this.inclusions;
	}

	public abstract MemberOwner getMemberOwner();

	public abstract FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition);

	public MemberRegistry prohibitDeclarations() {
		return new ProhibitDeclarations(this);
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
