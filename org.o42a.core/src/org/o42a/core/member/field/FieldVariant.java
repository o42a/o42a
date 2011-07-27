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
package org.o42a.core.member.field;

import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.util.log.Loggable;


public abstract class FieldVariant<A extends Artifact<A>> implements PlaceInfo {

	private final DeclaredField<A, ?> field;
	private final FieldDeclaration declaration;
	private final FieldDefinition definition;
	private FieldDeclarationStatement statement;

	protected FieldVariant(
			DeclaredField<A, ?> field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		this.field = field;
		this.declaration = declaration;
		this.definition = definition;
	}

	@Override
	public final Scope getScope() {
		return this.declaration.getScope();
	}

	public final MemberField toMember() {
		return this.field.toMember();
	}

	@Override
	public final CompilerContext getContext() {
		return this.declaration.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.declaration.getLoggable();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.declaration.getPlace();
	}

	@Override
	public final Container getContainer() {
		return this.declaration.getContainer();
	}

	public final CompilerLogger getLogger() {
		return this.field.getLogger();
	}

	public final DeclaredField<A, ?> getField() {
		return this.field;
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public final FieldDefinition getDefinition() {
		return this.definition;
	}

	public final StatementEnv getInitialEnv() {
		return this.statement.getInitialEnv();
	}

	@Override
	public final Distributor distribute() {
		return this.declaration.distribute();
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return this.declaration.distributeIn(container);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		return "FieldVariant[" + this.field + "]:" + this.definition;
	}

	protected abstract FieldDefinition reproduceDefinition(
			Reproducer reproducer);

	final FieldDeclarationStatement getStatement() {
		return this.statement;
	}

	final void setStatement(FieldDeclarationStatement statement) {
		this.statement = statement;
	}

}
