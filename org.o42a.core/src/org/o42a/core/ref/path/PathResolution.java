/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import org.o42a.core.Container;
import org.o42a.core.object.Obj;
import org.o42a.util.log.LogRecord;


public final class PathResolution {

	static PathResolution pathResolution(BoundPath path, Container result) {
		return new PathResolution(path, result);
	}

	static PathResolution pathResolutionError(
			BoundPath path,
			LogRecord message) {
		return new PathResolution(path, message, true);
	}

	static PathResolution noPathResolutionError(BoundPath path) {
		return new PathResolution(path, null, false);
	}

	private final BoundPath path;
	private final Container result;
	private final LogRecord errorMessage;
	private final boolean error;

	private PathResolution(
			BoundPath path,
			LogRecord errorMessage,
			boolean error) {
		this.path = path;
		this.result = null;
		this.error = error;
		this.errorMessage = errorMessage;
	}

	private PathResolution(BoundPath path, Container result) {
		this.path = path;
		this.result = result;
		this.error = false;
		this.errorMessage = null;
	}

	public final boolean isError() {
		return this.error;
	}

	public final boolean isResolved() {
		return this.result != null;
	}

	public final Container getResult() {
		return this.result;
	}

	public final Obj getObject() {
		if (this.result == null) {
			return null;
		}

		final Obj object = getResult().toObject();

		assert object != null :
			"Path " + this.path + " should lead to object";

		return object;
	}

	public LogRecord getErrorMessage() {
		return this.errorMessage;
	}

	@Override
	public String toString() {
		if (this.error) {
			return "ERROR";
		}
		if (this.result == null) {
			return "NO_RESOLUTION";
		}
		return this.result.toString();
	}

}
