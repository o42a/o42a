/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.impl.normalizer;

import org.o42a.core.Scope;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.NormalPath;
import org.o42a.core.ref.path.NormalSteps;


public class UnchangedNormalPath implements NormalPath {

	private final BoundPath path;

	public UnchangedNormalPath(BoundPath path) {
		this.path = path;
	}

	public final BoundPath getPath() {
		return this.path;
	}

	@Override
	public boolean isNormalized() {
		return true;
	}

	@Override
	public Scope getOrigin() {
		return this.path.getOrigin();
	}

	@Override
	public void appendTo(NormalSteps normalSteps) {
		normalSteps.addNormalStep(new NormalPathStep(this.path.getPath()));
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
		getPath().op(host).value().writeCond(dirs);
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {
		return getPath().op(host).value().writeValue(dirs);
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return "UnchanhgedNormalPath" + this.path;
	}

}
