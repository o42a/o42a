/*
    Test Framework
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
package org.o42a.lib.test;

import org.o42a.analysis.use.UserInfo;
import org.o42a.common.object.AnnotatedModule;
import org.o42a.common.object.RelatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;


@SourcePath("test.o42a")
@RelatedSources({"rt-float.o42a", "rt-integer.o42a", "rt-string.o42a"})
public class TestModule extends AnnotatedModule {

	public static Module testModule(CompilerContext parentContext) {
		return new TestModule(parentContext);
	}

	private TestModule(CompilerContext parentContext) {
		super(parentContext, moduleSources(TestModule.class));
	}

	public ObjectType test(UserInfo user) {
		return objectByName(user, "test").type().useBy(user);
	}

	private Obj objectByName(UserInfo user, String name) {

		final Member member = field(name);

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
