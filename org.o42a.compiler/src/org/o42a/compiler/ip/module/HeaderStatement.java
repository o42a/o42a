/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.ip.module;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;
import static org.o42a.core.st.Instruction.SKIP_INSTRUCTION;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.LogInfo;


class HeaderStatement extends Statement {

	static void notDirective(CompilerLogger logger, LogInfo location) {
		logger.error(
				"not_header_directive",
				location,
				"Only directives allowed in module header");
	}

	private final Ref ref;

	HeaderStatement(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return noDefinitions();
	}

	@Override
	public ValueType<?> getValueType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		return this.ref.setEnv(env);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {

		final Instruction instruction = this.ref.toInstruction(resolver);

		if (instruction != null) {
			return new HeaderInstruction(this.ref, instruction);
		}

		notDirective(getLogger(), this);

		return SKIP_INSTRUCTION;
	}

	@Override
	public Definitions define(Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new HeaderStatement(ref);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

}
