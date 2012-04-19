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
package org.o42a.core.source;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.member.MemberRegistry.noDeclarations;
import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.common.StandaloneObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.MainDefiner;


public class Module extends Obj {

	private final ModuleCompiler compiler;
	private final String moduleName;
	private DeclarativeBlock definition;
	private ObjectMemberRegistry memberRegistry;
	private MainDefiner definer;

	public Module(CompilerContext context, String moduleName) {
		this(context.compileModule(), moduleName);
	}

	public Module(ModuleCompiler compiler, String moduleName) {
		this(moduleScope(compiler), compiler, moduleName);
	}

	private Module(
			ModuleScope scope,
			ModuleCompiler compiler,
			String moduleName) {
		super(scope);
		this.compiler = compiler;
		this.moduleName =
				moduleName != null ? moduleName : compiler.getModuleName();
	}

	public final String getModuleName() {
		return this.moduleName;
	}

	public final String getModuleId() {
		return getCompiler().getModuleName();
	}

	public final ModuleCompiler getCompiler() {
		return this.compiler;
	}

	@Override
	public String toString() {
		if (this.moduleName == null) {
			return "Module";
		}
		return '<' + this.moduleName + '>';
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
		this.definer = this.definition.define(definitionEnv());

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
		return this.definer.createDefinitions();
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {
		throw new IllegalArgumentException(
				"Not an enclosing scope: " + enclosing);
	}

	private static ModuleScope moduleScope(ModuleCompiler compiler) {

		final Namespace moduleNamespace =
				compiler.getContext().getIntrinsics().getModuleNamespace();
		final Namespace namespace = new Namespace(compiler, moduleNamespace);
		final Distributor distributor = declarativeDistributor(namespace);
		final DeclarativeBlock enclosingBlock =
				new DeclarativeBlock(compiler, distributor, noDeclarations());

		compiler.encloseInto(enclosingBlock);

		return new ModuleScope(compiler, distributor);
	}

	private static final class ModuleScope extends StandaloneObjectScope {

		ModuleScope(LocationInfo location, Distributor distributor) {
			super(location, distributor);
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new ModuleIR(generator, this);
		}

		final Module module() {
			return (Module) getContainer();
		}

	}

	private static final class ModuleIR extends ScopeIR {

		private final CodeId id;

		ModuleIR(Generator generator, ModuleScope scope) {
			super(generator, scope);
			this.id = generator.id(scope.module().getModuleId());
		}

		@Override
		public CodeId getId() {
			return this.id;
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
