/*
    Compiler Core
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
package org.o42a.core.source;

import org.o42a.util.log.*;


public class CompilerLogger implements Logger {

	private final Logger logger;
	private final Object source;

	public CompilerLogger(Logger logger, Object source) {
		this.logger = logger != null ? logger : Logger.DEFAULT_LOGGER;
		this.source = source;
	}

	public CompilerLogger(CompilerLogger logger) {
		this.logger = logger;
		this.source = logger.getSource();
	}

	public void ambiguousClause(LogInfo location, String clauseName) {
		error(
				"ambiguous_clause",
				location,
				"Clause '%s' declaration is ambiguous",
				clauseName);
	}

	public void ambiguousMember(LogInfo location, String fieldName) {
		error(
				"ambiguous_member",
				location,
				"Member '%s' declaration is ambiguous",
				fieldName);
	}

	public void arithmeticError(LogInfo location, String message) {
		error("arithmetic_error", location, "Arithmetic error: %s", message);
	}

	public void cantInherit(LogInfo location, Object target) {
		error("cant_inherit", location, "%s can not be inherited", target);
	}

	public void forbiddenAccess(LogInfo location, Object target) {
		error(
				"forbidden_access",
				location,
				"Access to %s is forbidden",
				target);
	}

	public void ignored(LogInfo location) {
		warning("ignored", location, "Statement ignored");
	}

	public void incompatible(LogInfo location, Object type) {
		error("incompatible", location, "Not compatible with %s", type);
	}

	public void indefiniteValue(LogInfo location) {
		error("indefinite_value", location, "Indefinite value access");
	}

	public void invalidDeclaration(LogInfo location) {
		error("invalid_declaration", location, "Invalid declaration");
	}

	public void invalidReference(LogInfo location) {
		error("invalid_reference", location, "Not a valid reference");
	}

	public void noValue(LogInfo location) {
		error("no_value", location, "Value is missing");
	}

	public void noDefinition(LogInfo location) {
		error("no_definition", location, "Definition is missing");
	}

	public void notAbstract(LogInfo location) {
		error("not_abstract", location, "Abstract declaration expected");
	}

	public void notAdapter(LogInfo location) {
		error("not_adapter", location, "Adapter declaration expected");
	}

	public void notClause(LogInfo location) {
		error("not_clause", location, "Not clause");
	}

	public void notClauseDeclaration(LogInfo location) {
		error("not_clause_declaration", location, "Not a clause declaration");
	}

	public void notCondition(LogInfo location) {
		error("not_condition", location, "Not a condition");
	}

	public void notDerivedFrom(LogInfo location, Object ascendant) {
		error("not_deried_from", location, "Not derived from %s", ascendant);
	}

	public void notReproducible(LogInfo location) {
		error("not_reproducible", location, "Not reproducible");
	}

	public void notObject(LogInfo location, Object target) {
		error("not_object", location, "%s is not an object", target);
	}

	public void prohibitedAbstract(LogInfo location, String fieldName) {
		error(
				"prohibited_abstract",
				location,
				"Field '%s' can no be declared abstract, because "
				+  "it's not inside of prototype or another abstract field",
				fieldName);
	}

	public void prohibitedClauseDeclaration(LogInfo location) {
		error(
				"prohibited_clause_declaration",
				location,
				"Clause declarations prohibited here");
	}

	public void prohibitedDeclaredIn(LogInfo location) {
		error(
				"prohibited_declared_in",
				location,
				"Field scope declaration is prohibited here");
	}

	public void prohibitedDirective(LogInfo location, String directiveName) {
		error(
				"prohibited_directive",
				location,
				"Directive '%s' is prohibited here",
				directiveName);
	}

	public void syntaxError(LogInfo location) {
		error("syntax_error", location, "Syntax error");
	}

	public void unexpectedAbstract(LogInfo location) {
		error(
				"unexpected_abstract",
				location,
				"Unexpected abstract declaration");
	}

	public void unexpectedAdapter(LogInfo location) {
		error("unexpected_adapter", location, "Unexpected adapter declaration");
	}

	public void unresolved(LogInfo location, Object target) {
		error("unresolved", location, "'%s' can not be resolved", target);
	}

	public void unresolvedScope(LogInfo location, String scope) {
		error("unresolved_scope", location, "Unresolved scope: %s", scope);
	}

	public void unresolvedValue(LogInfo location, Object valuable) {
		error(
				"unresolved_value",
				location,
				"Value of '%s' can not be resolved",
				valuable);
	}

	public final void error(
			String code,
			LocationInfo location,
			String defaultMessage,
			Object... args) {
		error(
				code,
				location.getLocation().getLoggable(),
				defaultMessage,
				args);
	}

	public final void error(
			String code,
			Location location,
			String defaultMessage,
			Object... args) {
		error(code, location.getLoggable(), defaultMessage, args);
	}

	public final void error(
			String code,
			LogInfo location,
			String defaultMessage,
			Object... args) {
		log(Severity.ERROR, code, location, defaultMessage, args);
	}

	public final void warning(
			String code,
			LocationInfo location,
			String defaultMessage,
			Object... args) {
		warning(
				code,
				location.getLocation().getLoggable(),
				defaultMessage,
				args);
	}

	public final void warning(
			String code,
			Location location,
			String defaultMessage,
			Object... args) {
		warning(code, location.getLoggable(), defaultMessage, args);
	}

	public final void warning(
			String code,
			LogInfo location,
			String defaultMessage,
			Object... args) {
		log(Severity.WARNING, code, location, defaultMessage, args);
	}

	public final void log(
			Severity severity,
			String code,
			LogInfo location,
			String defaultMessage,
			Object... args) {
		log(new LogRecord(
				severity,
				"compiler." + code,
				defaultMessage,
				location.getLoggable(),
				args));
	}

	@Override
	public void log(LogRecord record) {
		getLogger().log(record);
	}

	protected Logger getLogger() {
		return this.logger;
	}

	protected Object getSource() {
		return this.source;
	}

}
