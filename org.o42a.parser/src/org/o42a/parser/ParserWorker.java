/*
    Parser
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.util.log.Logger.DEFAULT_LOGGER;

import java.io.IOException;

import org.o42a.ast.Position;
import org.o42a.util.io.Source;
import org.o42a.util.io.SourceReader;
import org.o42a.util.log.Logger;


public class ParserWorker {

	private SourceReader in;
	private final WorkerPos position;// first un-accepted position
	private final ParserContext root;
	private final UnacceptedChars unacceptedChars = new UnacceptedChars();
	private int lastChar = Integer.MIN_VALUE;
	private long eof = Long.MAX_VALUE;
	private Logger logger = DEFAULT_LOGGER;
	private final Source source;

	public ParserWorker(Source source) {
		this.source = source;
		this.position = new WorkerPos(source);
		this.root = new ParserContext(this, null, this.position, this.position);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public ParserLogger getParserLogger() {
		return this.root.getLogger();
	}

	public void setLogger(Logger logger) {
		this.logger = logger != null ? logger : DEFAULT_LOGGER;
	}

	public <T> T parse(Parser<T> parser) {
		this.root.setCurrent(this.position);
		return this.root.parse(parser);
	}

	public final Position position() {
		return this.position;
	}

	public void close() throws IOException {
		if (this.in != null) {
			this.in.close();
			this.in = null;
		}
	}

	final SourceReader in() throws IOException {
		if (this.in == null) {
			this.in = this.source.open();
		}
		return this.in;
	}

	final WorkerPos pos() {
		return this.position;
	}

	final UnacceptedChars unacceptedChars() {
		return this.unacceptedChars;
	}

	final int lastChar() {
		return this.lastChar;
	}

	final int setLastChar(int lastChar) {
		return this.lastChar = lastChar;
	}

	final long eof() {
		return this.eof;
	}

	final void setEOF(long eof) {
		this.eof = eof;
		this.lastChar = -1;
	}

}
