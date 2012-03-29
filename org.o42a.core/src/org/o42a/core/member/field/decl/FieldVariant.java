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
package org.o42a.core.member.field.decl;

import org.o42a.core.*;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.log.Loggable;


final class FieldVariant implements PlaceInfo {

	private final DeclaredField field;
	private final FieldDeclaration declaration;
	private final FieldDefinition definition;
	private FieldDeclarationStatement statement;
	private DeclarativeBlock content;
	private Definer definer;
	private Ascendants ascendants;

	FieldVariant(
			DeclaredField field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		this.field = field;
		this.declaration = declaration;
		this.definition = definition;
	}

	public final Ascendants getAscendants() {
		return this.ascendants;
	}

	public DeclarativeBlock getContent() {
		if (this.content != null) {
			return this.content;
		}

		final Container container;

		if (getField().ownsCompilerContext()) {
			container = new Namespace(
					getDefinition(),
					getField().getContainer());
		} else {
			container = getField().getContainer();
		}

		this.content = new DeclarativeBlock(
				container,
				container,
				getField().getMemberRegistry());
		this.definer = this.content.define(new VariantEnv(this));

		return this.content;
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

	public final DeclaredField getField() {
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

	protected FieldDefinition reproduceDefinition(Reproducer reproducer) {
		return new ReproducedObjectDefinition(this, reproducer);
	}

	final FieldDeclarationStatement getStatement() {
		return this.statement;
	}

	final void setStatement(FieldDeclarationStatement statement) {
		this.statement = statement;
	}

	Ascendants buildAscendants(
			Ascendants implicitAscendants,
			Ascendants ascendants) {
		if (!getDeclaration().isLink() && !getDeclaration().isVariable()) {

			final ObjectDefinerImpl definer =
					new ObjectDefinerImpl(this, implicitAscendants, ascendants);

			getDefinition().setImplicitAscendants(implicitAscendants);
			if (getField().isOverride()) {
				getDefinition().overrideObject(definer);
			} else {
				getDefinition().defineObject(definer);
			}

			return this.ascendants = definer.getAscendants();
		}

		final LinkDefinerImpl definer =
				new LinkDefinerImpl(this, ascendants);

		getDefinition().defineLink(definer);

		return this.ascendants = definer.getAscendants();
	}

	void declareMembers() {
		getContent().executeInstructions();
	}

	Definitions define(Definitions definitions, Scope scope) {

		final Definitions variantDefinitions =
				getContentDefiner().define(scope);

		if (variantDefinitions == null) {
			return definitions;
		}
		if (definitions == null) {
			return variantDefinitions;
		}

		return definitions.refine(variantDefinitions);
	}

	private Definer getContentDefiner() {
		if (this.definer == null) {
			getContent();
		}
		return this.definer;
	}

	private static final class VariantEnv extends StatementEnv {

		private final FieldVariant variant;
		private ValueStruct<?, ?> expectedValueStruct;

		VariantEnv(FieldVariant variant) {
			this.variant = variant;
		}

		@Override
		public boolean hasPrerequisite() {
			return this.variant.getInitialEnv().hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.variant.getInitialEnv().prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return this.variant.getInitialEnv().hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.variant.getInitialEnv().precondition(scope);
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			if (this.expectedValueStruct != null) {
				return this.expectedValueStruct;
			}

			final ValueStruct<?, ?> ancestorValueStruct =
					this.variant.getField().toObject().value().getValueStruct();

			return this.expectedValueStruct = ancestorValueStruct;
		}

	}

}
