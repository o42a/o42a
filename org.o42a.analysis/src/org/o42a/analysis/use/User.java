/*
    Compilation Analysis
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.analysis.use;

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;


public abstract class User<U extends Usage<U>> implements UserInfo, Uses<U> {

	private static final User<SimpleUsage> DUMMY_USER =
			ALL_SIMPLE_USAGES.dummyUser();

	public static User<SimpleUsage> dummyUser() {
		return DUMMY_USER;
	}

	public static UseCase useCase(String name) {
		return new UseCase(name);
	}

	public static UseCase steadyUseCase(String name) {
		return new UseCase(name, true);
	}

	private final AllUsages<U> allUsages;

	User(AllUsages<U> allUsages) {
		assert allUsages != null :
			"All usages not specified";
		this.allUsages = allUsages;
	}

	@Override
	public abstract boolean isDummy();

	@Override
	public final AllUsages<U> allUsages() {
		return this.allUsages;
	}

	@Override
	public final User<U> toUser() {
		return this;
	}

	@Override
	public final boolean isUsed(
			UseCaseInfo useCase,
			UseSelector<U> selector) {
		return selectUse(useCase, selector).isUsed();
	}

	abstract <UU extends Usage<UU>> void use(Usable<UU> usable, UU usage);

}
