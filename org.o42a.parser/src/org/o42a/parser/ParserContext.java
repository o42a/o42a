/*
    Parser
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.separator;

import java.io.IOException;

import org.o42a.ast.Node;
import org.o42a.ast.Position;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;
import org.o42a.util.io.SourceReader;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;
import org.o42a.util.log.Severity;


public final class ParserContext {

	private final ParserWorker worker;
	private final Parser<?> parser;
	private final WorkerPos current;
	private final WorkerPos firstUnaccepted;
	private final Expectations expectations;
	private final Log log;
	private int hasErrors;
	private long pendingOffset;
	private byte pending;

	ParserContext(
			ParserWorker worker,
			Parser<?> parser,
			WorkerPos current,
			WorkerPos firstUnaccepted) {
		this.worker = worker;
		this.parser = parser;
		this.current = current.clone();
		this.firstUnaccepted = firstUnaccepted;
		this.expectations = expectNothing();
		this.log = new Log(this);
	}

	ParserContext(
			ParserWorker worker,
			Parser<?> parser,
			WorkerPos current,
			WorkerPos firstUnaccepted,
			Expectations expectations) {
		this.worker = worker;
		this.parser = parser;
		this.current = current.clone();
		this.firstUnaccepted = firstUnaccepted;
		this.expectations = expectations.setContext(this);
		this.log = new Log(this);
	}

	public final <T> T parse(Parser<T> parser) {
		return parse(parser, getExpectations());
	}

	public final <T> T push(Parser<T> parser) {
		return push(parser, getExpectations());
	}

	public final <T> T checkFor(Parser<T> parser) {
		return checkFor(parser, getExpectations());
	}

	public final void acceptAll() {
		acceptBut(0);
	}

	public final void acceptButLast() {
		acceptBut(isEOF() ? 0 : 1);
	}

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
				(int) (this.firstUnaccepted.charOffset()
						- this.worker.pos().charOffset());
		final UnacceptedChars unacceptedChars =
				this.worker.unacceptedChars();

		for (int i = from, l = from + accept; i < l; ++i) {
			this.firstUnaccepted.move(
					unacceptedChars.offset(i),
					unacceptedChars.get(i));
		}

		if (this.firstUnaccepted == this.worker.position()) {
			unacceptedChars.accept(accept);
		}
		if (this.firstUnaccepted.charOffset() > this.current.charOffset()) {
			this.current.set(this.firstUnaccepted);
			this.pending = 0;
		}
	}

	public int next() {
		if (isEOF()) {
			return -1;
		}
		skip();

		final UnacceptedChars unacceptedChars = this.worker.unacceptedChars();

		for (;;) {

			final long idx =
					this.current.charOffset() - this.worker.pos().charOffset();
			final long offset;
			final int ch;

			if (idx < unacceptedChars.length()) {
				offset = unacceptedChars.offset((int) idx);
				ch = unacceptedChars.get((int) idx);
			} else {

				final int charCode;

				try {

					@SuppressWarnings("resource")
					final SourceReader in = this.worker.in();

					charCode = in.read();
					offset = in.offset();
				} catch (IOException e) {
					getLogger().ioError(current(), e.getLocalizedMessage());
					return -1;
				}

				if (charCode < 0) {
					this.worker.setEOF(this.current.charOffset());
					return -1;
				}

				ch = (char) charCode;
				unacceptedChars.append(offset, ch);
			}

			final int c;

			// handle newline for different OSes
			if (ch == '\n') {
				if (this.current.lastChar() == '\r') {// \r\n
					this.current.move(offset, '\n');
					continue;// handle \r\n as one \n
				}
				c = ch;
			} else if (ch == '\r') {
				c = '\n';// always handle \r as \n
			} else {
				c = ch;
			}

			this.pending = 1;
			this.pendingOffset = offset;

			return this.worker.setLastChar(c);
		}
	}

	public final boolean hasPending() {
		return this.pending > 0;
	}

	public void skip() {
		if (hasPending()) {
			this.current.move(this.pendingOffset, this.worker.lastChar());
			this.pending = 0;
		}
	}

	public final SeparatorNodes skipComments(boolean allowNewLine) {
		return push(separator(allowNewLine));
	}

	public final SeparatorNodes acceptComments(boolean allowNewLine) {
		return parse(separator(allowNewLine));
	}

	public final <T extends Node> T skipComments(
			boolean allowNewLine,
			T node) {

		final SeparatorNodes separators = skipComments(allowNewLine);

		if (separators != null) {
			node.addComments(separators);
		}

		return node;
	}

	public final <T extends Node> T acceptComments(
			boolean allowNewLine,
			T node) {

		final SeparatorNodes separators = acceptComments(allowNewLine);

		if (separators != null) {
			node.addComments(separators);
		}

		return node;
	}

	public final Position current() {
		return this.current;
	}

	public final Position firstUnaccepted() {
		return this.firstUnaccepted;
	}

	public final int unaccepted() {
		return (int) (this.current.charOffset()
				- this.firstUnaccepted.charOffset() + this.pending);
	}

	public final int lastChar() {
		return this.worker.lastChar();
	}

	public final int pendingOrNext() {
		return hasPending() ? lastChar() : next();
	}

	public final boolean isEOF() {
		return isFailed() || this.current.charOffset() >= this.worker.eof();
	}

	public final boolean isLineStart() {
		return this.current.isLineStart();
	}

	public final ParserLogger getLogger() {
		return this.log;
	}

	public final boolean hasErrors() {
		return this.hasErrors > 0;
	}

	public final Expectations getExpectations() {
		return this.expectations;
	}

	public final Expectations expectNothing() {
		return new Expectations(this);
	}

	public final Expectations expect(Parser<?> expectation) {
		return getExpectations().expect(expectation);
	}

	public final Expectations expect(char expectedChar) {
		return getExpectations().expect(expectedChar);
	}

	public final Expectations expect(String expectedString) {
		return getExpectations().expect(expectedString);
	}

	public final boolean asExpected() {
		return getExpectations().asExpected(this);
	}

	public final boolean unexpected() {
		return !isEOF() && pendingOrNext() != '\n' && !asExpected();
	}

	public final void logUnexpected(SourcePosition from, SourcePosition to) {
		if (from == null) {
			return;
		}
		getLogger().syntaxError(new SourceRange(from, to));
	}

	public void acceptUnexpected() {

		SourcePosition first = null;

		for (;;) {

			final SourcePosition start = current().fix();

			if (!unexpected()) {
				logUnexpected(first, start);
				return;
			}
			if (first == null) {
				first = start;
			}
			acceptAll();

			final SeparatorNodes comments = acceptComments(false);

			if (comments != null && comments.haveComments()) {
				logUnexpected(first, start);
				first = null;
			}
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("ParserContext[first unaccepted(");
		out.append(this.firstUnaccepted);
		out.append("), current(");
		out.append(this.current);
		if (!isLineStart()) {
			out.append(")]");
		} else {
			out.append("), line start]");
		}

		return out.toString();
	}
	protected <T> T parse(Parser<T> parser, Expectations expectations) {
		return parse(parser, expectations, this.firstUnaccepted, false);
	}

	protected <T> T push(Parser<T> parser, Expectations expectations) {
		return parse(parser, expectations, this.firstUnaccepted.clone(), false);
	}

	protected <T> T checkFor(Parser<T> parser, Expectations expectations) {
		return parse(parser, expectations, this.firstUnaccepted.clone(), true);
	}

	private boolean isFailed() {
		return this.hasErrors > 1;
	}

	private <T> T parse(
			Parser<T> parser,
			Expectations expectations,
			WorkerPos firstUnaccepted,
			boolean ignoreResult) {
		if (isEOF()) {
			return null;
		}

		final long start = firstUnaccepted.charOffset();
		final ParserContext context = new ParserContext(
				this.worker,
				parser,
				this.current,
				firstUnaccepted,
				expectations);
		final T result;
		final long accepted;

		try {
			result = parser.parse(context);
		} finally {
			if (ignoreResult) {
				accepted = 0L;
			} else {
				accepted = pop(context, start);
			}
		}

		if (!ignoreResult && accepted == 0 && result != null) {
			context.getLogger().notAccepted(current());
		}

		return result;
	}

	private long pop(ParserContext complete, long start) {

		final long accepted = complete.firstUnaccepted.charOffset() - start;

		if (accepted > 0) {
			if (complete.firstUnaccepted.charOffset()
					> this.current.charOffset()) {
				this.current.set(complete.firstUnaccepted);
				this.pending = 0;
			}
		}
		if (complete.hasErrors > this.hasErrors) {
			this.hasErrors = complete.hasErrors;
		}

		return accepted;
	}

	final void setCurrent(WorkerPos position) {
		this.current.set(position);
	}

	private final class Log extends ParserLogger {

		private final ParserContext context;

		Log(ParserContext context) {
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
			return this.context.worker.getLogger();
		}

		@Override
		protected Object getSource() {
			return this.context.parser.getClass().getSimpleName();
		}

	}

}
