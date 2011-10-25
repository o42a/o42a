/*
    Compiler Core
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
package org.o42a.core;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.impl.rescoper.TransparentRescoper;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;


public abstract class Rescoper {

	public static Rescoper transparentRescoper(Scope finalScope) {
		return new TransparentRescoper(finalScope);
	}

	public static Rescoper upgradeRescoper(Scope fromScope, Scope toScope) {
		if (fromScope == toScope) {
			return transparentRescoper(fromScope);
		}
		toScope.assertDerivedFrom(fromScope);
		return Path.SELF_PATH.toPrefix(toScope).toRescoper();
	}

	private final Scope finalScope;

	public Rescoper(Scope finalScope) {
		this.finalScope = finalScope;
	}

	public final Scope getFinalScope() {
		return this.finalScope;
	}

	public boolean isTransparent() {
		return false;
	}

	public abstract Path getPath();

	public abstract <R extends Rescopable<R>> R update(R rescopable);

	public abstract Scope rescope(Scope scope);

	public abstract Resolver rescope(Resolver resolver);

	public abstract Scope updateScope(Scope scope);

	public abstract Rescoper and(Rescoper other);

	public abstract void resolveAll(Resolver resolver);

	public abstract Rescoper reproduce(Reproducer reproducer);

	public abstract HostOp write(CodeDirs dirs, HostOp host);

}
