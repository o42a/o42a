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
package org.o42a.core;

import org.o42a.core.member.Visibility;
import org.o42a.util.log.*;


public class CompilerLogger implements Logger {

	private final Logger logger;
	private final Object source;

	public CompilerLogger(Logger logger, Object source) {
		this.logger = logger != null ? logger : Logger.DEFAULT_LOGGER;
		this.source = source;
	}

	public void abstractNotOverridden(LogInfo locaion, String fieldName) {
		error(
				"abstract_not_overridden",
				"Abstract field '%s' not overridden",
				locaion,
				fieldName);
	}

	public void abstractValue(LogInfo location) {
		error("abstract_value", "Abstract value access", location);
	}

	public void ambiguousClause(LogInfo location, String clauseName) {
		error(
				"ambiguous_clause",
				"Clause '%s' declaration is ambiguous",
				location,
				clauseName);
	}

	public void ambiguousField(LogInfo location, String fieldName) {
		error(
				"ambiguousField",
				"Field '%s' declaration is ambiguous",
				location,
				fieldName);
	}

	public void ambiguousValue(LogInfo location) {
		error("ambiguous_value", "Ambiguous value declaration", location);
	}

	public void arithmeticError(LogInfo location, String message) {
		error("arithmetic_error", "Arithmetic error: %s", location, message);
	}

	public void cantDeclareOverridden(LogInfo location, String fieldName) {
		error(
				"cant_declare_overridden",
				"Can not declare already overridden field '%s'",
				location,
				fieldName);
	}

	public void cantInherit(LogInfo location, Object target) {
		error("cant_inherit", "%s can not be inherited", location, target);
	}

	public void cantOverrideDeclared(LogInfo location, String fieldName) {
		error(
				"cant_override_declared",
				"Can not override already declared field '%s'",
				location,
				fieldName);
	}

	public void cantOverrideUnknown(LogInfo location, String fieldName) {
		error(
				"cant_override_unknown",
				"Can not override unknown field '%s'",
				location,
				fieldName);
	}

	public void dublicateBlockName(LogInfo location, String blockName) {
		error(
				"duplicate_block_name",
				"Block '%s' already declared",
				location,
				blockName);
	}

	public void expectedClauseName(LogInfo location) {
		error("expected_clause_name", "Clause name expected here", location);
	}

	public void expectedDeclaration(LogInfo location) {
		error("expected_declaration", "Declaration expected here", location);
	}

	public void forbiddenAccess(LogInfo location, Object target) {
		error(
				"forbidden_access",
				"Access to %s is forbidden",
				location,
				target);
	}

	public void illegalVisibility(LogInfo location) {
		error(
				"illegal_visibility",
				"Illegal field visibility qualifier",
				location);
	}

	public void ignored(LogInfo location) {
		warning("ignored", "Statement ignored", location);
	}

	public void incompatible(LogInfo location, Object type) {
		error("incompatible", "Not compatible with %s", location, type);
	}

	public void indefiniteValue(LogInfo location) {
		error("indefinite_value", "Indefinite value access", location);
	}

	public void invalidArtifact(LogInfo location) {
		error("invalid_artifact", "Artifact is not valid", location);
	}

	public void invalidClause(LogInfo location) {
		error("invalid_clause", "Invalid clause", location);
	}

	public void invalidClauseContent(LogInfo location) {
		error("invalid_clause_content", "Invalid clause content", location);
	}

	public void invalidClauseReused(LogInfo location) {
		error(
				"invalid_clause_reused",
				"Attempt to reuse inaccessible clause",
				location);
	}

	public void invalidDeclaration(LogInfo location) {
		error("invalid_declaration", "Invalid declaration", location);
	}

	public void invalidDefinition(LogInfo location) {
		error("invalid_definition", "Not a valid member definition", location);
	}

	public void invalidExpression(LogInfo location) {
		error("invalid_expression", "Not a valid expression", location);
	}

	public void invalidInteger(LogInfo location, String reason) {
		error(
				"invalid_integer",
				"Invalid integer literal %s ",
				location,
				reason);
	}

	public void invalidOverridden(LogInfo location) {
		error("invalid_overridden", "Only field can be overridden", location);
	}

	public void invalidPhrasePrefix(LogInfo location) {
		error("invalid_phrase_prefix", "Invalid phrase prefix", location);
	}

	public void invalidReference(LogInfo location) {
		error("invalid_reference", "Not a valid reference", location);
	}

	public void invalidStatement(LogInfo location) {
		error("invalid_statement", "Not a valid statement", location);
	}

	public void invalidType(LogInfo location) {
		error("invalid_type", "Not a valid type reference", location);
	}

	public void noClauseTarget(LogInfo location) {
		error("no_clause_target", "Clause has no target", location);
	}

	public void noDefinition(LogInfo location) {
		error("no_definition", "Definition is missing", location);
	}

	public void noModuleNoObject(LogInfo location) {
		error(
				"no_module_no_object",
				"Either module or object should be specified",
				location);
	}

	public void noName(LogInfo location) {
		error("no_name", "Name not specified", location);
	}

	public void notAbstract(LogInfo location) {
		error("not_abstract", "Abstract declaration expected", location);
	}

	public void notAdapter(LogInfo location) {
		error("not_adapter", "Adapter declaration expected", location);
	}

	public void notArray(LogInfo location) {
		error("not_array", "Not array", location);
	}

	public void notArrayItemInitializer(LogInfo location) {
		error(
				"not_array_item_initializer",
				"Array item initializer expected",
				location);
	}

	public void notClause(LogInfo location) {
		error("not_clause", "Not clause", location);
	}

	public void notClauseDeclaration(LogInfo location) {
		error("not_clause_declaration", "Not a clause declaration", location);
	}

	public void notCondition(LocationSpec location) {
		error("not_condition", "Not a condition", location);
	}

	public void notDerivedFrom(LogInfo location, Object ascendant) {
		error("not_deried_from", "Not derived from %s", location, ascendant);
	}

	public void notFieldDeclaration(LogInfo location) {
		error("not_field_declaration", "Not a field declaration", location);
	}

	public void notFloat(LogInfo location, String literal) {
		error("not_float", "Not a floating point value: %s", location, literal);
	}

	public void notInteger(LogInfo location, String literal) {
		error("not_integer", "Not an integer: %s", location, literal);
	}

	public void notPath(LogInfo location) {
		error("not_path", "Not a path", location);
	}

	public void notPrototype(LogInfo location) {
		error("not_prototype", "Prototype declaration expected", location);
	}

	public void notReproducible(LogInfo location) {
		error("not_reproducible", "Not reproducible", location);
	}

	public void notObject(LogInfo location, Object target) {
		error("not_object", "%s is not an object", location, target);
	}

	public void notObjectDeclaration(LogInfo location) {
		error(
				"not_object_declaration",
				"Object declaration expected",
				location);
	}

	public void notTypeRef(LogInfo location) {
		error("not_type_ref", "Not a valid type reference", location);
	}

	public void prohibitedAbstract(LogInfo location, String fieldName) {
		error(
				"prohibited_abstract",
				"Field '%s' can no be declared abstract, because "
				+  "it's not inside of prototype or another abstract field",
				location,
				fieldName);
	}

	public void prohibitedClauseDeclaration(LogInfo location) {
		error(
				"prohibited_clause_declaration",
				"Clause declarations prohibited here",
				location);
	}

	public void prohibitedConditionalDeclaration(LogInfo location) {
		error(
				"prohibited_conditional_declaration",
				"Only object field can be declared"
				+ " within conditional sentence",
				location);
	}

	public void prohibitedDeclaration(LogInfo location) {
		error(
				"prohibited_declaration",
				"Declarations prohibited here",
				location);
	}

	public void prohibitedDeclaredIn(LogInfo location) {
		error(
				"prohibited_declared_in",
				"Field scope declaration is prohibited here",
				location);
	}

	public void prohibitedDeclarativeEllipsis(LogInfo location) {
		error(
				"prohibited_declarative_ellipsis",
				"Ellipsis is only allowed within imperative block",
				location);
	}

	public void prohibitedDirective(LogInfo location, String directiveName) {
		error(
				"prohibited_directive",
				"Directive '%s' is prohibited here",
				location,
				directiveName);
	}

	public void prohibitedIssueEllipsis(LogInfo location) {
		error(
				"prohibited_issue_ellipsis",
				"Ellipsis is prohibited within issue",
				location);
	}

	public void prohibitedLinkType(LogInfo location) {
		error(
				"prohibited_link_type",
				"Link type is not expected here",
				location);
	}

	public void prohibitedLocal(LogInfo location) {
		error(
				"prohibited_local",
				"Local scope declaration is prohibited here",
				location);
	}

	public void prohibitedLocalAbstract(LogInfo location, String fieldName) {
		error(
				"prohibited_local_abstract",
				"Local field '%s' can no be abstract",
				location,
				fieldName);
	}

	public void prohibitedLocalAdapter(LogInfo location) {
		error("prohibited_local_adapter", "Adapter can not be local", location);
	}

	public void prohibitedLocalVisibility(LogInfo location, String fieldName) {
		error(
				"prohibited_local_visibility",
				"Local field '%s' can not have visibility",
				location,
				fieldName);
	}

	public void prohibitedOverriderClause(LogInfo location) {
		error(
				"prohibited_overrider_clause",
				"Overrider clause is prohibited here",
				location);
	}

	public void prohibitedPhraseName(LogInfo location) {
		error(
				"prohibited_phrase_name",
				"Name can not follow another name or phrase prefix",
				location);
	}

	public void prohibitedPrivateAbstract(LogInfo location, String fieldName) {
		error(
				"prohibited_private_abstract",
				"Private field '%s' can not be abstract",
				location,
				fieldName);
	}

	public void prohibitedPrototype(LogInfo location) {
		error(
				"prohibited_prototype",
				"Field can not be declared as prototype",
				location);
	}

	public void prohibitedRuntimeSample(LogInfo location) {
		error(
				"prohibited_runtime_sample",
				"Sample should be resolvable at compile time."
				+ " Variable, link or local can not be used as sample",
				location);
	}

	public void prohibitedSampleAtRuntime(LogInfo location) {
		error(
				"prohibited_sample_at_runtime",
				"Run-time object can not have samples",
				location);
	}

	public void prohibitedSamples(LogInfo location) {
		error("prohibited_samples", "Samples are prohibited here", location);
	}

	public void prohibitedVariableType(LogInfo location) {
		error(
				"prohibited_variable_type",
				"Variable type is not expected here",
				location);
	}

	public void recursiveResolution(LogInfo location, Object resolvable) {
		error(
				"recursive_resolution",
				"Infinite recursion when resolving %s",
				location,
				resolvable);
	}

	public void requiredInitializer(LogInfo location) {
		error("required_initializer", "Initializer required here", location);
	}

	public void requiredLinkTarget(LogInfo location) {
		error("required_link_target", "Link target is required here", location);
	}

	public void unavailableSource(
			LogInfo location,
			String sourceName,
			String reason) {
		error(
				"unavailable_source",
				"Source '%s' can not be opened: %s",
				location,
				sourceName,
				reason);
	}

	public void unexpectedAbstract(LogInfo location) {
		error(
				"unexpected_abstract",
				"Unexpected abstract declaration",
				location);
	}

	public void unexpectedAbsolutePath(LogInfo location) {
		error(
				"unexpected_absolute_path",
				"Relative path expected here",
				location);
	}

	public void unexpectedAdapter(LogInfo location) {
		error("unexpected_adapter", "Unexpected adapter declaration", location);
	}

	public void unexpectedAncestor(
			LogInfo location,
			Object actual,
			Object expected) {
		error(
				"unexpected_ancestor",
				"Wrong ancestor: %s, but expected: %s",
				location,
				actual,
				expected);
	}

	public void unexpectedArrayDimension(
			LogInfo location,
			int actual,
			int expected) {
		error(
				"unexpected_array_dimension",
				"Unexpected array dimension: %d, but %d expected",
				location,
				actual,
				expected);
	}

	public void unexpectedPrototype(LogInfo location) {
		error(
				"unexpected_prototype",
				"Unexpected prototype declaration",
				location);
	}

	public void unexpectedType(
			LogInfo location,
			Object actual,
			Object expected) {
		error(
				"unexpected_type",
				"Unexpected type: %s, but %s expected",
				location,
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
				"Wrong '%s' field visibility: %s, but %s expected",
				location,
				fieldName,
				actual,
				expected);
	}

	public void unresolved(LogInfo location, Object target) {
		error("unresolved", "'%s' can not be resolved", location, target);
	}

	public void unresolvedModule(LogInfo location, String moduleId) {
		error(
				"unresolved_module",
				"Module <%s> can not be resolved",
				location,
				moduleId);
	}

	public void unresolvedParent(LogInfo location, String fieldName) {
		error(
				"unresolved_parent",
				"Enclosing field '%s' can be found",
				location,
				fieldName);
	}

	public void unresolvedScope(LogInfo location, String scope) {
		error("unresolved_scope", "Unresolved scope: %s", location, scope);
	}

	public void unresolvedValue(LogInfo location, Object valuable) {
		error(
				"unresolved_value",
				"Value of '%s' can not be resolved",
				location,
				valuable);
	}

	public void unsupportedBinaryOperator(
			LogInfo location,
			String operator,
			Object adapterType) {
		error(
				"unsupported_binary_operator",
				"Binary operator '%s' is not supported, because neither left,"
				+ " nor right operand have an '%s' adapter",
				location,
				operator,
				adapterType);
	}

	public void unsupportedRightOperand(
			LogInfo location,
			Object operandType,
			String operator) {
		error(
				"unsupported_right_operand",
				"Right operand of type %s is not supported by operator %s",
				location,
				operandType,
				operator);
	}

	public void unsupportedUnaryOperator(
			LogInfo location,
			String operator,
			Object adapterType) {
		error(
				"unsupported_unary_operator",
				"Unary operator '%s' is not supported, "
				+ "because operand doesn't have an '%s' adapter",
				location,
				operator,
				adapterType);
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
				"compiler." + code,
				defaultMessage,
				location.getLoggable(),
				args));
	}

}
