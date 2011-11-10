/*
    Parser
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.parser;

import org.o42a.ast.Position;
import org.o42a.util.io.Source;
import org.o42a.util.io.SourcePosition;


final class WorkerPos extends Position implements Cloneable {

	private Source source;
	private int line;
	private int column;
	private long offset;
	private long charOffset;
	private int lastChar;

	WorkerPos(Source source) {
		this.source = source;
		this.line = 1;
		this.column = 1;
		this.charOffset = 0;
	}

	WorkerPos(Source source, SourcePosition start) {
		this.source = start.source();
		this.line = start.line();
		this.column = start.column();
		this.offset = start.offset();
		this.charOffset = 0;
	}

	@Override
	public Source source() {
		return this.source;
	}

	@Override
	public int column() {
		return this.column;
	}

	@Override
	public int line() {
		return this.line;
	}

	@Override
	public final long offset() {
		return this.offset;
	}

	@Override
	public WorkerPos clone() {
		try {
			return (WorkerPos) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	final long charOffset() {
		return this.charOffset;
	}

	final int lastChar() {
		return this.lastChar;
	}

	void set(WorkerPos position) {
		this.source = position.source;
		this.line = position.line;
		this.column = position.column;
		this.offset = position.offset;
		this.charOffset = position.charOffset;
		this.lastChar = position.lastChar;
	}

	void move(long offset, int c) {
		this.offset = offset;
		this.charOffset++;
		if (c == '\r') {
			this.line++;
			this.column = 1;
		} else if (c == '\n') {
			if (this.lastChar != '\r') {
				this.line++;
				this.column = 1;
			}
		} else {
			this.column++;
		}
		this.lastChar = c;
	}

}
