/*
    Test Framework
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
package org.o42a.lib.test;

import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.core.CompilerContext;
import org.o42a.core.artifact.common.Module;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.lib.test.rt.*;


public class TestModule extends Module {

	public static Module testModule(CompilerContext context) {

		final URL base;

		try {

			final URL self = TestModule.class.getResource(
					TestModule.class.getSimpleName() + ".class");

			base = new URL(self, "../../../..");
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}

		final CompilerContext moduleContext =
			context.urlContext(
				"Test",
				base,
				"test.o42a",
				DECLARATION_LOGGER);

		return new TestModule(moduleContext);
	}

	private TestModule(CompilerContext context) {
		super(context, "Test");
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		members.addMember(new RtVoid(this).toMember());
		members.addMember(new RtFalse(this).toMember());
		members.addMember(new RtString(this).toMember());
		members.addMember(new RtInteger(this).toMember());
		members.addMember(new RtFloat(this).toMember());
		super.declareMembers(members);
	}

}
