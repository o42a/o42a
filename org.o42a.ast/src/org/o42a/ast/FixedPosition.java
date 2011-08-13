/*
    Abstract Syntax Tree
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
package org.o42a.ast;

import java.io.Serializable;

import org.o42a.util.io.Source;
import org.o42a.util.log.*;


public class FixedPosition
		extends Position
		implements LoggablePosition, Cloneable, Serializable {

	private static final long serialVersionUID = 6809375589547367813L;

	private final Source source;
	private final int line;
	private final int column;
	private final long offset;
	private LogReason reason;

	public FixedPosition(Source source) {
		this.source = source;
		this.line = 1;
		this.column = 0;
		this.offset = 0;
	}

	public FixedPosition(Source source, int line, int column, long offset) {
		this.source = source;
		this.line = line;
		this.column = column;
		this.offset = offset;
	}

	FixedPosition(Position position) {
		this(
				position.source(),
				position.line(),
				position.column(),
				position.offset());
	}

	@Override
	public LogReason getReason() {
		return this.reason;
	}

	@Override
	public FixedPosition setReason(LogReason reason) {
		if (reason == null) {
			return this;
		}

		final FixedPosition clone = clone();

		if (this.reason == null) {
			clone.reason = reason;
		} else {
			clone.reason = this.reason.setNext(reason);
		}

		return clone;
	}

	@Override
	public Loggable getLoggable() {
		return this;
	}

	@Override
	public Source source() {
		return this.source;
	}

	@Override
	public int line() {
		return this.line;
	}

	@Override
	public int column() {
		return this.column;
	}

	@Override
	public long offset() {
		return this.offset;
	}

	@Override
	public FixedPosition fix() {
		return this;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {
		return visitor.visitPosition(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		print(out, true);
	}

	@Override
	protected FixedPosition clone() {
		try {
			return (FixedPosition) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
