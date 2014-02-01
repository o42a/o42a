/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import org.o42a.codegen.data.Content;


final class DebugSettings implements Content<DbgOptionsType> {

	private boolean debug;
	private boolean quiet;
	private byte noDebugMessages;
	private byte debugBlocksOmitted;
	private byte silentCalls;

	public final boolean isDebug() {
		return this.debug;
	}

	public final void setDebug(boolean debug) {
		this.debug = debug;
	}

	public final boolean isQuiet() {
		return !isDebug() || this.quiet;
	}

	public final void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	public final boolean isNoDebugMessages() {
		return enabled(this.noDebugMessages);
	}

	public final void setNoDebugMessages(boolean quiet) {
		this.noDebugMessages = newValue(quiet);
	}

	public final boolean isDebugBlocksOmitted() {
		return enabled(this.debugBlocksOmitted);
	}

	public final void setDebugBlocksOmitted(boolean debugBlocksOmitted) {
		this.debugBlocksOmitted = newValue(debugBlocksOmitted);
	}

	public final boolean isSilentCalls() {
		return enabled(this.silentCalls);
	}

	public final void setSilentCalls(boolean silentCalls) {
		this.silentCalls = newValue(silentCalls);
	}

	@Override
	public void allocated(DbgOptionsType instance) {
	}

	@Override
	public void fill(DbgOptionsType instance) {
		instance.quiet().setValue(toInt8(this.quiet));
		instance.noDebugMessages().setValue(toInt8(this.noDebugMessages));
		instance.debugBlocksOmitted().setValue(toInt8(this.debugBlocksOmitted));
		instance.silentCalls().setValue(toInt8(this.silentCalls));
	}

	private boolean enabled(byte flag) {
		if (!isDebug()) {
			return true;
		}
		if (isQuiet()) {
			return flag >= 0;
		}
		return flag > 0;
	}

	private static byte newValue(boolean value) {
		if (value) {
			return 1;
		}
		return -1;
	}

	private static byte toInt8(boolean flag) {
		if (flag) {
			return 1;
		}
		return 0;
	}

	private static byte toInt8(byte flag) {
		if (flag > 0) {
			return 1;
		}
		return 0;
	}

}
