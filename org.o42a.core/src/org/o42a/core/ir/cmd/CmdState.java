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

import static org.o42a.core.ir.cmd.CmdResult.CMD_DONE;
import static org.o42a.core.ir.cmd.CmdResult.CMD_NEXT;
import static org.o42a.core.ir.cmd.CmdResult.CMD_REPEAT;


public final class CmdState<T> {

	private T state;
	private CmdResult result;
	private CmdState<?> nested;

	public final T get() {
		return this.state;
	}

	public final void repeat(T state) {
		setResult(CMD_REPEAT);
		this.state = state;
	}

	public final void next() {
		setResult(CMD_NEXT);
	}

	public final void done() {
		setResult(CMD_DONE);
	}

	<TT> CmdResult writeNested(Control control, Cmd<TT> cmd) {

		final CmdState<TT> nested = nestedState();
		final MainControl main = control.main();

		main.setLastState(nested);

		final CmdResult result = nested.write(control, cmd);

		if (!result.isRepeat()) {
			this.nested = null;
		}

		main.setLastState(this);

		return result;
	}

	private void setResult(CmdResult result) {
		assert this.result == null :
			"Command result already set";
		this.result = result;
	}

	@SuppressWarnings("unchecked")
	private <TT> CmdState<TT> nestedState() {
		if (this.nested != null) {
			return (CmdState<TT>) this.nested;
		}

		final CmdState<TT> nested = new CmdState<>();

		this.nested = nested;

		return nested;
	}

	private CmdResult write(Control control, Cmd<T> cmd) {
		cmd.write(control, this);

		final CmdResult result = this.result;

		assert result != null :
			"Indefinite command result";

		this.result = null;

		return result;
	}

}
