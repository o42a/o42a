/*
    Parser
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
package org.o42a.parser;

import static org.o42a.util.log.Logger.DEFAULT_LOGGER;

import java.io.IOException;
import java.io.Reader;

import org.o42a.ast.Position;
import org.o42a.util.Source;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;
import org.o42a.util.log.Severity;


public class ParserWorker {

	private Reader in;
	private final Pos position;// first un-accepted position
	private final Context root;
	private final StringBuilder unacceptedText = new StringBuilder();
	private int lastChar = Integer.MIN_VALUE;
	private long eof = Long.MAX_VALUE;
	private Logger logger = DEFAULT_LOGGER;
	private final Source source;

	public ParserWorker(Source source) {
		this.source = source;
		this.position = new Pos(source);
		this.root = new Context(null, this.position, this.position);
	}

	public ParserWorker(Source source, Position start) {
		this.source = source;
		this.position = new Pos(source, start);
		this.root = new Context(null, this.position, this.position);
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
		this.root.current.set(this.position);
		return this.root.parse(parser);
	}

	public Position position() {
		return this.position;
	}

	public void close() throws IOException {
		if (this.in != null) {
			this.in.close();
			this.in = null;
		}
	}

	private final Reader in() throws IOException {
		if (this.in == null) {
			this.in = this.source.open();
		}
		return this.in;
	}

	private static final class Pos extends Position implements Cloneable {

		private Source source;
		private int line;
		private int column;
		private long offset;
		private char lastChar;

		Pos(Source source) {
			this.source = source;
			this.line = 1;
			this.column = 1;
			this.offset = 0;
		}

		Pos(Source source, Position start) {
			this.source = start.source();
			this.line = start.line();
			this.column = start.column();
			this.offset = start.offset();
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
		public long offset() {
			return this.offset;
		}

		@Override
		public Pos clone() {
			try {
				return (Pos) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		private void set(Pos position) {
			this.source = position.source;
			this.line = position.line;
			this.column = position.column;
			this.offset = position.offset;
			this.lastChar = position.lastChar;
		}

		private void move(char c) {
			this.offset++;
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

	private final class Context extends ParserContext {

		private final Parser<?> parser;
		private final Pos current;
		private final Pos firstUnaccepted;
		private final Expectations expectations;
		private final Log log;
		private int hasErrors;
		private byte pending;

		Context(
				Parser<?> parser,
				Pos current,
				Pos firstUnaccepted) {
			this.parser = parser;
			this.current = current.clone();
			this.firstUnaccepted = firstUnaccepted;
			this.expectations = expectNothing();
			this.log = new Log(this);
		}

		Context(
				Parser<?> parser,
				Pos current,
				Pos firstUnaccepted,
				Expectations expectations) {
			this.parser = parser;
			this.current = current.clone();
			this.firstUnaccepted = firstUnaccepted;
			this.expectations = expectations.setContext(this);
			this.log = new Log(this);
		}

		@Override
		public void acceptBut(int charsLeft) {

			final int unaccepted = unaccepted();
			int accept = unaccepted - charsLeft;

			if (accept < 0) {
				getLogger().cantAccept(current(), charsLeft, unaccepted);
				accept = unaccepted;
			}
			if (accept == 0) {
				return;
			}

			final int from =
				(int) (this.firstUnaccepted.offset()
						- ParserWorker.this.position.offset());
			final StringBuilder unacceptedText =
				ParserWorker.this.unacceptedText;

			for (int i = from, l = from + accept; i < l; ++i) {
				this.firstUnaccepted.move(unacceptedText.charAt(i));
			}

			if (this.firstUnaccepted == ParserWorker.this.position) {
				unacceptedText.replace(0, accept, "");
			}
			if (this.firstUnaccepted.offset > this.current.offset) {
				this.current.set(this.firstUnaccepted);
				this.pending = 0;
			}
		}

		@Override
		public void skip() {
			if (this.pending > 0) {
				this.current.move((char) ParserWorker.this.lastChar);
				this.pending = 0;
			}
		}

		@Override
		public Position current() {
			return this.current;
		}

		@Override
		public Position firstUnaccepted() {
			return this.firstUnaccepted;
		}

		@Override
		public int unaccepted() {
			return (int) (this.current.offset()
					- this.firstUnaccepted.offset() + this.pending);
		}

		@Override
		public int next() {
			if (isEOF()) {
				return -1;
			}
			skip();

			final StringBuilder unacceptedText =
				ParserWorker.this.unacceptedText;

			for (;;) {

				final long idx = current().offset() - position().offset();
				final char ch;

				if (idx < unacceptedText.length()) {
					ch = unacceptedText.charAt((int) idx);
				} else {

					final int charCode;

					try {
						charCode = in().read();
					} catch (IOException e) {
						getLogger().ioError(current(), e.getLocalizedMessage());
						return -1;
					}

					if (charCode < 0) {
						ParserWorker.this.eof = current().offset();
						return ParserWorker.this.lastChar = -1;
					}

					ch = (char) charCode;
					unacceptedText.append(ch);
				}

				final char c;

				// handle newline for different OSes
				if (ch == '\n') {
					if (this.current.lastChar == '\r') {// \r\n
						this.current.move('\n');
						continue;// handle \r\n as one \n
					}
					c = ch;
				} else if (ch == '\r') {
					c = '\n';// always handle \r as \n
				} else {
					c = ch;
				}

				this.pending = 1;

				return ParserWorker.this.lastChar = c;
			}
		}

		@Override
		public int lastChar() {
			return ParserWorker.this.lastChar;
		}

		@Override
		public boolean isEOF() {
			return isFailed() || current().offset() >= ParserWorker.this.eof;
		}

		@Override
		public ParserLogger getLogger() {
			return this.log;
		}

		@Override
		public boolean hasErrors() {
			return this.hasErrors > 0;
		}

		@Override
		public Expectations getExpectations() {
			return this.expectations;
		}

		@Override
		protected <T> T parse(Parser<T> parser, Expectations expectations) {
			return parse(parser, expectations, this.firstUnaccepted);
		}

		@Override
		protected <T> T push(Parser<T> parser, Expectations expectations) {
			return parse(parser, expectations, this.firstUnaccepted.clone());
		}

		private boolean isFailed() {
			return this.hasErrors > 1;
		}

		private <T> T parse(
				Parser<T> parser,
				Expectations expectations,
				Pos firstUnaccepted) {
			if (isEOF()) {
				return null;
			}

			final long start = firstUnaccepted.offset;
			final Context context = new Context(
					parser,
					this.current,
					firstUnaccepted,
					expectations);
			final T result;
			final long accepted;

			try {
				result = parser.parse(context);
			} finally {
				accepted = pop(context, start);
			}

			if (accepted == 0 && result != null) {
				context.getLogger().notAccepted(current());
			}

			return result;
		}

		private long pop(Context complete, long start) {

			final long accepted = complete.firstUnaccepted.offset - start;

			if (accepted > 0) {
				if (complete.firstUnaccepted.offset
						> this.current.offset) {
					this.current.set(complete.firstUnaccepted);
					this.pending = 0;
				}
			}
			if (complete.hasErrors > this.hasErrors) {
				this.hasErrors = complete.hasErrors;
			}

			return accepted;
		}

	}

	private final class Log extends ParserLogger {

		private final Context context;

		Log(Context context) {
			this.context = context;
		}

		@Override
		public void log(LogRecord record) {

			final int ordinal = record.getSeverity().ordinal();

			if (ordinal >= Severity.ERROR.ordinal()) {
				if (ordinal <= Severity.FATAL.ordinal()) {
					this.context.hasErrors = 2;
				} else {
					this.context.hasErrors = 1;
				}
			}
			super.log(record);
		}

		@Override
		protected Logger getLogger() {
			return ParserWorker.this.getLogger();
		}

		@Override
		protected Object getSource() {
			return this.context.parser.getClass().getSimpleName();
		}

	}

}
