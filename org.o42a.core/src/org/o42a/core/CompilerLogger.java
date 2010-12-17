/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.ast.Node;
import org.o42a.ast.NodeInfo;
import org.o42a.core.member.Visibility;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;
import org.o42a.util.log.Severity;


public class CompilerLogger implements Logger {

	private final Logger logger;
	private final Object source;

	public CompilerLogger(Logger logger, Object source) {
		this.logger = logger != null ? logger : Logger.DEFAULT_LOGGER;
		this.source = source;
	}

	public void abstractNotOverridden(NodeInfo locaion, String fieldName) {
		error(
				"abstract_not_overridden",
				"Abstract field '%s' not overridden",
				locaion,
				fieldName);
	}

	public void abstractValue(NodeInfo location) {
		error("abstract_value", "Abstract value access", location);
	}

	public void ambiguousClause(NodeInfo location, String clauseName) {
		error(
				"ambiguous_clause",
				"Clause '%s' declaration is ambiguous",
				location,
				clauseName);
	}

	public void ambiguousField(NodeInfo location, String fieldName) {
		error(
				"ambiguousField",
				"Field '%s' declaration is ambiguous",
				location,
				fieldName);
	}

	public void ambiguousValue(NodeInfo location) {
		error("ambiguous_value", "Ambiguous value declaration", location);
	}

	public void arithmeticError(NodeInfo location, String message) {
		error("arithmetic_error", "Arithmetic error: %s", location, message);
	}

	public void cantDeclareOverridden(NodeInfo location, String fieldName) {
		error(
				"cant_declare_overridden",
				"Can not declare already overridden field '%s'",
				location,
				fieldName);
	}

	public void cantInherit(NodeInfo location, Object target) {
		error("cant_inherit", "%s can not be inherited", location, target);
	}

	public void cantOverrideDeclared(NodeInfo location, String fieldName) {
		error(
				"cant_override_declared",
				"Can not override already declared field '%s'",
				location,
				fieldName);
	}

	public void cantOverrideUnknown(NodeInfo location, String fieldName) {
		error(
				"cant_override_unknown",
				"Can not override unknown field '%s'",
				location,
				fieldName);
	}

	public void dublicateBlockName(NodeInfo location, String blockName) {
		error(
				"duplicate_block_name",
				"Block '%s' already declared",
				location,
				blockName);
	}

	public void expectedClauseName(NodeInfo location) {
		error("expected_clause_name", "Clause name expected here", location);
	}

	public void expectedDeclaration(NodeInfo location) {
		error("expected_declaration", "Declaration expected here", location);
	}

	public void forbiddenAccess(NodeInfo location, Object target) {
		error(
				"forbidden_access",
				"Access to %s is forbidden",
				location,
				target);
	}

	public void illegalVisibility(NodeInfo location) {
		error(
				"illegal_visibility",
				"Illegal field visibility qualifier",
				location);
	}

	public void ignored(NodeInfo location) {
		warning("ignored", "Statement ignored", location);
	}

	public void incompatible(NodeInfo location, Object type) {
		error("incompatible", "Not compatible with %s", location, type);
	}

	public void indefiniteValue(NodeInfo location) {
		error("indefinite_value", "Indefinite value access", location);
	}

	public void invalidArtifact(NodeInfo location) {
		error("invalid_artifact", "Artifact is not valid", location);
	}

	public void invalidClause(NodeInfo location) {
		error("invalid_clause", "Invalid clause", location);
	}

	public void invalidClauseContent(NodeInfo location) {
		error("invalid_clause_content", "Invalid clause content", location);
	}

	public void invalidClauseReused(NodeInfo location) {
		error(
				"invalid_clause_reused",
				"Attempt to reuse inaccessible clause",
				location);
	}

	public void invalidDeclaration(NodeInfo location) {
		error("invalid_declaration", "Invalid declaration", location);
	}

	public void invalidDefinition(NodeInfo location) {
		error("invalid_definition", "Not a valid member definition", location);
	}

	public void invalidExpression(NodeInfo location) {
		error("invalid_expression", "Not a valid expression", location);
	}

	public void invalidInteger(NodeInfo location, String reason) {
		error(
				"invalid_integer",
				"Invalid integer literal %s ",
				location,
				reason);
	}

	public void invalidOverridden(NodeInfo location) {
		error("invalid_overridden", "Only field can be overridden", location);
	}

	public void invalidPhrasePrefix(NodeInfo location) {
		error("invalid_phrase_prefix", "Invalid phrase prefix", location);
	}

	public void invalidReference(NodeInfo location) {
		error("invalid_reference", "Not a valid reference", location);
	}

	public void invalidStatement(NodeInfo location) {
		error("invalid_statement", "Not a valid statement", location);
	}

	public void invalidType(NodeInfo location) {
		error("invalid_type", "Not a valid type reference", location);
	}

	public void noClauseTarget(NodeInfo location) {
		error("no_clause_target", "Clause has no target", location);
	}

	public void noDefinition(LocationSpec location) {
		error("no_definition", "Definition is missing", location);
	}

	public void noModuleNoObject(NodeInfo location) {
		error(
				"no_module_no_object",
				"Either module or object should be specified",
				location);
	}

	public void noName(NodeInfo location) {
		error("no_name", "Name not specified", location);
	}

	public void notAbstract(NodeInfo location) {
		error("not_abstract", "Abstract declaration expected", location);
	}

	public void notAdapter(NodeInfo location) {
		error("not_adapter", "Adapter declaration expected", location);
	}

	public void notArray(NodeInfo location) {
		error("not_array", "Not array", location);
	}

	public void notArrayItemInitializer(NodeInfo location) {
		error(
				"not_array_item_initializer",
				"Array item initializer expected",
				location);
	}

	public void notClause(NodeInfo location) {
		error("not_clause", "Not clause", location);
	}

	public void notClauseDeclaration(NodeInfo location) {
		error("not_clause_declaration", "Not a clause declaration", location);
	}

	public void notDerivedFrom(NodeInfo location, Object ascendant) {
		error("not_deried_from", "Not derived from %s", location, ascendant);
	}

	public void notFieldDeclaration(NodeInfo location) {
		error("not_field_declaration", "Not a field declaration", location);
	}

	public void notFloat(NodeInfo location, String literal) {
		error("not_float", "Not a floating point value: %s", location, literal);
	}

	public void notInteger(NodeInfo location, String literal) {
		error("not_integer", "Not an integer: %s", location, literal);
	}

	public void notPath(NodeInfo location) {
		error("not_path", "Not a path", location);
	}

	public void notPrototype(NodeInfo location) {
		error("not_prototype", "Prototype declaration expected", location);
	}

	public void notReproducible(NodeInfo location) {
		error("not_reproducible", "Not reproducible", location);
	}

	public void notObject(NodeInfo location, Object target) {
		error("not_object", "%s is not an object", location, target);
	}

	public void notObjectDeclaration(NodeInfo location) {
		error(
				"not_object_declaration",
				"Object declaration expected",
				location);
	}

	public void notTypeRef(NodeInfo location) {
		error("not_type_ref", "Not a valid type reference", location);
	}

	public void prohibitedAbstract(NodeInfo location, String fieldName) {
		error(
				"prohibited_abstract",
				"Field '%s' can no be declared abstract, because "
				+  "it's not inside of prototype or another abstract field",
				location,
				fieldName);
	}

	public void prohibitedClauseDeclaration(NodeInfo location) {
		error(
				"prohibited_clause_declaration",
				"Clause declarations prohibited here",
				location);
	}

	public void prohibitedConditionalDeclaration(NodeInfo location) {
		error(
				"prohibited_conditional_declaration",
				"Only object field can be declared"
				+ " within conditional sentence",
				location);
	}

	public void prohibitedDeclaration(NodeInfo location) {
		error(
				"prohibited_declaration",
				"Declarations prohibited here",
				location);
	}

	public void prohibitedDeclaredIn(NodeInfo location) {
		error(
				"prohibited_declared_in",
				"Field scope declaration is prohibited here",
				location);
	}

	public void prohibitedDeclarativeEllipsis(NodeInfo location) {
		error(
				"prohibited_declarative_ellipsis",
				"Ellipsis is only allowed within imperative block",
				location);
	}

	public void prohibitedDirective(NodeInfo location, String directiveName) {
		error(
				"prohibited_directive",
				"Directive '%s' is prohibited here",
				location,
				directiveName);
	}

	public void prohibitedIssueEllipsis(NodeInfo location) {
		error(
				"prohibited_issue_ellipsis",
				"Ellipsis is prohibited within issue",
				location);
	}

	public void prohibitedLinkType(NodeInfo location) {
		error(
				"prohibited_link_type",
				"Link type is not expected here",
				location);
	}

	public void prohibitedLocal(LocationSpec location) {
		error(
				"prohibited_local",
				"Local scope declaration is prohibited here",
				location);
	}

	public void prohibitedLocalAbstract(NodeInfo location, String fieldName) {
		error(
				"prohibited_local_abstract",
				"Local field '%s' can no be abstract",
				location,
				fieldName);
	}

	public void prohibitedLocalAdapter(NodeInfo location) {
		error("prohibited_local_adapter", "Adapter can not be local", location);
	}

	public void prohibitedLocalVisibility(NodeInfo location, String fieldName) {
		error(
				"prohibited_local_visibility",
				"Local field '%s' can not have visibility",
				location,
				fieldName);
	}

	public void prohibitedOverriderClause(LocationSpec location) {
		error(
				"prohibited_overrider_clause",
				"Overrider clause is prohibited here",
				location);
	}

	public void prohibitedPhraseName(NodeInfo location) {
		error(
				"prohibited_phrase_name",
				"Name can not follow another name or phrase prefix",
				location);
	}

	public void prohibitedPrivateAbstract(NodeInfo location, String fieldName) {
		error(
				"prohibited_private_abstract",
				"Private field '%s' can not be abstract",
				location,
				fieldName);
	}

	public void prohibitedPrototype(NodeInfo location) {
		error(
				"prohibited_prototype",
				"Field can not be declared as prototype",
				location);
	}

	public void prohibitedRuntimeSample(NodeInfo location) {
		error(
				"prohibited_runtime_sample",
				"Sample should be resolvable at compile time."
				+ " Variable, link or local can not be used as sample",
				location);
	}

	public void prohibitedSampleAtRuntime(NodeInfo location) {
		error(
				"prohibited_sample_at_runtime",
				"Run-time object can not have samples",
				location);
	}

	public void prohibitedSamples(NodeInfo location) {
		error("prohibited_samples", "Samples are prohibited here", location);
	}

	public void prohibitedVariableType(NodeInfo location) {
		error(
				"prohibited_variable_type",
				"Variable type is not expected here",
				location);
	}

	public void recursiveResolution(NodeInfo location, Object resolvable) {
		error(
				"recursive_resolution",
				"Infinite recursion when resolving %s",
				location,
				resolvable);
	}

	public void requiredInitializer(NodeInfo location) {
		error("required_initializer", "Initializer required here", location);
	}

	public void requiredLinkTarget(NodeInfo location) {
		error("required_link_target", "Link target is required here", location);
	}

	public void unavailableSource(
			NodeInfo location,
			String sourceName,
			String reason) {
		error(
				"unavailable_source",
				"Source '%s' can not be opened: %s",
				location,
				sourceName,
				reason);
	}

	public void unexpectedAbstract(NodeInfo location) {
		error(
				"unexpected_abstract",
				"Unexpected abstract declaration",
				location);
	}

	public void unexpectedAbsolutePath(NodeInfo location) {
		error(
				"unexpected_absolute_path",
				"Relative path expected here",
				location);
	}

	public void unexpectedAdapter(NodeInfo location) {
		error("unexpected_adapter", "Unexpected adapter declaration", location);
	}

	public void unexpectedAncestor(NodeInfo location, Object actual, Object expected) {
		error(
				"unexpected_ancestor",
				"Wrong ancestor: %s, but expected: %s",
				location,
				actual,
				expected);
	}

	public void unexpectedArrayDimension(
			NodeInfo location,
			int actual,
			int expected) {
		error(
				"unexpected_array_dimension",
				"Unexpected array dimension: %d, but %d expected",
				location,
				actual,
				expected);
	}

	public void unexpectedPrototype(NodeInfo location) {
		error(
				"unexpected_prototype",
				"Unexpected prototype declaration",
				location);
	}

	public void unexpectedType(
			NodeInfo location,
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
			NodeInfo location,
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

	public void unresolved(NodeInfo location, Object target) {
		error("unresolved", "'%s' can not be resolved", location, target);
	}

	public void unresolvedModule(NodeInfo location, String moduleId) {
		error(
				"unresolved_module",
				"Module <%s> can not be resolved",
				location,
				moduleId);
	}

	public void unresolvedParent(NodeInfo location, String fieldName) {
		error(
				"unresolved_parent",
				"Enclosing field '%s' can be found",
				location,
				fieldName);
	}

	public void unresolvedScope(NodeInfo location, String scope) {
		error("unresolved_scope", "Unresolved scope: %s", location, scope);
	}

	public void unresolvedValue(NodeInfo location, Object valuable) {
		error(
				"unresolved_value",
				"Value of '%s' can not be resolved",
				location,
				valuable);
	}

	public void unsupportedBinaryOperator(
			NodeInfo location,
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
			NodeInfo location,
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
			NodeInfo location,
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
			NodeInfo location,
			Object... args) {
		log(Severity.ERROR, code, defaultMessage, location, args);
	}

	private final void warning(
			String code,
			String defaultMessage,
			NodeInfo location,
			Object... args) {
		log(Severity.WARNING, code, defaultMessage, location, args);
	}

	private final void log(
			Severity severity,
			String code,
			String defaultMessage,
			NodeInfo location,
			Object... args) {

		final Object loc;
		final Node node = location.getNode();

		if (node != null) {

			final StringBuilder out = new StringBuilder();

			node.printRange(out);

			loc = out;
		} else {
			loc = location;
		}

		log(new LogRecord(
				getSource(),
				severity,
				"compiler." + code,
				defaultMessage,
				loc,
				args));
	}

}
