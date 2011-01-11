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

import org.o42a.util.log.*;


public abstract class ParserLogger implements Logger {

	public void cantAccept(LogInfo location, int acceptingBut, int charsLeft) {
		warning(
				"cant_accept",
				"Can not accept all but %d"
				+ " characters: only %d"
				+ " pending characters left",
				location,
				acceptingBut,
				charsLeft);
	}

	public void discouragingSoftHyphen(LogInfo location) {
		warning(
				"discouraging_soft_hyphen",
				"Discouraging soft hyphen",
				location);
	}

	public void discouragingWhitespace(LogInfo location) {
		warning(
				"discouraging_whitespace",
				"Discouraging whitespace",
				location);
	}

	public void emptyAlternative(LogInfo location) {
		warning("empty_alternative", "Empty alternative", location);
	}

	public void emptyOpposite(LogInfo location) {
		error("empty_opposite", "Opposite is empty", location);
	}

	public void emptyStatement(LogInfo location) {
		warning("empty_statement", "Empty statement", location);
	}

	public void eof(LogInfo location) {
		fatal("eof", "Unexpected end of file", location);
	}

	public void invalidDefinition(LogInfo location) {
		fatal("invalid_definition", "Invalid definition", location);
	}

	public void invalidEllipsisTarget(LogInfo location) {
		error(
				"invalid_ellipsis_target",
				"Invalid target: expected either block name or nothing",
				location);
	}

	public void invalidSpaceInNumber(LogInfo location) {
		warning(
				"invalid_space_in_number",
				"Only a single space character allowed in number",
				location);
	}

	public void ioError(LogInfo location, String message) {
		fatal("io_error", "I/O error: %s", location, message);
	}

	public void missingClause(LogInfo location) {
		error(
				"missing_clause",
				"Clause reference is missing",
				location);
	}

	public void missingDeclaredIn(LogInfo location) {
		error(
				"missing_declared_in",
				"Declaration reference is missing",
				location);
	}

	public void missingOperand(LogInfo location, String operator) {
		error(
				"missing_operand",
				"Operand of operator '%s' is missing",
				location,
				operator);
	}

	public void missingRightOperand(LogInfo location, String operator) {
		error(
				"missing_right_operand",
				"Right operand of operator '%s' is missing",
				location,
				operator);
	}

	public void missingSample(LogInfo location) {
		error("missing_sample", "Sample reference is missing", location);
	}

	public void missingType(LogInfo location) {
		error("missing_type", "Type is missing", location);
	}

	public void missingValue(LogInfo location) {
		error("missing_value", "Value is missing", location);
	}

	public void notAccepted(LogInfo location) {
		warning(
				"not_accepted",
				"Result produced, but no characters accepted",
				location);
	}

	public void notClosed(LogInfo location, String brace) {
		error("not_closed", "'%s' not closed", location, brace);
	}

	public void oppositeToEmpty(LogInfo location) {
		warning("opposite_to_empty", "Opposite to empty sentence", location);
	}

	public void syntaxError(LogInfo location) {
		error("syntax_error", "Syntax error", location);
	}

	public void unrecognizedEscapeSequence(LogInfo location, String sequence) {
		error(
				"unrecognized_escape_sequence",
				"Unrecognized escape sequence: %s",
				location,
				sequence);
	}

	public void unrecognizedSentence(LogInfo location) {
		fatal("unrecognized_sentence", "Unrecognized sentence", location);
	}

	public void unterminatedStringLiteral(LogInfo location) {
		error(
				"unterminated_string_literal",
				"Unterminated string literal",
				location);
	}

	public void unterminatedUnicodeEscapeSequence(LogInfo location) {
		error(
				"unterminated_unicode_escape_sequence",
				"Unterminated unicode escape sequence",
				location);
	}

	@Override
	public void log(LogRecord record) {
		getLogger().log(record);
	}

	protected abstract Object getSource();

	protected abstract Logger getLogger();

	private final void fatal(
			String code,
			String defaultMessage,
			LogInfo location,
			Object... args) {
		log(Severity.FATAL, code, defaultMessage, location, args);
	}

	private final void error(
			String code,
			String defaultMessage,
			LogInfo location,
			Object... args) {
		log(Severity.ERROR, code, defaultMessage, location, args);
	}

	private final void warning(
			String code,
			String defaultMessage,
			LogInfo location,
			Object... args) {
		log(Severity.WARNING, code, defaultMessage, location, args);
	}

	private final void log(
			Severity severity,
			String code,
			String defaultMessage,
			LogInfo location,
			Object... args) {
		log(new LogRecord(
				getSource(),
				severity,
				"parser." + code,
				defaultMessage,
				location.getLoggable(),
				args));
	}

}
