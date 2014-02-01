/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.member.field;

import static org.o42a.core.member.field.DefinitionTarget.definitionTarget;
import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.field.impl.InvalidFieldDefinition;
import org.o42a.core.member.field.impl.RescopedFieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;


public abstract class FieldDefinition extends Contained {

	public static FieldDefinition invalidDefinition(
			LocationInfo location,
			Distributor distributor) {
		return new InvalidFieldDefinition(location, distributor);
	}

	public static FieldDefinition impliedDefinition(
			LocationInfo location,
			Distributor scope) {
		return new AscendantsDefinition(location, scope).fieldDefinition(
				location,
				emptyBlock(location));
	}

	public static DefinitionTarget definerTarget(ObjectDefiner definer) {

		final Field[] allOverridden = definer.getField().getOverridden();

		for (Field overridden : allOverridden) {

			final TypeParameters<?> typeParameters =
					overridden.toObject().type().getParameters();

			if (!typeParameters.getValueType().isVoid()) {
				return definitionTarget(typeParameters);
			}
		}

		return objectDefinition();
	}

	public static DefinitionTarget refDefinitionTarget(Ref ref) {
		return pathDefinitionTarget(ref.getPath());
	}

	public static DefinitionTarget pathDefinitionTarget(BoundPath path) {

		final PathResolution resolution = path.resolve(
				pathResolver(path.getOrigin(), dummyRefUser()));

		if (resolution.isError()) {
			return objectDefinition();
		}

		final Obj object = resolution.getObject();

		if (object == null) {
			return objectDefinition();
		}

		return definitionTarget(object.type().getParameters());
	}

	public FieldDefinition(Ref ref) {
		super(ref, ref.distribute());
	}

	public FieldDefinition(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public abstract void init(Field field, Ascendants implicitAscendants);

	public boolean isValid() {
		return true;
	}

	public abstract DefinitionTarget getDefinitionTarget();

	public abstract void defineObject(ObjectDefiner definer);

	public abstract void overrideObject(ObjectDefiner definer);

	public abstract void defineMacro(MacroDefiner definer);

	public FieldDefinition prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new RescopedFieldDefinition(this, prefix);
	}

	public final FieldDefinition upgradeScope(Scope toScope) {
		if (toScope.is(getScope())) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

}
