/*
    Compiler LLVM Back-end
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.backend.llvm.code;

import org.o42a.util.string.ID;


final class LLInstructions {

	private static final Next END = new End();

	private Current current;

	LLInstructions(ID id) {
		this.current = new Current(id);
	}

	private LLInstructions(Current current) {
		this.current = current;
	}

	@Override
	public String toString() {
		if (this.current == null) {
			return super.toString();
		}
		return this.current.toString();
	}

	final void set(long instr) {
		this.current.set(instr);
	}

	final long next() {
		return this.current.next().get();
	}

	final LLInstructions inset(ID id) {

		final Current newCurrent =
				new Current(this.current.id, this.current.next());
		final Current inset = new Current(id, newCurrent);

		this.current.next(inset);
		this.current = newCurrent;

		return new LLInstructions(inset);
	}

	private interface Next {

		long get();

	}

	private final class Current implements Next {

		private final ID id;
		private long first;
		private Next next;

		Current(ID id) {
			this.id = id;
			this.next = END;
		}

		Current(ID id, Next next) {
			this.id = id;
			this.next = next;
		}

		@Override
		public long get() {
			if (this.first != 0) {
				return this.first;
			}
			return this.next.get();
		}

		@Override
		public String toString() {
			if (this.next == null) {
				return super.toString();
			}

			final StringBuilder out = new StringBuilder();

			out.append(this.id);
			if (this.first != 0) {
				out.append(" (0x");
				out.append(Long.toHexString(this.first));
				out.append(')');
			}
			if (this.next != END) {
				out.append(", ").append(this.next);
			}

			return out.toString();
		}

		final void set(long instr) {
			if (this.first == 0L) {
				this.first = instr;
			}
		}

		final Next next() {
			return this.next;
		}

		void next(Next next) {
			this.next = next;
		}

	}

	private static final class End implements Next {

		@Override
		public long get() {
			return 0;
		}

		@Override
		public String toString() {
			return ".";
		}

	}

}
