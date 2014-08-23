/*
    Utilities
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
package org.o42a.util.log;


public class SimpleLogMessage implements LogMessage {

	private final Severity severity;
	private final String code;
	private final String text;

	public SimpleLogMessage(Severity severity, String code, String text) {
		assert severity != null :
			"Message severity not specified";
		this.severity = severity;
		this.code = code != null ? code : severity.toString();
		this.text = text != null ? text : severity.toString();
	}

	@Override
	public Severity getSeverity() {
		return this.severity;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getText() {
		return this.text;
	}

}
