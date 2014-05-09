/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.access.AccessRules.ACCESS_FROM_TITLE;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.ast.file.SubTitleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.compiler.ip.field.FieldDeclarableVisitor;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.DefinitionSource;
import org.o42a.core.source.Location;
import org.o42a.util.io.SourceFileName;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.string.Name;


final class SectionTitle implements LogInfo {

	private final Section section;
	private final Loggable loggable;
	private final DeclaratorNode declaratorNode;

	SectionTitle(Section section) {
		this.section = section;

		final SectionNode sectionNode = section.getSectionNode();

		this.loggable = titleLoggable(sectionNode);

		if (sectionNode.getTitle() != null) {
			this.declaratorNode = sectionNode.getDeclarator();
		} else {
			this.declaratorNode = null;
		}
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final DeclaratorNode getDeclaratorNode() {
		return this.declaratorNode;
	}

	public final boolean isValid() {
		if (!isImplicit()) {
			return true;
		}
		// Title present, but it doesn't contain declarator.
		return getSection().getSectionNode().getTitle() == null;
	}

	public final boolean isImplicit() {
		return this.declaratorNode == null;
	}

	public final Section getSection() {
		return this.section;
	}

	public final DefinitionSource getSource() {
		return getSection().getSource();
	}

	public final CompilerLogger getLogger() {
		return getSection().getLogger();
	}

	public FieldDeclaration fieldDeclaration(Distributor distributor) {
		if (isImplicit()) {
			return implicitFieldDeclaration(distributor);
		}
		return explicitFieldDeclaration(distributor);
	}

	@Override
	public String toString() {
		if (this.loggable == null) {
			return super.toString();
		}
		return this.loggable.toString();
	}

	final AscendantsDefinition ascendants(
			Distributor distributor,
			TypeConsumer consumer) {
		if (isImplicit()) {
			return new AscendantsDefinition(
					new Location(distributor.getContext(), this),
					distributor);
		}

		final ExpressionNode definition = getDeclaratorNode().getDefinition();

		if (definition == null) {
			return new AscendantsDefinition(
					new Location(distributor.getContext(), this),
					distributor);
		}

		return definition.accept(
				new SectionAscendantsVisitor(consumer),
				ACCESS_FROM_TITLE.distribute(distributor));
	}

	static Loggable titleLoggable(SectionNode sectionNode) {

		final DeclaratorNode declaratorNode = sectionNode.getDeclarator();

		if (declaratorNode != null) {
			return declaratorNode.getLoggable();
		}

		final SentenceNode titleNode = sectionNode.getTitle();

		if (titleNode != null) {
			return titleNode.getLoggable();
		}

		final SubTitleNode subTitleNode = sectionNode.getSubTitle();

		if (subTitleNode != null) {
			return subTitleNode.getLoggable();
		}

		return sectionNode.getLoggable();
	}

	private FieldDeclaration implicitFieldDeclaration(Distributor distributor) {

		final Name fieldName = fieldNameByFileName();

		if (fieldName == null) {
			return null;
		}

		return FieldDeclaration.fieldDeclaration(
				new Location(distributor.getContext(), this),
				distributor,
				FIELD_NAME.memberName(fieldName))
				.makeStatic();
	}

	private FieldDeclaration explicitFieldDeclaration(Distributor distributor) {

		final FieldDeclarableVisitor visitor = new FieldDeclarableVisitor(
				PLAIN_IP,
				distributor.getContext(),
				getDeclaratorNode());
		final DeclarableNode declarableNode =
				getDeclaratorNode().getDeclarable();

		return validate(declarableNode.accept(
				visitor,
				ACCESS_FROM_TITLE.distribute(distributor)));
	}

	private Name fieldNameByFileName() {

		final SourceFileName fileName = getSource().getFileName();

		if (!fileName.isValid()) {
			// Can not establish field name by invalid file name.
			return null;
		}
		if (fileName.isAdapter()) {
			getLogger().error(
					"missing_adapter_section_title",
					this,
					"Section title required for adapter");
			return null;
		}
		if (fileName.isOverride()) {
			getLogger().error(
					"missing_override_section_title",
					this,
					"Section title required when overriding field");
			return null;
		}

		return fileName.getFieldName();
	}

	private FieldDeclaration validate(FieldDeclaration declaration) {
		if (!getSource().getFileName().isValid()) {
			// File name is invalid, so can't validate.
			return declaration;
		}
		if (!declaration.isAdapter()) {
			return validateField(declaration);
		}
		return validateAdapter(declaration);
	}

	private FieldDeclaration validateField(FieldDeclaration declaration) {
		if (declaration.isMacro()) {
			getLogger().error(
					"prohibited_macro_section_title",
					this.declaratorNode.getDeclarable(),
					"Macro declaration is prohibited within section title");
			return declaration;
		}

		final SourceFileName fileName = getSource().getFileName();
		final MemberRefNode fieldNode =
				(MemberRefNode) getDeclaratorNode().getDeclarable();

		if (fileName.isAdapter()) {
			getLogger().warning(
					"not_adapter_section_title",
					fieldNode,
					"Adapter declaration expected in file '%s'",
					fileName);
		}

		return validateOverride(declaration, fieldNode.getDeclaredIn());
	}

	private FieldDeclaration validateAdapter(FieldDeclaration declaration) {

		final SourceFileName fileName = getSource().getFileName();
		final DeclarableAdapterNode adapterNode =
				(DeclarableAdapterNode)
				getDeclaratorNode().getDeclarable();
		final MemberRefNode adapteeNode = adapterNode.getMember();

		if (!fileName.isAdapter()) {
			getLogger().warning(
					"unexpected_adapter_section_title",
					adapterNode,
					"Adapter declaration is not expected in file '%s'",
					fileName);
		} else if (!validatePath(adapteeNode, fileName.getAdapterId())) {
			getLogger().warning(
					"unmatched_adaptee_section_title",
					adapteeNode,
					"Adapter declaration doen't match"
					+ " the name of file: ",
					fileName);
		}

		return validateOverride(declaration, adapteeNode.getDeclaredIn());
	}

	private FieldDeclaration validateOverride(
			FieldDeclaration declaration,
			RefNode declaredIn) {

		final SourceFileName fileName = getSource().getFileName();

		if (fileName.isOverride()) {
			if (!declaration.getDeclarationMode().canOverride()) {
				getLogger().warning(
						"not_override_section_title",
						getDeclaratorNode().getDeclarable(),
						"Field override expected in file '%s'",
						fileName);
			} else if (declaredIn != null
					&& !validatePath(declaredIn, fileName.getDeclaredIn())) {
				getLogger().warning(
						"unmatched_declared-in_section_title",
						declaredIn,
						"Declared-in declaration doen't match"
						+ " the name of file: ",
						fileName);
			}
		}

		return declaration;
	}

	private boolean validatePath(RefNode ref, Name[] path) {

		final SectionTitlePathValidator validator =
				new SectionTitlePathValidator(path);
		final Object result = ref.accept(validator, path.length - 1);

		return result != null;
	}

}
