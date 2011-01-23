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

import org.o42a.core.artifact.Artifact;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.LocalFieldOp;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;


public class FieldVariant<A extends Artifact<A>> extends St {

	private DeclaredField<A> field;
	private final FieldDeclaration declaration;
	private final FieldDefinition definition;
	private FieldVariantDecl<A> decl;
	private Conditions initialConditions;

	protected FieldVariant(
			DeclaredField<A> field,
			FieldDeclaration declaration,
			FieldDefinition definition) {
		super(declaration, definition.distribute());
		this.field = field;
		this.declaration = declaration;
		this.definition = definition;
	}

	public final MemberField toMember() {
		return this.field.toMember();
	}

	public final DeclaredField<A> getField() {
		return this.field;
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	public final FieldDefinition getDefinition() {
		return this.definition;
	}

	@Override
	public StatementKind getKind() {
		return StatementKind.FIELD;
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	public final Conditions getInitialConditions() {
		return this.initialConditions;
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		assert this.initialConditions == null :
			"Conditions already set for " + this;
		this.initialConditions = conditions;
		return conditions.notCondition(this);
	}

	@Override
	public Definitions define(DefinitionTarget target) {
		return null;
	}

	@Override
	public Action initialValue(LocalScope scope) {
		return initialCondition(scope);
	}

	@Override
	public Action initialCondition(LocalScope scope) {

		final Field<A> field =
			scope.member(getField().getKey())
			.toField()
			.toKind(getField().getArtifactKind());
		final LogicalValue logicalValue =
			field.getArtifact()
			.materialize()
			.getDefinitions()
			.fullCondition()
			.logicalValue(scope);

		return new ExecuteCommand(this, logicalValue);
	}

	@Override
	public St reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final FieldDeclaration declaration =
			getDeclaration().reproduce(reproducer);

		if (declaration == null) {
			return null;
		}

		final FieldDefinition definition =
			getDefinition().reproduce(reproducer);

		if (definition == null) {
			return null;
		}

		reproducer.getStatements().field(declaration, definition);

		return null;
	}

	@Override
	public String toString() {
		return "FieldVariant[" + this.field + "]:" + this.definition;
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new LocalFieldOp(builder, this);
	}

	FieldVariantDecl<A> getDecl() {
		if (this.decl == null) {
			this.decl = getField().getDecl().variantDecl(this);
			this.decl.init();
		}
		return this.decl;
	}

	void init() {
		getDecl();
	}

}
