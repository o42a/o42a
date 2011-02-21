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
package org.o42a.core.artifact.link;

import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;


final class OverriddenLink extends Link {

	private final DeclaredLinkField field;

	OverriddenLink(DeclaredLinkField field) {
		super(field, field.getArtifactKind());
		this.field = field;
	}

	@Override
	public boolean isValid() {
		return this.field.validate(true);
	}

	@Override
	public String toString() {
		return this.field.toString();
	}

	@Override
	protected TypeRef buildTypeRef() {

		final TypeRef inherited = this.field.inheritedTypeRef();
		final TypeRef declared = declaredTypeRef();

		if (declared != null) {

			final TypeRelation relation = inherited.relationTo(declared);

			if (relation.isAscendant()) {
				return declared;
			}
			if (!relation.isError()) {
				getLogger().notDerivedFrom(declared, inherited);
			}
			this.field.invalid();
		}

		return inherited;
	}

	private TypeRef declaredTypeRef() {

		final FieldDefinition definition = this.field.getDefinition();

		if (definition == null) {
			return null;
		}

		final TypeRef typeRef = this.field.getDeclaration().getType();

		if (typeRef == null) {
			return null;
		}
		if (!typeRef.getArtifact().accessBy(this.field).checkPrototypeUse()) {
			this.field.invalid();
		}

		return typeRef.rescope(this.field.getEnclosingScope());
	}

	@Override
	protected TargetRef buildTargetRef() {

		final TargetRef ref = this.field.declaredRef();

		if (ref != null) {
			return ref;
		}

		final Field<Link>[] overridden = this.field.getOverridden();

		if (overridden.length != 1) {
			getLogger().requiredLinkTarget(this.field);
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
		return this.field.getOverridden()[0].getArtifact();
	}

}
