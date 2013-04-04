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
package org.o42a.compiler.ip.type.def;

import static org.o42a.core.member.AccessSource.FROM_TYPE;

import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.Container;
import org.o42a.core.member.AccessSource;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.CheckResult;


final class TypeDefinitionAccessRules extends AccessRules {

	public static final AccessRules ACCESS_FROM_TYPE =
			new TypeDefinitionAccessRules();

	private TypeDefinitionAccessRules() {
		super(FROM_TYPE);
	}

	@Override
	public Ref selfRef(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor) {
		return prohibitObjectRef(location);
	}

	@Override
	public AccessRules setSource(AccessSource source) {
		return this;
	}

	@Override
	public CheckResult checkContainerAccessibility(
			LocationInfo location,
			Container from,
			Container to) {
		if (from.getScope().is(to.getScope())) {
			prohibitObjectRef(location);
			return CheckResult.CHECK_ERROR;
		}
		return CheckResult.CHECK_OK;
	}

	@Override
	public boolean containerIsVisible(Container from, Container to) {
		return true;
	}

	@Override
	public AccessRules contentRules() {
		return this;
	}

	private Ref prohibitObjectRef(LocationInfo location) {
		location.getLocation().getLogger().error(
				"prohibited_type_object_ref",
				location,
				"Can not refer the object from its type definition");
		return null;
	}

}
