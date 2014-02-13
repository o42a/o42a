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

import org.o42a.util.log.*;


public abstract class ParserLogger implements Logger {

	public void cantAccept(LogInfo location, int acceptingBut, int charsLeft) {
		warning(
				"cant_accept",
				location,
				"Can not accept all but %d"
				+ " characters: only %d"
				+ " pending characters left",
				acceptingBut,
				charsLeft);
	}

	public void discouragingWhitespace(LogInfo location) {
		warning(
				"discouraging_whitespace",
				location,
				"Discouraging whitespace");
	}

	public void emptyAlternative(LogInfo location) {
		warning("empty_alternative", location, "Empty alternative");
	}

	public void emptyOpposite(LogInfo location) {
		error("empty_opposite", location, "Opposite is empty");
	}

	public void emptyStatement(LogInfo location) {
		warning("empty_statement", location, "Empty statement");
	}

	public void eof(LogInfo location) {
		fatal("eof", location, "Unexpected end of file");
	}

	public void invalidDefinition(LogInfo location) {
		fatal("invalid_definition", location, "Invalid definition");
	}

	public void invalidEllipsisTarget(LogInfo location) {
		error(
				"invalid_ellipsis_target",
				location,
				"Invalid target: expected either block name or nothing");
	}

	public void ioError(LogInfo location, String message) {
		fatal("io_error", location, "I/O error: %s", message);
	}

	public void missingClause(LogInfo location) {
		error(
				"missing_clause",
				location,
				"Clause reference is missing");
	}

	public void missingInclusionTag(LogInfo location) {
		error(
				"missing_inclusion_tag",
				location,
				"Inclusion tag is missing");
	}

	public void missingOperand(LogInfo location, String operator) {
		error(
				"missing_operand",
				location,
				"Operand of operator '%s' is missing",
				operator);
	}

	public void missingRightOperand(LogInfo location, String operator) {
		error(
				"missing_right_operand",
				location,
				"Right operand of operator '%s' is missing",
				operator);
	}

	public void missingValue(LogInfo location) {
		error("missing_value", location, "Value is missing");
	}

	public void notClosed(LogInfo location, String brace) {
		error("not_closed", location, "'%s' not closed", brace);
	}

	public void odd(LogInfo location) {
		error("odd", location, "No expressions allowed here");
	}

	public void oppositeToEmpty(LogInfo location) {
		warning("opposite_to_empty", location, "Opposite to empty sentence");
	}

	public void syntaxError(LogInfo location) {
		error("syntax_error", location, "Syntax error");
	}

	public void unrecognizedEscapeSequence(LogInfo location, String sequence) {
		error(
				"unrecognized_escape_sequence",
				location,
				"Unrecognized escape sequence: %s",
				sequence);
	}

	public void unrecognizedSentence(LogInfo location) {
		fatal("unrecognized_sentence", location, "Unrecognized sentence");
	}

	public void unterminatedStringLiteral(LogInfo location) {
		error(
				"unterminated_string_literal",
				location,
				"Unterminated string literal");
	}

	public void unterminatedUnicodeEscapeSequence(LogInfo location) {
		error(
				"unterminated_unicode_escape_sequence",
				location,
				"Unterminated unicode escape sequence");
	}

	public final void fatal(
			String code,
			LogInfo location,
			String defaultMessage,
			Object... args) {
		log(Severity.FATAL, code, defaultMessage, location, args);
	}

	public final void error(
			String code,
			LogInfo location,
			String defaultMessage,
			Object... args) {
		log(Severity.ERROR, code, defaultMessage, location, args);
	}

	public final void warning(
			String code,
			LogInfo location,
			String defaultMessage,
			Object... args) {
		log(Severity.WARNING, code, defaultMessage, location, args);
	}

	@Override
	public void log(LogRecord record) {
		getLogger().log(record);
	}

	protected abstract Object getSource();

	protected abstract Logger getLogger();

	private final void log(
			Severity severity,
			String code,
			String defaultMessage,
			LogInfo location,
			Object... args) {
		log(new LogRecord(
				severity,
				"parser." + code,
				defaultMessage,
				location.getLoggable(),
				args));
	}

}
