/*
    Compiler Core
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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExitLoop;
import org.o42a.core.st.action.RepeatLoop;


abstract class EllipsisDefiner extends Definer {

	EllipsisDefiner(EllipsisStatement ellipsis, StatementEnv env) {
		super(ellipsis, env);
	}

	public final EllipsisStatement getEllipsis() {
		return (EllipsisStatement) getStatement();
	}

	@Override
	public StatementEnv nextEnv() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return noDefinitions();
	}

	@Override
	public Definitions define(Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	static class ExitDefiner extends EllipsisDefiner {

		ExitDefiner(EllipsisStatement ellipsis, StatementEnv env) {
			super(ellipsis, env);
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return new ExitLoop(this, getEllipsis().getName());
		}

	}

	static class RepeatDefiner extends EllipsisDefiner {

		RepeatDefiner(EllipsisStatement ellipsis, StatementEnv env) {
			super(ellipsis, env);
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return new RepeatLoop(this, getEllipsis().getName());
		}

	}

}
