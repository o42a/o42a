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


public final class Use<U> {

	private final User user;
	private final U used;
	private Useable<U> useable;

	Use(User user, U used, Useable<U> useable) {
		this.user = user;
		this.used = used;
		this.useable = useable;
	}

	public final U get() {
		return this.used;
	}

	public final User getUser() {
		return this.user;
	}

	public final boolean isUsed() {
		return this.useable != null;
	}

	public final void release() {

		final Useable<U> useable = this.useable;

		if (useable == null) {
			return;
		}

		this.useable = null;
		this.user.release(this);
		useable.released(this);
	}

	@Override
	public String toString() {
		if (this.used == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		if (isUsed()) {
			out.append("Use[").append(this.used);
			out.append(" by ").append(this.user);
		} else {
			out.append("Unused[");
			out.append(this.used);
		}
		out.append(']');

		return out.toString();
	}

}
