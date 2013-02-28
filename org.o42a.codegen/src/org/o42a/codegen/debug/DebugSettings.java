/*
    Compiler Code Generator
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.codegen.debug;


final class DebugSettings {

	private boolean debug;
	private boolean debugBlocksOmitted;
	private boolean noDebugMessages;

	public final boolean isDebug() {
		return this.debug;
	}

	public final void setDebug(boolean debug) {
		this.debug = debug;
	}

	public final boolean isNoDebugMessages() {
		return !isDebug() || this.noDebugMessages;
	}

	public final void setNoDebugMessages(boolean quiet) {
		this.noDebugMessages = quiet;
	}

	public final boolean isDebugBlocksOmitted() {
		return !isDebug() || this.debugBlocksOmitted;
	}

	public final void setDebugBlocksOmitted(boolean debugBlocksOmitted) {
		this.debugBlocksOmitted = debugBlocksOmitted;
	}

}
