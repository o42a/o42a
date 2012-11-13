/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.type.macro;

import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.ref.type.TypeParameter;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.ref.type.TypeRef;


final class TypeParamMetaDep extends MetaDep {

	private final Ref macroRef;
	private final TypeParamMacroDep macroDep;
	private final PathTemplate template;
	private MetaDep parentDep;
	private MetaDep nestedDep;

	TypeParamMetaDep(
			Meta declaredIn,
			TypeParamMacroDep macroDep,
			Ref macroRef,
			PathTemplate template) {
		super(declaredIn);
		this.macroDep = macroDep;
		this.macroRef = macroRef;
		this.template = template;
	}

	@Override
	public final MetaDep parentDep() {
		return this.parentDep;
	}

	@Override
	public final MetaDep nestedDep() {
		if (this.nestedDep != null) {
			return this.nestedDep;
		}

		final Nesting nesting = this.macroDep.getNesting();

		if (nesting == null) {
			return null;
		}

		final Obj nested =
				nesting.findObjectIn(getDeclaredIn().getObject().getScope());

		return this.nestedDep = new ValueStructUpdate(this, nested.meta());
	}

	@Override
	protected boolean triggered(Meta meta) {

		final Resolution resolution =
				this.macroRef.resolve(meta.getObject().getScope().resolver());

		if (!resolution.isResolved()) {
			return false;
		}

		return resolution.toObject().meta().isUpdated();
	}

	@Override
	protected boolean changed(Meta meta) {
		return typeParamChanged(findNestedMeta(meta));
	}

	final void setParentDep(MetaDep parentDep) {
		this.parentDep = parentDep;
	}

	final boolean typeParamChanged(Meta meta) {
		return typeParamChanged(meta, this.macroDep.getParamIndex());
	}

	private Meta findNestedMeta(Meta meta) {

		final Meta nestedMeta = nestedMeta(meta);

		if (nestedMeta != null) {
			return nestedMeta;
		}

		return meta;
	}

	private final boolean typeParamChanged(Meta meta, int paramIndex) {

		final TypeParameters typeParams =
				meta.getObject().type().getParameters();
		final MemberKey paramKey =
				findNestedMeta(getDeclaredIn())
				.getObject()
				.type()
				.getParameters()
				.getParameters()[paramIndex]
				.getKey();
		final TypeRef typeRef = typeParams.typeRef(paramKey);

		return typeParamChanged(meta, typeRef, this.macroDep.getDepth());
	}

	private boolean typeParamChanged(Meta meta, TypeRef typeRef, int depth) {
		if (depth > 0) {

			final TypeParameter[] subTypeParams =
					typeRef.getParameters().getParameters();

			for (int i = 0; i < subTypeParams.length; ++i) {

				final TypeRef subTypeRef = subTypeParams[i].getTypeRef();

				if (typeParamChanged(meta, subTypeRef, depth - 1)) {
					return true;
				}
			}

			return false;
		}

		if (!typeRef.getPath().hasTemplate(this.template)) {
			// The value struct does not depend on the same macro expansion
			// any more (it was overridden).
			return false;
		}

		return true;
	}

}
