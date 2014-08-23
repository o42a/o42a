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

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;
import org.o42a.util.io.SourceReader;


public class ConsoleLogger implements Logger {

	private int columns;

	public ConsoleLogger(int columns) {
		this.columns = columns;
	}

	public int getColumns() {
		return this.columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	@Override
	public void log(LogRecord record) {

		final Formatter formatter = new Formatter();

		try {
			formatter.format(record.getMessage().getText(), record.getArgs());
			formatLocation(formatter, record.getLoggable().getLocation());
			formatDetails(formatter, record);
			System.err.print(formatter.toString());
		} finally {
			formatter.close();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[cols: " + this.columns + ']';
	}

	private void formatLocation(Formatter formatter, LogLocation location) {

		final SourceRange range = location.getRange();

		if (range != null) {
			formatRange(formatter, range);
			return;
		}

		final SourcePosition position = location.getPosition();

		if (position != null) {
			formatPosition(formatter, position);
			return;
		}

		formatter.format(" at %s\n", location.getSource());
	}

	private void formatPosition(Formatter formatter, SourcePosition position) {
		formatLine(formatter, position, -1);
	}

	private void formatRange(Formatter formatter, SourceRange range) {

		final SourcePosition start = range.getStart();
		final SourcePosition end = range.getEnd();
		final int lastColumn;

		if (start.getLine() == end.getLine()
				&& start.getSource().equals(end.getSource())) {

			final int endColumn = end.getColumn();

			if (endColumn == start.getColumn()) {
				lastColumn = -1;
			} else {
				lastColumn = endColumn;
			}
		} else {
			lastColumn = Integer.MAX_VALUE;
		}

		formatLine(formatter, start, lastColumn);
	}

	private void formatLine(
			Formatter formatter,
			SourcePosition position,
			int lastColumn) {

		final String lineStr = Integer.toString(position.getLine());
		final int indent = lineStr.length() + 6;
		int firstColumn = -1;
		CharSequence sourceLine;

		try {

			final SourceLine line = sourceLine(
					position,
					indent,
					lastColumn <= 0 ? Integer.MAX_VALUE : lastColumn - 1);

			sourceLine = line.line;
			firstColumn = line.firstColumn;
		} catch (Throwable e) {

			final String message = e.getMessage();

			sourceLine =
					"ERROR opening source file: "
					+ (message != null ? message : e.toString());
		}

		formatter.format(
				" at %s\n    %s: %s\n",
				position.getSource(),
				lineStr,
				sourceLine);

		if (firstColumn < 0) {
			return;
		}

		// Indent for column.
		final int spaces = position.getColumn() - 1 - firstColumn;

		printChars(formatter, indent + spaces, ' ');

		final int colsLeft = sourceLineCols(indent) - spaces;

		if (lastColumn <= 0) {
			// Print a single position.
			formatter.format("^ %d\n", position.getColumn());
		}

		// Print the range.
		final String startStr = Integer.toString(position.getColumn());
		final int colsForStart = startStr.length() + 3;
		final int colsForRest = colsLeft - colsForStart;
		final int colsToEnd =
				lastColumn - 2 - firstColumn - (spaces + colsForStart);

		if (colsToEnd <= 0) {
			if (colsToEnd == 0) {
				// Short range.
				// Format: ^ <start column> ^ <end column>
				formatter.format("^ %s ^ %d\n", startStr, lastColumn);
				return;
			}

			final int colsToEnd2 =
					lastColumn - 2 - firstColumn - (spaces + 1);

			// Very short range.
			// Format: ^_^ <start column> - <end column>
			// or    : ^ <start column> - <end column>
			formatter.format("^");
			if (colsToEnd2 >= 0) {
				printChars(formatter, colsToEnd2, '_');
				formatter.format("^");
			}
			formatter.format(" %s - %d\n", startStr, lastColumn);
			return;
		}

		formatter.format("^ %s ", startStr);
		if (colsToEnd >= colsForRest) {
			// The end column can not be displayed.
			// Fill the rest of the line with underscores.
			// Format: ^ <start column> ____________
			printChars(formatter, colsForRest, '_');
			formatter.format("\n");
			return;
		}

		// Print the range tail.
		final String endStr = Integer.toString(lastColumn);
		final int colsForEnd = endStr.length() + 1;
		final int underscores = colsToEnd - colsForEnd;

		if (underscores >= 0) {
			// Print the end column number before the '^' sign.
			// Format: ^ <start column> _____ <end column> ^
			printChars(formatter, underscores - 1, '_');
			if (underscores > 0) {
				formatter.format(" %s ^\n", endStr);
			} else {
				formatter.format("%s ^\n", endStr);
			}
			return;
		}

		// No place for the end column number.
		// Print it after the '^' sign.
		// Format: ^ <start column> ________ ^ <end column>
		printChars(formatter, colsToEnd - 1, '_');
		formatter.format(" ^ %s\n", endStr);
	}

	private void formatDetails(Formatter formatter, LogRecord record) {

		final LogDetails details = record.getLoggable().toDetails();

		if (details == null) {
			return;
		}

		for (LogDetail detail : details) {
			formatDetail(formatter, detail, details.detail(detail));
		}
	}

	private void formatDetail(
			Formatter formatter,
			LogDetail detail,
			LogLocation location) {
		formatter.format("  %s", detail.getMessage());
		formatLocation(formatter, location);
	}

	private SourceLine sourceLine(
			SourcePosition position,
			int indent,
			int lastColumn)
	throws IOException {

		final int lineCols = sourceLineCols(indent);
		int[] line = new int[position.getColumn() + lineCols];
		int column = 0;

		final SourceReader in = position.getSource().open();

		try {

			final long offset = position.getOffset();

			// Position input roughly at the line start.
			final long roughLineStart = offset - position.getColumn() * 4;

			if (roughLineStart >= 0) {
				in.seek(roughLineStart);
			}

			// Move to the offset and fill the line.
			while (offset > in.offset()) {

				final int c = in.read();

				if (c < 0) {
					throw new EOFException("Invalid file offset: " + offset);
				}
				if (c == '\n') {
					// New line. Start over.
					column = 0;
					continue;
				}
				if (column >= line.length) {
					line = Arrays.copyOf(line, line.length + lineCols);
				}
				line[column++] = c;
			}

			// Fill the rest of the line.
			final int maxLen;

			if (lastColumn < lineCols) {
				maxLen = lineCols;
			} else {
				maxLen = column + lineCols;
			}

			for (;;) {

				final int c = in.read();

				if (c < 0 || c == '\n') {
					break;
				}
				if (column >= maxLen) {
					break;
				}
				if (column >= line.length) {
					line = Arrays.copyOf(line, line.length + lineCols);
				}
				line[column++] = c;
			}
		} finally {
			in.close();
		}

		// Remove redundant leading chars and convert to string.
		final int redundantChars = column - lineCols;
		final int firstColumn;
		final StringBuilder out;

		if (redundantChars > 0) {
			out = new StringBuilder(lineCols);
			firstColumn = redundantChars;
		} else {
			out = new StringBuilder(column);
			firstColumn = 0;
		}

		for (int i = firstColumn; i < column; ++i) {
			out.appendCodePoint(line[i]);
		}

		return new SourceLine(out, firstColumn);
	}

	private int sourceLineCols(int indent) {

		final int freeColumns = getColumns() - indent;

		if (freeColumns > 10) {
			return freeColumns;
		}

		return 80 - indent;
	}

	private static void printChars(Formatter formatter, int numChars, char c) {
		if (numChars > 0) {

			final char[] chars = new char[numChars];

			Arrays.fill(chars, c);

			formatter.format(new String(chars));
		}
	}

	private final class SourceLine {

		private final CharSequence line;
		private final int firstColumn;

		public SourceLine(CharSequence line, int firstColumn) {
			this.line = line;
			this.firstColumn = firstColumn;
		}

	}

}
