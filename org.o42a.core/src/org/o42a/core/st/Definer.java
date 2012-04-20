/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.st;

import static org.o42a.core.st.DefTargets.NO_DEFS;
import static org.o42a.core.st.ImplicationTargets.*;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.value.ValueStruct;


public abstract class Definer extends Implication<Definer> {

	public static DefTargets noDefs() {
		return NO_DEFS;
	}

	private final DefinerEnv env;
	private Eval eval;

	public Definer(Statement statement, DefinerEnv env) {
		super(statement);
		this.env = env;
	}

	public abstract DefTargets getDefTargets();

	public final DefinerEnv env() {
		return this.env;
	}

	public abstract DefValue value(Resolver resolver);

	public final void resolveAll(Resolver resolver) {
		getStatement().fullyResolved();
		getContext().fullResolution().start();
		try {
			fullyResolve(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public abstract InlineEval inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin);

	public abstract void normalize(RootNormalizer normalizer);

	public Eval eval(CodeBuilder builder) {

		final Eval eval = this.eval;

		if (eval != null && eval.getBuilder() == builder) {
			return eval;
		}

		assert getStatement().assertFullyResolved();

		return this.eval = createEval(builder);
	}

	protected final DefTargets expressionDef() {
		return new DefTargets(this, PRECONDITION_MASK | NON_CONSTANT_MASK);
	}

	protected final DefTargets valueDef() {
		return new DefTargets(
				this,
				PRECONDITION_MASK | VALUE_MASK | NON_CONSTANT_MASK);
	}

	protected final DefTargets fieldDef() {
		return new DefTargets(this, FIELD_MASK);
	}

	protected final DefTargets clauseDef() {
		return new DefTargets(this, CLAUSE_MASK);
	}

	protected abstract void fullyResolve(Resolver resolver);

	protected abstract Eval createEval(CodeBuilder builder);

}
