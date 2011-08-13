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


public final class UseCase extends AbstractUser implements UseCaseInfo {

	private final String name;
	private final UseFlag usedFlag;
	private final UseFlag unusedFlag;
	private final UseFlag checkUseFlag;
	private UseTracker topLevelTracker;
	private int rev;
	private final boolean steady;

	UseCase(String name) {
		this.name = name;
		this.steady = false;
		this.usedFlag = new UseFlag(this, (byte) 1);
		this.unusedFlag = new UseFlag(this, (byte) -1);
		this.checkUseFlag = new UseFlag(this, (byte) 0);
	}

	UseCase(String name, boolean steady) {
		this.name = name;
		this.steady = true;
		this.unusedFlag = this.usedFlag = new UseFlag(this, (byte) 1);
		this.checkUseFlag = new UseFlag(this, (byte) 0);
	}

	public final boolean isSteady() {
		return this.steady;
	}

	@Override
	public final UseCase toUseCase() {
		return this;
	}

	public final UseFlag usedFlag() {
		return this.usedFlag;
	}

	public final UseFlag unusedFlag() {
		return this.unusedFlag;
	}

	public final UseFlag checkUseFlag() {
		return this.checkUseFlag;
	}

	public final boolean caseFlag(UseFlag flag) {
		return flag != null && flag.getUseCase() == this;
	}

	public final UseFlag useFlag(boolean used) {
		return used ? this.usedFlag : this.unusedFlag;
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		return useCase == this ? usedFlag() : unusedFlag();
	}

	@Override
	public String toString() {
		return this.name;
	}

	final int start(UseTracker tracker) {
		if (this.topLevelTracker != null) {
			return this.rev;
		}
		this.topLevelTracker = tracker;
		return ++this.rev;
	}

	final boolean end(UseTracker tracker) {
		if (this.topLevelTracker != tracker) {
			return false;
		}
		this.topLevelTracker = null;
		return true;
	}

}
