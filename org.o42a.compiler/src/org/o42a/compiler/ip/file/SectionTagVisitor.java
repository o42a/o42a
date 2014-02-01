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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.SectionTag;


final class SectionTagVisitor
		extends AbstractExpressionVisitor<SectionTag, SectionTag> {

	private final CompilerLogger logger;

	SectionTagVisitor(CompilerLogger logger) {
		this.logger = logger;
	}

	public final CompilerLogger getLogger() {
		return this.logger;
	}

	@Override
	public SectionTag visitMemberRef(MemberRefNode ref, SectionTag p) {

		final ExpressionNode owner = ref.getOwner();
		final SectionTag ownerTag;

		if (owner == null) {
			ownerTag = p;
		} else {
			ownerTag = owner.accept(this, p);
			if (ownerTag == null) {
				return null;
			}
		}

		final NameNode name = ref.getName();

		if (name == null) {
			return super.visitMemberRef(ref, ownerTag);
		}
		if (ref.getDeclaredIn() != null) {
			getLogger().error(
					"prohibited_section_tag_declared_in",
					ref.getDeclaredIn(),
					"Section tag should not contain declared-in specifier");
		}

		return ownerTag.append(name.getName());
	}

	@Override
	protected SectionTag visitExpression(
			ExpressionNode expression,
			SectionTag p) {
		getLogger().error(
				"invalid_section_tag",
				expression,
				"Invalid section tag");
		return null;
	}

}
