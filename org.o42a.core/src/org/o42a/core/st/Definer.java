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
import org.o42a.core.object.def.Definitions;
import org.o42a.util.log.LogInfo;


public abstract class Definer extends Implication<Definer> {

	public static DefTargets noDefs() {
		return NO_DEFS;
	}

	public static DefTargets expressionDef(LogInfo location) {
		return new DefTargets(location, PRECONDITION_MASK);
	}

	public static DefTargets valueDef(LogInfo location) {
		return new DefTargets(location, PRECONDITION_MASK | VALUE_MASK);
	}

	public static DefTargets fieldDef(LogInfo location) {
		return new DefTargets(location, FIELD_MASK);
	}

	public static DefTargets clauseDef(LogInfo location) {
		return new DefTargets(location, CLAUSE_MASK);
	}

	private final DefinerEnv env;

	public Definer(Statement statement, DefinerEnv env) {
		super(statement);
		this.env = env;
	}

	public abstract DefTargets getDefTargets();

	public abstract DefinitionTargets getDefinitionTargets();

	public final DefinerEnv env() {
		return this.env;
	}

	public abstract DefinerEnv nextEnv();

	public abstract Definitions define(Scope scope);

}
