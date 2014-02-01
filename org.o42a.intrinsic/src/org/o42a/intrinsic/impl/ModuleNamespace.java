/*
    Intrinsics
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.intrinsic.impl;

import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.core.Namespace;
import org.o42a.core.member.Accessor;
import org.o42a.core.source.Intrinsics;


public class ModuleNamespace extends Namespace {

	public ModuleNamespace(Intrinsics intrinsics) {
		super(intrinsics.getTop(), intrinsics.getTop());
		useNamespace(ROOT_PATH.bind(this, getScope()));
	}

	@Override
	protected boolean accessibleBy(Accessor accessor) {
		return true;
	}

}
