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
package org.o42a.core.source;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.util.StringCodec.canonicalName;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.*;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueType;


public class Module extends PlainObject {

	private final String moduleName;
	private DeclarativeBlock definition;
	private ObjectMemberRegistry memberRegistry;

	public Module(CompilerContext context, String moduleName) {
		super(new ModuleScope(
				new Location(context, context.getSource()),
				moduleName));
		this.moduleName = moduleName;
		this.memberRegistry =
				new ObjectMemberRegistry(new ModuleInclusions(this), this);
	}

	public final String getModuleName() {
		return this.moduleName;
	}

	public final String getModuleId() {
		return ((ModuleScope) getScope()).moduleId;
	}

	public <C> C capablility(Class<? extends C> capabilityType) {
		return null;
	}

	@Override
	public String toString() {
		return getScope().toString();
	}

	protected DeclarativeBlock getDefinition() {
		if (this.definition == null) {

			final BlockBuilder compiled = getContext().compileBlock();
			final DeclarativeBlock definition = new DeclarativeBlock(
					this,
					new Namespace(this, this),
					this.memberRegistry);

			definition.setEnv(StatementEnv.objectEnv(this));
			compiled.buildBlock(definition);

			this.definition = definition;
		}

		return this.definition;
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				ValueType.VOID.typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.memberRegistry.registerMembers(members);
	}

	@Override
	protected void updateMembers() {
		getDefinition().executeInstructions();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getDefinition().define(getScope());
	}

	private static final class ModuleScope extends ObjectScope {

		private final String moduleId;

		ModuleScope(LocationInfo location, String moduleId) {
			super(
					location,
					declarativeDistributor(
							location.getContext()
							.getIntrinsics().getModuleNamespace()));
			this.moduleId = canonicalName(moduleId);
		}

		@Override
		public String toString() {
			return "<" + this.moduleId + '>';
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new ModuleIR(generator, this);
		}

	}

	private static final class ModuleIR extends ScopeIR {

		private final CodeId id;

		ModuleIR(Generator generator, ModuleScope scope) {
			super(generator, scope);
			this.id = generator.id(scope.moduleId);
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
