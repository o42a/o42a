/*
    Compilation Analysis
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
package org.o42a.analysis.use;


public class ProxyUser<U extends Usage<U>> extends AbstractUser<U> {

	private User<U> proxied;
	private UseCase useCase;

	public ProxyUser(AllUsages<U> allUsages) {
		super(allUsages);
		this.proxied = allUsages.dummyUser();
	}

	public ProxyUser(User<U> proxied) {
		super(proxied.allUsages());
		this.proxied = proxied;
	}

	public final User<U> getProxied() {
		return this.proxied;
	}

	public void setProxied(User<U> proxied) {
		if (this.useCase == null) {
			this.proxied = proxied != null ? proxied : allUsages().dummyUser();
			return;
		}

		if (proxied == null) {
			if (this.proxied.isDummyUser()) {
				return;
			}
			this.proxied = allUsages().dummyUser();
		} else if (proxied.equals(this.proxied)) {
			return;
		} else {
			this.proxied = proxied;
		}

		this.useCase.update();
	}

	@Override
	public UseFlag selectUse(UseCaseInfo useCase, UseSelector<U> selector) {

		final UseCase uc = useCase.toUseCase();

		if (uc.isSteady()) {
			return uc.usedFlag();
		}

		this.useCase = uc;

		return getProxied().selectUse(uc, selector);
	}

	@Override
	public String toString() {
		if (this.proxied == null) {
			return super.toString();
		}
		return "ProxyUser[" + this.proxied.toString() + ']';
	}

}
