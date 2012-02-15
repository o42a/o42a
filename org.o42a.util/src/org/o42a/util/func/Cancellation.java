/*
    Utilities
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.util.func;


public final class Cancellation {

	public static final Cancelable NOT_CANCELABLE = new NotCancelable();

	public static Cancelable cancelables(Cancelable... cancelables) {
		if (cancelables == null || cancelables.length == 0) {
			return NOT_CANCELABLE;
		}
		if (cancelables.length == 1) {
			return cancelables[0];
		}
		return new Cancelables(cancelables);
	}

	public static Cancelable appendCancelable(
			Cancelable first,
			Cancelable second) {
		if (first == NOT_CANCELABLE) {
			return first;
		}
		return cancelables(first, second);
	}

	public static void cancelAll(Cancelable... cancelables) {
		for (Cancelable cancelable : cancelables) {
			cancelable.cancel();
		}
	}

	public static void cancelAll(Iterable<? extends Cancelable> cancelables) {
		for (Cancelable cancellable : cancelables) {
			cancellable.cancel();
		}
	}

	public static void cancelUpToNull(Cancelable... cancelables) {
		for (Cancelable cancellable : cancelables) {
			if (cancellable == null) {
				return;
			}
		}
	}

	private Cancellation() {
	}

	private static final class NotCancelable implements Cancelable {

		@Override
		public void cancel() {
		}

		@Override
		public String toString() {
			return "NotCancelable";
		}

	}

	private static final class Cancelables implements Cancelable {

		private final Cancelable[] cancelables;

		Cancelables(Cancelable[] cancelables) {
			this.cancelables = cancelables;
		}

		@Override
		public void cancel() {
			for (Cancelable cancelable : this.cancelables) {
				cancelable.cancel();
			}
		}

		@Override
		public String toString() {
			if (this.cancelables == null) {
				return super.toString();
			}

			final StringBuilder out = new StringBuilder();

			out.append(this.cancelables[0]);
			for (int i = 1; i < this.cancelables.length; ++i) {
				out.append("; ").append(this.cancelables[i]);
			}

			return out.toString();
		}

	}

}
