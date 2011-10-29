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

import static org.o42a.core.member.MemberId.fieldName;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.DirectiveObject;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Namespace;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.PathWithAlias;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = Root.class, value = "use_object.o42a")
public class UseObject extends DirectiveObject {

	private final MemberKey moduleKey;
	private final MemberKey objectKey;
	private final MemberKey aliasKey;

	public UseObject(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
		this.moduleKey = fieldName("module").key(getScope());
		this.objectKey = fieldName("object").key(getScope());
		this.aliasKey = fieldName("alias").key(getScope());
	}

	@Override
	public void apply(Ref directive, InstructionContext context) {

		final Namespace namespace = directive.getContainer().toNamespace();

		if (namespace == null) {
			getLogger().prohibitedDirective(directive, "Use object");
			return;
		}

		final Obj object = directive.getResolution().materialize();
		final Field<?> moduleField =
				object.member(this.moduleKey).toField(context);
		final Value<?> moduleValue =
				moduleField.getArtifact().materialize()
				.value().explicitUseBy(context).getValue();

		if (!moduleValue.getKnowledge().isKnown()) {
			getLogger().unresolvedValue(
					moduleField,
					moduleField.getDisplayName());
			return;
		}

		final String moduleId = stringValue(moduleValue);

		final Field<?> objectField =
				object.member(this.objectKey).toField(context);
		final Value<?> objectValue =
				objectField.getArtifact().materialize()
				.value().explicitUseBy(context).getValue();

		if (!objectValue.getKnowledge().isKnown()) {
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

		final Field<?> aliasField =
				object.member(this.aliasKey).toField(context);
		final Value<?> aliasValue =
				aliasField.getArtifact().materialize()
				.value().explicitUseBy(context).getValue();

		if (!aliasValue.getKnowledge().isKnown()) {
			getLogger().unresolvedValue(
					aliasField,
					aliasField.getDisplayName());
			return;
		}

		final String explicitAlias = stringValue(aliasValue);
		final PathWithAlias path =
				directive.getContext().getCompiler().compilePath(
						directive.getScope(),
						moduleId,
						directive,
						pathString);

		if (path == null) {
			return;
		}

		final String alias;

		if (explicitAlias != null) {
			alias = explicitAlias;
		} else {
			alias = path.getAlias();
			if (alias == null) {
				directive.getLogger().error(
						"missing_use_object_alias",
						directive,
						"Object alias required");
				return;
			}
		}

		namespace.useObject(path.getPath(), alias);
	}

	private static String stringValue(Value<?> value) {
		if (value.getKnowledge().isFalse()) {
			return null;
		}

		final String string =
				ValueType.STRING.cast(value).getCompilerValue().trim();

		if (string.isEmpty()) {
			return null;
		}

		return string;
	}

}
