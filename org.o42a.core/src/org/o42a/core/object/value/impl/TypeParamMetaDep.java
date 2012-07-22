/*
    Compiler Core
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
package org.o42a.core.object.value.impl;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.core.Scope;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathLabel;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueStruct;


public class TypeParamMetaDep extends MetaDep implements PathLabel, Consumer {

	private final Ref macroRef;
	private MetaDep parentDep;
	private MetaDep nestedDep;
	private TypeParamMacroDep macroDep;

	public TypeParamMetaDep(
			Meta declaredIn,
			TypeParamMacroDep macroDep,
			Ref macroRef) {
		super(declaredIn, macroDep);
		this.macroDep = macroDep;
		this.macroRef = macroRef;
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
	public Ref expandMacro(Ref ref) {
		return Path.SELF_PATH
				.label(this)// Label the path to recognize it.
				.bind(ref, ref.getScope())
				.target(ref.distribute());
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
	protected boolean updateMeta(Meta meta) {

		final Meta nestedMeta = nestedMeta(meta);

		if (nestedMeta != null) {
			return updateTypeParam(nestedMeta);
		}

		return updateTypeParam(meta);
	}

	final void setParentDep(MetaDep parentDep) {
		this.parentDep = parentDep;
	}

	final boolean updateTypeParam(Meta meta) {

		final ValueStruct<?, ?> oldValueStruct =
				meta.getObject().value().getValueStruct();
		final TypeParameters oldParams =
				oldValueStruct.getParameters();
		final TypeRef oldTypeRef = oldParams.getTypeRef();
		final TypeRef newTypeRef =
				updateTypeParam(meta, oldTypeRef, this.macroDep.getDepth());

		if (newTypeRef == null) {
			return false;
		}

		meta.getObject().value().setValueStruct(
				oldValueStruct.setParameters(oldParams.setTypeRef(newTypeRef)));

		return true;
	}

	private TypeRef updateTypeParam(Meta meta, TypeRef typeRef, int depth) {
		if (depth > 0) {

			final TypeParameters subTypeParams =
					typeRef.getValueStruct().getParameters();
			final TypeRef oldSubTypeRef = subTypeParams.getTypeRef();
			final TypeRef newSubTypeRef = updateTypeParam(
					meta,
					oldSubTypeRef,
					depth - 1);

			if (newSubTypeRef == null) {
				return null;
			}

			return typeRef.setValueStruct(
					subTypeParams.setTypeRef(newSubTypeRef));
		}

		if (!typeRef.getPath().hasLabel(this)) {
			// The value struct does not depend on the same macro expansion
			// any more (it was overridden).
			return null;
		}

		// Re-expand the macro in the new scope.
		final Ref newRef = macroRef(meta).reexpandMacro().consume(this);

		if (newRef == null) {
			// Error.
			return null;
		}
		if (newRef.getPath().getPath().equals(typeRef.getPath().getPath())) {
			// Path didn't change.
			return null;
		}

		return newRef.toTypeRef();
	}

	private Ref macroRef(Meta meta) {

		final Scope scope = meta.getObject().getScope();
		final PrefixPath prefix;

		if (this.macroDep.getNesting() == null) {
			prefix = upgradePrefix(this.macroRef, scope);
		} else {
			final Scope delcaredIn = getDeclaredIn().getObject().getScope();
			prefix = delcaredIn.getEnclosingScopePath().toPrefix(scope);
		}

		return this.macroRef.prefixWith(prefix);
	}

}
