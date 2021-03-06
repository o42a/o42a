/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.st;

import org.o42a.compiler.ip.access.AbstractAccess;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Statements;


public final class StatementsAccess extends AbstractAccess<Statements> {

	public StatementsAccess(AccessRules rules, Statements statements) {
		super(rules.contentRules(), statements);
	}

	public final boolean isDeclarative() {
		return get().getSentenceFactory().isDeclarative();
	}

	public final AccessDistributor nextDistributor() {
		return getRules().distribute(get().nextDistributor());
	}

	public final void statement(Statement statement) {
		get().statement(statement);
	}

}
