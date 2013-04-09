/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.source;

import static org.o42a.core.Distributor.containerDistributor;
import static org.o42a.core.member.MemberRegistry.noDeclarations;
import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.Namespace;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.common.StandaloneObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public class Module extends Obj {

	private final ModuleCompiler compiler;
	private final Name moduleName;
	private DeclarativeBlock definition;
	private ObjectMemberRegistry memberRegistry;
	private DefinitionsBuilder definitionsBuilder;

	public Module(CompilerContext context, Name moduleName) {
		this(context.compileModule(), moduleName);
	}

	public Module(ModuleCompiler compiler, Name moduleName) {
		this(moduleScope(compiler, moduleName), compiler, moduleName);
	}

	private Module(
			ModuleScope scope,
			ModuleCompiler compiler,
			Name moduleName) {
		super(scope);
		this.compiler = compiler;
		this.moduleName =
				moduleName != null ? moduleName : compiler.getModuleName();
	}

	public final Name getModuleName() {
		return this.moduleName;
	}

	public final ModuleCompiler getCompiler() {
		return this.compiler;
	}

	@Override
	public String toString() {
		if (this.moduleName == null) {
			return "Module";
		}
		return '<' + this.moduleName.toString() + '>';
	}

	@Override
	protected final Nesting createNesting() {
		return Nesting.NO_NESTING;
	}

	@Override
	protected Ascendants buildAscendants() {
		return getCompiler().buildAscendants(new Ascendants(this));
	}

	@Override
	protected void postResolve() {
		super.postResolve();

		this.memberRegistry =
				new ObjectMemberRegistry(new ModuleInclusions(this), this);
		this.definition = new DeclarativeBlock(
				this,
				new Namespace(this, this),
				this.memberRegistry);
		this.definitionsBuilder = this.definition.definitions(definitionEnv());

		getCompiler().define(this.definition, IMPLICIT_SECTION_TAG);
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

	private static ModuleScope moduleScope(
			ModuleCompiler compiler,
			Name moduleName) {

		final Namespace moduleNamespace =
				compiler.getContext().getIntrinsics().getModuleNamespace();
		final Namespace namespace = new Namespace(compiler, moduleNamespace);
		final Distributor distributor = containerDistributor(namespace);
		final DeclarativeBlock enclosingBlock =
				new DeclarativeBlock(compiler, distributor, noDeclarations());

		compiler.encloseInto(enclosingBlock);

		return new ModuleScope(
				compiler,
				distributor,
				moduleName != null ? moduleName : compiler.getModuleName());
	}

	private static final class ModuleScope extends StandaloneObjectScope {

		private final ID id;

		ModuleScope(
				LocationInfo location,
				Distributor distributor,
				Name moduleName) {
			super(location, distributor);
			this.id = moduleName.toID();
		}

		@Override
		public ID getId() {
			return this.id;
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new ModuleIR(generator, this);
		}

	}

	private static final class ModuleIR extends ScopeIR {

		ModuleIR(Generator generator, ModuleScope scope) {
			super(generator, scope);
		}

		@Override
		public void allocate() {
			getScope().toObject().ir(getGenerator()).allocate();
		}

		@Override
		protected void targetAllocated() {
		}

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {
			return getScope().toObject().ir(getGenerator()).op(builder, code);
		}

	}

}
