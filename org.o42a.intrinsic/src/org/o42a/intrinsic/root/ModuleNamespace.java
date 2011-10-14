/*
    Intrinsics
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.core.Namespace;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.intrinsic.CompilerIntrinsics;


public class ModuleNamespace extends Namespace {

	public ModuleNamespace(CompilerIntrinsics intrinsics) {
		super(intrinsics.getTop(), intrinsics.getTop());
		useNamespace(new RootRef(intrinsics));
	}

	@Override
	protected boolean accessibleBy(Accessor accessor) {
		return true;
	}

	private static final class RootRef extends Wrap {

		RootRef(CompilerIntrinsics intrinsics) {
			super(
					intrinsics.getTop(),
					declarativeDistributor(intrinsics.getTop()));
		}

		@Override
		protected Ref resolveWrapped() {
			return ROOT_PATH.target(this, distribute());
		}

	}

}
