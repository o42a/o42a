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
package org.o42a.core.st;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Statements;


public abstract class Reproducer {

	private final Scope reproducingScope;
	private final Distributor distributor;

	public Reproducer(Scope reproducingScope, Distributor distributor) {
		this.reproducingScope = reproducingScope;
		this.distributor = distributor;
	}

	public final Scope getReproducingScope() {
		return this.reproducingScope;
	}

	public final Scope getScope() {
		return this.distributor.getScope();
	}

	public final Container getContainer() {
		return this.distributor.getContainer();
	}

	public final boolean isTopLevel() {
		return getScope().is(getPhrasePrefix().getScope());
	}

	public abstract Ref getPhrasePrefix();

	public abstract boolean phraseCreatesObject();

	public abstract MemberRegistry getMemberRegistry();

	public abstract Statements<?> getStatements();

	public final void applyClause(LocationInfo location, Clause clause) {
		applyClause(location, getStatements(), clause);
	}

	public final Distributor distribute() {
		return this.distributor;
	}

	public Reproducer distributeBy(Distributor distributor) {
		distributor.assertScopeIs(getScope());
		return new Wrap(this, getStatements(), distributor);
	}

	public Reproducer reproduceIn(Statements<?> statements) {
		statements.assertScopeIs(getScope());
		return new Wrap(this, statements, this.distributor);
	}

	public abstract Reproducer reproducerOf(Scope reproducingScope);

	public final CompilerLogger getLogger() {
		return this.distributor.getLogger();
	}

	public abstract void applyClause(
			LocationInfo location,
			Statements<?> statements,
			Clause clause);

	private static final class Wrap extends Reproducer {

		private final Reproducer reproducer;
		private final Statements<?> statements;

		Wrap(
				Reproducer reproducer,
				Statements<?> statements,
				Distributor distributor) {
			super(reproducer.getReproducingScope(), distributor);
			this.statements = statements;
			this.reproducer = reproducer;
		}

		@Override
		public boolean phraseCreatesObject() {
			return this.reproducer.phraseCreatesObject();
		}

		@Override
		public Ref getPhrasePrefix() {
			return this.reproducer.getPhrasePrefix();
		}

		@Override
		public final MemberRegistry getMemberRegistry() {
			return this.reproducer.getMemberRegistry();
		}

		@Override
		public final Statements<?> getStatements() {
			return this.statements;
		}

		@Override
		public Reproducer distributeBy(Distributor distributor) {
			return new Wrap(this.reproducer, getStatements(), distributor);
		}

		@Override
		public Reproducer reproduceIn(Statements<?> statements) {
			return new Wrap(this.reproducer, statements, distribute());
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (getReproducingScope().is(reproducingScope)) {
				return this;
			}
			return this.reproducer.reproducerOf(reproducingScope);
		}

		@Override
		public String toString() {
			return this.reproducer.toString();
		}

		@Override
		public void applyClause(
				LocationInfo location,
				Statements<?> statements,
				Clause clause) {
			this.reproducer.applyClause(location, statements, clause);
		}

	}

}
