/*
    Utilities
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
package org.o42a.util.log;


public final class LogDetail {

	public static LogDetail logDetail(String code, String message) {
		assert code != null :
			"Detail code not specified";
		assert message != null :
			"Default message not specified";
		return new LogDetail(code, message);
	}

	private final String code;
	private final String message;

	private LogDetail(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public final String getCode() {
		return this.code;
	}

	public final String getMessage() {
		return this.message;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final LogDetail other = (LogDetail) obj;

		return this.code.equals(other.code);
	}

	@Override
	public String toString() {
		if (this.message == null) {
			return super.toString();
		}
		return this.message;
	}

}
