/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;
import static org.o42a.core.st.Instruction.SKIP_INSTRUCTION;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.*;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.Directive;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.log.LogInfo;


class HeaderStatement extends Statement {

	static void notDirective(CompilerLogger logger, LogInfo location) {
		logger.error(
				"not_header_directive",
				location,
				"Only directives allowed in file header");
	}

	private final Ref ref;

	HeaderStatement(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public Definer define(StatementEnv env) {
		return new HeaderDefiner(this, env);
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
	public final InlineValue inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void normalizeImperative(Normalizer normalizer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	@Override
	protected final void fullyResolveImperative(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

	private static final class HeaderDefiner extends Definer {

		private Definer refDefiner;

		HeaderDefiner(HeaderStatement header, StatementEnv env) {
			super(header, env);
			this.refDefiner = header.getRef().define(env);
		}

		public final HeaderStatement getHeader() {
			return (HeaderStatement) getStatement();
		}

		@Override
		public StatementEnv nextEnv() {
			return this.refDefiner.nextEnv();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {

			final Ref ref = getHeader().getRef();
			final Directive directive = ref.resolve(resolver).toDirective();

			if (directive == null) {
				notDirective(getLogger(), this);
				return SKIP_INSTRUCTION;
			}

			return new HeaderInstruction(ref, directive);
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return noDefinitions();
		}

		@Override
		public ValueStruct<?, ?> valueStruct(Scope scope) {
			throw new UnsupportedOperationException();
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

	}

}
