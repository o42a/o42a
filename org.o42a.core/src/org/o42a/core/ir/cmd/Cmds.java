/*
    Compiler Core
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
package org.o42a.core.ir.cmd;


public final class Cmds {

	@SuppressWarnings("rawtypes")
	private static final InlineCmd<?> NO_INLINE_CMD = new NoCmd();

	@SuppressWarnings("unchecked")
	public static <T> InlineCmd<T> noInlineCmd() {
		return (InlineCmd<T>) NO_INLINE_CMD;
	}

	@SuppressWarnings("unchecked")
	public static <T> Cmd<T> noCmd() {
		return (Cmd<T>) NO_INLINE_CMD;
	}

	private Cmds() {
	}
}
