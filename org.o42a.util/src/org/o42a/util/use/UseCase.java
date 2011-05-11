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


public final class UseCase extends AbstractUser {

	private final String name;
	private final UseFlag usedFlag;
	private final UseFlag unusedFlag;
	private User topLevel;
	private int rev;

	UseCase(String name) {
		this.name = name;
		this.usedFlag = new UseFlag(this, true);
		this.unusedFlag = new UseFlag(this, false);
	}

	public final boolean caseFlag(UseFlag flag) {
		return flag != null && flag.getUseCase() == this;
	}

	public final UseFlag usedFlag() {
		return this.usedFlag;
	}

	public final UseFlag unusedFlag() {
		return this.unusedFlag;
	}

	public final UseFlag useFlag(boolean used) {
		return used ? this.usedFlag : this.unusedFlag;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	UseFlag getUseBy(UseCase useCase) {
		return useCase == this ? usedFlag() : unusedFlag();
	}

	final int start(User user) {
		if (this.topLevel != null) {
			return this.rev;
		}
		this.topLevel = user;
		return ++this.rev;
	}

	final boolean end(User user) {
		if (this.topLevel != user) {
			return false;
		}
		this.topLevel = null;
		return true;
	}

}
