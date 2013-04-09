/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.keeper;

import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.BaseFieldDefinition;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.link.LinkValueType;


final class KeptValueFieldDefinition extends BaseFieldDefinition {

	private final Ref value;
	private TypeRef valueTypeInterface;

	KeptValueFieldDefinition(Ref ref, KeepValue keepValue) {
		super(ref);
		this.value = keepValue.getValue();
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return refDefinitionTarget(getValue());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(ancestor());
		definer.setParameters(typeParameters(definer));
		keepValue(definer);
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(getRef(), null);

		final LinkValueType linkType =
				definer.getField().getDeclaration().getLinkType();

		definer.setParameters(
				linkType.typeParameters(
						getRef()
						.getInterface()
						.setParameters(rescopedTypeParameters())
						.rescope(definer.getField()))
				.toObjectTypeParameters());
	}

	@Override
	public void overridePlainObject(ObjectDefiner definer) {
		keepValue(definer);
	}

	protected TypeRef ancestor() {
		if (this.valueTypeInterface != null) {
			return this.valueTypeInterface;
		}
		return this.valueTypeInterface = getValue().getValueTypeInterface();
	}

	private ObjectTypeParameters typeParameters(ObjectDefiner definer) {
		return rescopedTypeParameters()
				.rescope(definer.getField())
				.toObjectTypeParameters();
	}

	private TypeRefParameters rescopedTypeParameters() {

		final TypeRefParameters typeParameters = ancestor().copyParameters();
		final BoundPath path = getRef().getPath();

		if (path.length() == 1) {
			return typeParameters;
		}

		final PrefixPath prefix = path.cut(1).toPrefix(getRef().getScope());

		return typeParameters.prefixWith(prefix);
	}

	private void keepValue(ObjectDefiner definer) {
		definer.define(new KeepValueBlock(this, getValue()));
	}

	private static final class KeepValueBlock extends BlockBuilder {

		private final Ref value;

		KeepValueBlock(LocationInfo location, Ref value) {
			super(location);
			this.value = value;
		}

		@Override
		public void buildBlock(Block<?> block) {

			final Statements<?> statements =
					block.propose(this).alternative(this);

			statements.statement(new KeepValueStatement(
					this,
					statements.nextDistributor(),
					this.value));
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "(= //" + this.value + ')';
		}

	}

}
