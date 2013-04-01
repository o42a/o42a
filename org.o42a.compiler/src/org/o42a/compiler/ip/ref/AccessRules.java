/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import static org.o42a.core.member.AccessSource.FROM_DECLARATION;
import static org.o42a.core.member.AccessSource.FROM_DEFINITION;
import static org.o42a.core.member.AccessSource.FROM_TYPE;

import org.o42a.core.Distributor;
import org.o42a.core.member.AccessSource;


public abstract class AccessRules {

	public static final AccessRules ACCESS_FROM_TYPE =
			new SimpleAccessRules(FROM_TYPE);
	public static final AccessRules ACCESS_FROM_DECLARATION =
			new SimpleAccessRules(FROM_DECLARATION);
	public static final AccessRules ACCESS_FROM_DEFINITION =
			new SimpleAccessRules(FROM_DEFINITION);

	private final AccessSource source;

	public AccessRules(AccessSource source) {
		assert source != null :
			"Access source not specified";
		this.source = source;
	}

	public final AccessSource getSource() {
		return this.source;
	}

	public abstract AccessRules setSource(AccessSource source);

	public final AccessDistributor distribute(Distributor distributor) {
		if (distributor.getClass() != AccessDistributor.class) {
			return new AccessDistributor(distributor, this);
		}
		return distribute((AccessDistributor) distributor);
	}

	public final AccessDistributor distribute(AccessDistributor distributor) {
		return distributor.setAccessRules(this);
	}

}
