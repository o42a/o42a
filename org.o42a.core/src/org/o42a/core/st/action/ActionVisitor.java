/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.st.action;


public abstract class ActionVisitor<P, T> {

	public T visitReturnValue(ReturnValue returnValue, P p) {
		return visitAction(returnValue, p);
	}

	public T visitExecuteCommand(ExecuteCommand executeCommand, P p) {
		return visitAction(executeCommand, p);
	}

	public T visitRepeatLoop(RepeatLoop repeatLoop, P p) {
		return visitAction(repeatLoop, p);
	}

	public T visitExitLoop(ExitLoop exitLoop, P p) {
		return visitAction(exitLoop, p);
	}

	protected abstract T visitAction(Action action, P p);

}
