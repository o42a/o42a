/*
    Compiler Core
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
package org.o42a.core.source;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.Visibility;
import org.o42a.util.log.*;


public class CompilerLogger implements Logger {

	public static LogReason logDeclaration(LogInfo location) {
		return new LogReason(
				"compiler.declaration",
				"Declaration is ",
				location);
	}

	public static LogReason anotherDeclaration(LogInfo location) {
		return new LogReason(
				"compiler.declaration",
				"Another declaration ",
				location);
	}

	private final Logger logger;
	private final Object source;

	public CompilerLogger(Logger logger, Object source) {
		this.logger = logger != null ? logger : Logger.DEFAULT_LOGGER;
		this.source = source;
	}

	public void abstractNotOverridden(LogInfo locaion, String fieldName) {
		error(
				"abstract_not_overridden",
				locaion,
				"Abstract field '%s' not overridden",
				fieldName);
	}

	public void abstractValue(LogInfo location) {
		error("abstract_value", location, "Abstract value access");
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

	public void ambiguousValue(LogInfo location) {
		error("ambiguous_value", location, "Ambiguous value declaration");
	}

	public void arithmeticError(LogInfo location, String message) {
		error("arithmetic_error", location, "Arithmetic error: %s", message);
	}

	public void cantDeclareOverridden(LogInfo location, String fieldName) {
		error(
				"cant_declare_overridden",
				location,
				"Can not declare already overridden field '%s'",
				fieldName);
	}

	public void cantInherit(LogInfo location, Object target) {
		error("cant_inherit", location, "%s can not be inherited", target);
	}

	public void cantOverrideDeclared(LogInfo location, String fieldName) {
		error(
				"cant_override_declared",
				location,
				"Can not override already declared field '%s'",
				fieldName);
	}

	public void cantOverrideUnknown(LogInfo location, String fieldName) {
		error(
				"cant_override_unknown",
				location,
				"Can not override unknown field '%s'",
				fieldName);
	}

	public void expectedClauseName(LogInfo location) {
		error("expected_clause_name", location, "Clause name expected here");
	}

	public void expectedDefinition(LogInfo location) {
		error("expected_definition", location, "Definition expected here");
	}

	public void forbiddenAccess(LogInfo location, Object target) {
		error(
				"forbidden_access",
				location,
				"Access to %s is forbidden",
				target);
	}

	public void illegalVisibility(LogInfo location) {
		error(
				"illegal_visibility",
				location,
				"Illegal field visibility qualifier");
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

	public void invalidArtifact(LogInfo location) {
		error("invalid_artifact", location, "Artifact is not valid");
	}

	public void invalidClause(LogInfo location) {
		error("invalid_clause", location, "Invalid clause");
	}

	public void invalidClauseContent(LogInfo location) {
		error("invalid_clause_content", location, "Invalid clause content");
	}

	public void invalidClauseReused(LogInfo location) {
		error(
				"invalid_clause_reused",
				location,
				"Attempt to reuse inaccessible clause");
	}

	public void invalidDeclaration(LogInfo location) {
		error("invalid_declaration", location, "Invalid declaration");
	}

	public void invalidDefinition(LogInfo location) {
		error("invalid_definition", location, "Not a valid member definition");
	}

	public void invalidExpression(LogInfo location) {
		error("invalid_expression", location, "Not a valid expression");
	}

	public void invalidInteger(LogInfo location, String reason) {
		error(
				"invalid_integer",
				location,
				"Invalid integer literal %s ",
				reason);
	}

	public void invalidPhrasePrefix(LogInfo location) {
		error("invalid_phrase_prefix", location, "Invalid phrase prefix");
	}

	public void invalidReference(LogInfo location) {
		error("invalid_reference", location, "Not a valid reference");
	}

	public void invalidStatement(LogInfo location) {
		error("invalid_statement", location, "Not a valid statement");
	}

	public void invalidType(LogInfo location) {
		error("invalid_type", location, "Not a valid type reference");
	}

	public void noClauseTarget(LogInfo location) {
		error("no_clause_target", location, "Clause has no target");
	}

	public void noDefinition(LogInfo location) {
		error("no_definition", location, "Definition is missing");
	}

	public void noModuleNoObject(LogInfo location) {
		error(
				"no_module_no_object",
				location,
				"Either module or object should be specified");
	}

	public void notAbstract(LogInfo location) {
		error("not_abstract", location, "Abstract declaration expected");
	}

	public void notAdapter(LogInfo location) {
		error("not_adapter", location, "Adapter declaration expected");
	}

	public void notArrayItemInitializer(LogInfo location) {
		error(
				"not_array_item_initializer",
				location,
				"Array item initializer expected");
	}

	public void notArtifact(LogInfo location) {
		error("not_artifact", location, "Not an artifact");
	}

	public void notClause(LogInfo location) {
		error("not_clause", location, "Not clause");
	}

	public void notClauseDeclaration(LogInfo location) {
		error("not_clause_declaration", location, "Not a clause declaration");
	}

	public void notCondition(LocationInfo location) {
		error("not_condition", location, "Not a condition");
	}

	public void notDerivedFrom(LogInfo location, Object ascendant) {
		error("not_deried_from", location, "Not derived from %s", ascendant);
	}

	public void notFloat(LogInfo location, String literal) {
		error("not_float", location, "Not a floating point value: %s", literal);
	}

	public void notInteger(LogInfo location, String literal) {
		error("not_integer", location, "Not an integer: %s", literal);
	}

	public void notPath(LogInfo location) {
		error("not_path", location, "Not a path");
	}

	public void notPrototype(LogInfo location) {
		error("not_prototype", location, "Prototype declaration expected");
	}

	public void notReproducible(LogInfo location) {
		error("not_reproducible", location, "Not reproducible");
	}

	public void notObject(LogInfo location, Object target) {
		error("not_object", location, "%s is not an object", target);
	}

	public void notObjectDeclaration(LogInfo location) {
		error(
				"not_object_declaration",
				location,
				"Object declaration expected");
	}

	public void notTypeRef(LogInfo location) {
		error("not_type_ref", location, "Not a valid type reference");
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

	public void prohibitedDeclaration(LogInfo location) {
		error(
				"prohibited_declaration",
				location,
				"Declarations prohibited here");
	}

	public void prohibitedDeclaredIn(LogInfo location) {
		error(
				"prohibited_declared_in",
				location,
				"Field scope declaration is prohibited here");
	}

	public void prohibitedDeclarativeEllipsis(LogInfo location) {
		error(
				"prohibited_declarative_ellipsis",
				location,
				"Ellipsis is only allowed within imperative block");
	}

	public void prohibitedDefinition(LogInfo location) {
		error(
				"prohibited_definition",
				location,
				"Definition is prohibited here");
	}

	public void prohibitedDirective(LogInfo location, String directiveName) {
		error(
				"prohibited_directive",
				location,
				"Directive '%s' is prohibited here",
				directiveName);
	}

	public void prohibitedLinkType(LogInfo location) {
		error(
				"prohibited_link_type",
				location,
				"Link type is not expected here");
	}

	public void prohibitedLocal(LogInfo location) {
		error(
				"prohibited_local",
				location,
				"Local scope declaration is prohibited here");
	}

	public void prohibitedLocalAbstract(LogInfo location, String fieldName) {
		error(
				"prohibited_local_abstract",
				location,
				"Local field '%s' can no be abstract",
				fieldName);
	}

	public void prohibitedLocalAdapter(LogInfo location) {
		error("prohibited_local_adapter", location, "Adapter can not be local");
	}

	public void prohibitedLocalVisibility(LogInfo location, String fieldName) {
		error(
				"prohibited_local_visibility",
				location,
				"Local field '%s' can not have visibility",
				fieldName);
	}

	public void prohibitedOverriderClause(LogInfo location) {
		error(
				"prohibited_overrider_clause",
				location,
				"Overrider clause is prohibited here");
	}

	public void prohibitedPrivateAbstract(LogInfo location, String fieldName) {
		error(
				"prohibited_private_abstract",
				location,
				"Private field '%s' can not be abstract",
				fieldName);
	}

	public void prohibitedPrototype(LogInfo location) {
		error(
				"prohibited_prototype",
				location,
				"Field can not be declared as prototype");
	}

	public void prohibitedSamples(LogInfo location) {
		error("prohibited_samples", location, "Samples are prohibited here");
	}

	public void prohibitedVariableType(LogInfo location) {
		error(
				"prohibited_variable_type",
				location,
				"Variable type is not expected here");
	}

	public void recursiveResolution(LogInfo location, Object resolvable) {
		error(
				"recursive_resolution",
				location,
				"Infinite recursion when resolving %s",
				resolvable);
	}

	public void requiredInitializer(LogInfo location) {
		error("required_initializer", location, "Initializer required here");
	}

	public void unavailableSource(
			LogInfo location,
			String sourceName,
			String reason) {
		error(
				"unavailable_source",
				location,
				"Source '%s' can not be opened: %s",
				sourceName,
				reason);
	}

	public void unexpectedAbstract(LogInfo location) {
		error(
				"unexpected_abstract",
				location,
				"Unexpected abstract declaration");
	}

	public void unexpectedAbsolutePath(LogInfo location) {
		error(
				"unexpected_absolute_path",
				location,
				"Relative path expected here");
	}

	public void unexpectedAdapter(LogInfo location) {
		error("unexpected_adapter", location, "Unexpected adapter declaration");
	}

	public void unexpectedArrayDimension(
			LogInfo location,
			int actual,
			int expected) {
		error(
				"unexpected_array_dimension",
				location,
				"Unexpected array dimension: %d, but %d expected",
				actual,
				expected);
	}

	public void unexpectedPrototype(LogInfo location) {
		error(
				"unexpected_prototype",
				location,
				"Unexpected prototype declaration");
	}

	public void unexpectedType(
			LogInfo location,
			Object actual,
			Object expected) {
		error(
				"unexpected_type",
				location,
				"Unexpected type: %s, but %s expected",
				actual,
				expected);
	}

	public void unexpectedVisibility(
			LogInfo location,
			String fieldName,
			Visibility actual,
			Visibility expected) {
		error(
				"unexpected_visibility",
				location,
				"Wrong '%s' field visibility: %s, but %s expected",
				fieldName,
				actual,
				expected);
	}

	public void unresolved(LogInfo location, Object target) {
		error("unresolved", location, "'%s' can not be resolved", target);
	}

	public void unresolvedModule(LogInfo location, String moduleId) {
		error(
				"unresolved_module",
				location,
				"Module <%s> can not be resolved",
				moduleId);
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

	public void wrongArtifactKind(
			LogInfo location,
			ArtifactKind<?> actual,
			ArtifactKind<?> expected) {
		error(
				"wrong_artifact_kind",
				location,
				"Wrong kind of artifact: %s, but %s expected",
				actual,
				expected);
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

	protected Logger getLogger() {
		return this.logger;
	}

	protected Object getSource() {
		return this.source;
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
				"compiler." + code,
				defaultMessage,
				location.getLoggable(),
				args));
	}

}
