/*
    Utilities
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
package org.o42a.util.use;

import org.o42a.util.ArrayUtil;


final class UnknownUseFlag extends UseFlag {

	private final Usable<?> checking;
	private UnknownUseFlag[] deps;
	private boolean hasDependants;
	private boolean skip;
	private byte used;

	UnknownUseFlag(UseCase useCase, Usable<?> checking) {
		super(useCase);
		this.checking = checking;
	}

	@Override
	public final boolean isUsed() {
		if (this.used != 0) {
			return this.used > 0;
		}
		if (this.deps != null) {
			this.skip = true;
			for (UnknownUseFlag dep : this.deps) {
				if (dep.skip) {
					continue;
				}
				if (dep.isUsed()) {
					this.used = 1;
					return true;
				}
			}
		}
		this.used = -1;
		return false;
	}

	@Override
	public String toString() {
		return "CheckUse[" + this.checking + " by " + getUseCase() + ']';
	}

	@Override
	UnknownUseFlag toUnknown() {
		return this;
	}

	UnknownUseFlag dependsOn(UnknownUseFlag dep) {
		if (this == dep) {
			return this;
		}
		if (!this.hasDependants && this.deps == null) {
			dep.hasDependants = true;
			return dep;
		}

		dep.hasDependants = true;
		if (this.deps == null) {
			this.deps = new UnknownUseFlag[] {dep};
		} else {
			this.deps = ArrayUtil.append(this.deps, dep);
		}

		return this;
	}

	UseFlag end(Usable<?> checked) {
		if (this.deps == null && this.checking == checked) {
			this.used = -1;
			return getUseCase().unusedFlag();
		}
		return this;
	}

	UseFlag setTrue() {
		this.used = 1;
		return getUseCase().usedFlag();
	}

}
