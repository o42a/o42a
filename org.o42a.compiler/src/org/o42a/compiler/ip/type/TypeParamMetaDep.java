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
package org.o42a.compiler.ip.type;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.core.Scope;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Label;


final class TypeParamMetaDep extends MetaDep implements Label<Void>, Consumer {

	private final Ref macroRef;
	private MetaDep parentDep;
	private MetaDep nestedDep;
	private TypeParamMacroDep macroDep;

	TypeParamMetaDep(
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
	public Ref expandMacro(Ref macroRef, Ref macroExpansion) {
		// Label the expansion to recognize it.
		return macroExpansion.label(this);
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

		final Meta nestedMeta = nestedMeta(meta);

		if (nestedMeta != null) {
			return typeParamChanged(nestedMeta);
		}

		return typeParamChanged(meta);
	}

	final void setParentDep(MetaDep parentDep) {
		this.parentDep = parentDep;
	}

	final boolean typeParamChanged(Meta meta) {

		final ValueStruct<?, ?> oldValueStruct =
				meta.getObject().value().getValueStruct();
		final TypeParameters oldParams =
				oldValueStruct.getParameters();
		final TypeRef oldTypeRef = oldParams.getTypeRef();

		return typeParamChanged(meta, oldTypeRef, this.macroDep.getDepth());
	}

	private boolean typeParamChanged(Meta meta, TypeRef typeRef, int depth) {
		if (depth > 0) {

			final TypeParameters subTypeParams =
					typeRef.getValueStruct().getParameters();
			final TypeRef oldSubTypeRef = subTypeParams.getTypeRef();

			return typeParamChanged(meta, oldSubTypeRef, depth - 1);
		}

		if (!typeRef.getPath().getLabels().have(this)) {
			// The value struct does not depend on the same macro expansion
			// any more (it was overridden).
			return false;
		}

		// Re-expand the macro in the new scope.
		final Ref newRef = macroRef(meta).reexpandMacro().consume(this);

		if (newRef == null) {
			// Error.
			return false;
		}

		// Path changed?
		return !newRef.getPath().getPath().equals(typeRef.getPath().getPath());
	}

	private Ref macroRef(Meta meta) {

		final Scope scope = meta.getObject().getScope();
		final PrefixPath prefix;

		if (this.macroDep.getNesting() == null) {
			prefix = upgradePrefix(this.macroRef, scope);
		} else {

			final Scope delcaredIn =
					nestedDep().getDeclaredIn().getObject().getScope();

			prefix = delcaredIn.getEnclosingScopePath().toPrefix(scope);
		}

		return this.macroRef.prefixWith(prefix);
	}

}
