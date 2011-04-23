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
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.member.Member;
import org.o42a.lib.test.rt.*;
import org.o42a.lib.test.run.RunTests;
import org.o42a.util.use.UserInfo;


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

	public ObjectType test(UserInfo user) {
		return objectByName(user, "test").type().useBy(user);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		members.addMember(new RunTests(this).toMember());
		members.addMember(new RtVoid(this).toMember());
		members.addMember(new RtFalse(this).toMember());
		members.addMember(new RtString(this).toMember());
		members.addMember(new RtInteger(this).toMember());
		members.addMember(new RtFloat(this).toMember());
		super.declareMembers(members);
	}

	private Obj objectByName(UserInfo user, String name) {

		final Member member = member(name);

		if (member == null) {
			getLogger().unresolved(this, toString() + ':' + name);
			return getContext().getFalse();
		}

		final Obj object = member.substance(user).toObject();

		if (object == null) {
			getLogger().notObject(this, toString() + ':' + name);
			return getContext().getFalse();
		}

		return object;
	}

}
