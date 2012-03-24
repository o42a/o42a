/*
    Intrinsics
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.Scope;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.PathWithAlias;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


@SourcePath(relativeTo = Root.class, value = "use_object.o42a")
public class UseObject extends DirectiveObject {

	private Ref module;
	private Ref object;
	private Ref alias;

	public UseObject(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public void apply(Ref directive, InstructionContext context) {

		final Namespace namespace = directive.getContainer().toNamespace();

		if (namespace == null) {
			getLogger().prohibitedDirective(directive, "Use object");
			return;
		}

		final Scope scope = directive.getResolution().getScope();
		final Resolver resolver = scope.dummyResolver();
		final Value<?> moduleValue = module().value(resolver);

		if (!moduleValue.getKnowledge().isKnown()) {
			getLogger().unresolvedValue(directive, "module");
			return;
		}

		final String moduleId = stringValue(moduleValue);

		final Value<?> objectValue = object().value(resolver);

		if (!objectValue.getKnowledge().isKnown()) {
			getLogger().unresolvedValue(directive, "object");
			return;
		}

		final String pathString = stringValue(objectValue);

		if (pathString == null && moduleId == null) {
			getLogger().noModuleNoObject(directive);
			return;
		}

		final Value<?> aliasValue = alias().value(resolver);

		if (!aliasValue.getKnowledge().isKnown()) {
			getLogger().unresolvedValue(directive, "alias");
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

	private final Ref module() {
		if (this.module != null) {
			return this.module;
		}

		final Path path =
				fieldName("module").key(getScope()).toPath().mayDereference();

		return this.module = path.bind(this, getScope()).target(distribute());
	}

	private final Ref object() {
		if (this.object != null) {
			return this.object;
		}

		final Path path =
				fieldName("object").key(getScope()).toPath().mayDereference();

		return this.object = path.bind(this, getScope()).target(distribute());
	}

	private final Ref alias() {
		if (this.alias != null) {
			return this.alias;
		}

		final Path path =
				fieldName("alias").key(getScope()).toPath().mayDereference();

		return this.alias = path.bind(this, getScope()).target(distribute());
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
