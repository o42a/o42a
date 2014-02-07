/*
    Compiler Commons
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.common.object;

import static org.o42a.core.member.MemberRegistry.noDeclarations;

import org.o42a.common.ref.ScopeSet;
import org.o42a.common.source.SourceTree;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.member.AbstractInclusions;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.FieldCompiler;
import org.o42a.core.source.ObjectCompiler;
import org.o42a.core.st.sentence.DeclarativeBlock;


public class CompiledObject extends Obj {

	public static CompiledField compileField(
			Obj owner,
			CompilerContext context) {

		final FieldCompiler compiler = context.compileField();
		final Namespace namespace =
				new Namespace(compiler, owner.getContainer());
		final DeclarativeBlock enclosingBlock =
				new DeclarativeBlock(compiler, namespace, noDeclarations());

		compiler.encloseInto(enclosingBlock);

		final FieldDeclaration declaration = compiler.declare(owner);

		return new CompiledField(owner, declaration, compiler);
	}

	public static CompiledField compileField(
			Obj owner,
			SourceTree<?> sourceTree) {
		return compileField(owner, sourceTree.context(owner.getContext()));
	}

	private final FieldCompiler compiler;
	private ObjectMemberRegistry memberRegistry;
	private DeclarativeBlock definition;
	private ScopeSet errorReportedAt;
	private DefinitionsBuilder definitionsBuilder;

	public CompiledObject(CompiledField field) {
		super(field);
		field.init(this);
		this.compiler = field.getCompiler();
	}

	public final Field getField() {
		return getScope().toField();
	}

	@Override
	public String toString() {
		return getScope().toString();
	}

	public final ObjectCompiler getCompiler() {
		return this.compiler;
	}

	@Override
	protected final Nesting createNesting() {
		return getField().toMember().getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		return getCompiler().buildAscendants(new Ascendants(this));
	}

	@Override
	protected void postResolve() {
		super.postResolve();

		this.memberRegistry =
				new ObjectMemberRegistry(
						new CompiledInclusions(getField()),
						this);
		this.definition = new DeclarativeBlock(
				this,
				new Namespace(this, this),
				this.memberRegistry);
		this.definitionsBuilder = this.definition.definitions(definitionEnv());

		getCompiler().define(this.definition);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.memberRegistry.registerMembers(members);
	}

	@Override
	protected void updateMembers() {
		this.definition.executeInstructions();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return this.definitionsBuilder.buildDefinitions();
	}

	protected final boolean reportError(Resolver resolver) {
		return reportError(resolver.getScope());
	}

	protected final boolean reportError(Scope scope) {
		if (this.errorReportedAt == null) {
			this.errorReportedAt = new ScopeSet();
			this.errorReportedAt.add(scope);
			return true;
		}
		return this.errorReportedAt.add(scope);
	}

	private static final class CompiledInclusions extends AbstractInclusions {

		private final Field field;

		CompiledInclusions(Field field) {
			this.field = field;
		}

		@Override
		public String toString() {
			if (this.field == null) {
				return super.toString();
			}
			return "FieldInclusions[" + this.field + ']';
		}

		@Override
		protected String includedIntoName() {
			return this.field.getDisplayName();
		}

	}

}
