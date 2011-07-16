/*
    Intrinsics
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
package org.o42a.intrinsic.string;

import org.o42a.common.object.CompiledObject;
import org.o42a.common.source.EmptyURLSource;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.intrinsic.root.Root;


public class Strings extends CompiledObject {

	public static final URLSourceTree STRINGS =
			new EmptyURLSource(Root.ROOT, "strings");

	public Strings(Root owner) {
		super(compileField(owner, STRINGS));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		super.declareMembers(members);
		members.addMember(new CompareStrings(this).toMember());
		members.addMember(new ConcatStrings(this).toMember());
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

}
