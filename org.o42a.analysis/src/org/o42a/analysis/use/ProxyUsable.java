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


public class ProxyUsable<U extends Usage<U>> extends ProxyUser<U> {

	private SimpleUsable<U> usable;

	public ProxyUsable(AllUsages<U> allUsages, Object used) {
		this(allUsages, null, used);
	}

	public ProxyUsable(AllUsages<U> allUsages, String name, Object used) {
		super(allUsages);
		this.usable = new SimpleUsable<>(allUsages, name, used);
		setProxied(this.usable.toUser());
	}

	public ProxyUsable(User<U> proxied, Object used) {
		this(proxied, null, used);
	}

	public ProxyUsable(User<U> proxied, String name, Object used) {
		super(proxied);
		this.usable = new SimpleUsable<>(allUsages(), name, used);
	}

	@Override
	public void setProxied(User<U> proxied) {
		if (this.usable.hasUses()) {
			// Usable were used, which means it is proxied.
			// Recreate it to mark unused.
			// The old usable instance remain unchanged and may be utilized
			// outside the proxy.
			this.usable = new SimpleUsable<>(
					allUsages(),
					this.usable.name(),
					this.usable.used());
		}
		super.setProxied(proxied);
	}

	public final void useBy(User<?> user, U usage) {
		if (user.isDummyUser()) {
			// Ignore dummy user.
			return;
		}

		if (!this.usable.hasUses()) {
			// Usable never used, which means it's not proxied.

			final User<U> oldProxied = getProxied();

			if (!oldProxied.isDummyUser()) {
				// Some user already proxied. Use the usable by it.
				for (U u : allUsages().usages()) {
					this.usable.useBy(u.selectiveUser(oldProxied), u);
				}
			}

			// Proxy the usable.
			super.setProxied(this.usable.toUser());
		}

		// Usable is proxied here. Use it.
		this.usable.useBy(user, usage);
	}

	@Override
	public String toString() {
		if (this.usable == null) {
			return super.toString();
		}
		return this.usable.toString();
	}

}
