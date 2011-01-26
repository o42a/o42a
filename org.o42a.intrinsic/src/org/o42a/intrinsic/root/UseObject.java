/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import static org.o42a.core.member.MemberId.memberName;

import org.o42a.common.intrinsic.IntrinsicDirective;
import org.o42a.core.Namespace;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class UseObject extends IntrinsicDirective {

	private final MemberKey moduleKey;
	private final MemberKey objectKey;
	private final MemberKey aliasKey;

	public UseObject(Root root) {
		super(root, "use_object");
		this.moduleKey = memberName("module").key(getScope());
		this.objectKey = memberName("object").key(getScope());
		this.aliasKey = memberName("alias").key(getScope());
	}

	@Override
	public <S extends Statements<S>> void apply(Block<S> block, Ref directive) {

		final Namespace namespace = directive.getContainer().toNamespace();

		if (namespace == null) {
			getLogger().prohibitedDirective(directive, "Use object");
			return;
		}

		final Obj object = directive.getResolution().materialize();
		final Field<?> moduleField = object.member(this.moduleKey).toField();
		final Value<?> moduleValue =
			moduleField.getArtifact().materialize().getValue();

		if (!moduleValue.isDefinite()) {
			getLogger().unresolvedValue(
					moduleField,
					moduleField.getDisplayName());
			return;
		}

		final String moduleId = stringValue(moduleValue);

		final Field<?> objectField = object.member(this.objectKey).toField();
		final Value<?> objectValue =
			objectField.getArtifact().materialize().getValue();

		if (!objectValue.isDefinite()) {
			getLogger().unresolvedValue(
					objectField,
					objectField.getDisplayName());
			return;
		}

		final String pathString = stringValue(objectValue);

		if (pathString == null && moduleId == null) {
			getLogger().noModuleNoObject(directive);
			return;
		}

		final Field<?> aliasField = object.member(this.aliasKey).toField();
		final Value<?> aliasValue =
			aliasField.getArtifact().materialize().getValue();

		if (!aliasValue.isDefinite()) {
			getLogger().unresolvedValue(
					aliasField,
					aliasField.getDisplayName());
			return;
		}

		final String alias = stringValue(aliasValue);

		final Ref path = directive.getContext().getCompiler().compilePath(
				directive.getScope(),
				moduleId,
				directive,
				pathString);

		if (path == null) {
			return;
		}

		namespace.useObject(path, alias);
	}

	@Override
	protected void postResolve() {
		includeSource("use_object.o42a");
		super.postResolve();
	}

	private static String stringValue(Value<?> value) {
		if (value.isFalse()) {
			return null;
		}

		final String string =
			ValueType.STRING.cast(value).getDefiniteValue().trim();

		if (string.isEmpty()) {
			return null;
		}

		return string;
	}

}
