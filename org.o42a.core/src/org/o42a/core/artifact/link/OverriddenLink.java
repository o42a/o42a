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
package org.o42a.core.artifact.link;

import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;


final class OverriddenLink extends Link {

	private final LinkFieldDecl decl;

	OverriddenLink(LinkFieldDecl decl) {
		super(decl.getField(), decl.getKind());
		this.decl = decl;
	}

	@Override
	public boolean isValid() {
		return this.decl.validate(true);
	}

	@Override
	public String toString() {
		return this.decl.toString();
	}

	@Override
	protected TypeRef buildTypeRef() {

		final TypeRef inherited = this.decl.inheritedTypeRef();
		final TypeRef declared = declaredTypeRef();

		if (declared != null) {
			if (declared.derivedFrom(inherited)) {
				return declared;
			}
			getLogger().notDerivedFrom(declared, inherited);
			this.decl.invalid();
		}

		return inherited;
	}

	private TypeRef declaredTypeRef() {

		final FieldDefinition definition = this.decl.getDefinition();

		if (definition == null) {
			return null;
		}

		final DeclaredField<Link> field = this.decl.getField();
		final TypeRef typeRef = field.getDeclaration().getType();

		if (typeRef == null) {
			return null;
		}
		if (!typeRef.getArtifact().accessBy(field).checkPrototypeUse()) {
			this.decl.invalid();
		}

		return typeRef.rescope(field.getEnclosingScope());
	}

	@Override
	protected TargetRef buildTargetRef() {

		final TargetRef ref = this.decl.declaredRef();

		if (ref != null) {
			return ref;
		}

		final Field<Link>[] overridden = this.decl.getField().getOverridden();

		if (overridden.length != 1) {
			getLogger().requiredLinkTarget(this.decl.getField());
		}

		return overridden[0].getArtifact().getTargetRef();
	}

	@Override
	protected TypeRef correctTypeRef(TargetRef targetRef, TypeRef typeRef) {
		return getOverridden().getTypeRef().upgradeScope(
				getScope().getEnclosingScope());
	}

	@Override
	protected TargetRef correctTargetRef(TargetRef targetRef, TypeRef typeRef) {
		return getOverridden().getTargetRef();
	}

	private Link getOverridden() {
		return this.decl.getField().getOverridden()[0].getArtifact();
	}

}
