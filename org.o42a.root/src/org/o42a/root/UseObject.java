/*
    Root Object Definition
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.root;

import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.DirectiveObject;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.PathWithAlias;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.Name;


@SourcePath(relativeTo = Root.class, value = "use_object.o42a")
public class UseObject extends DirectiveObject {

	static final Name OBJECT = CASE_INSENSITIVE.canonicalName("object");
	static final Name MODULE = CASE_INSENSITIVE.canonicalName("module");
	static final Name ALIAS = CASE_INSENSITIVE.canonicalName("alias");

	private Ref module;
	private Ref object;
	private Ref alias;

	public UseObject(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public void apply(Ref directive, InstructionContext context) {

		final Namespace namespace = directive.getContainer().toNamespace();

		if (namespace == null) {
			getLogger().prohibitedDirective(
					directive.getLocation(),
					"Use object");
			return;
		}

		final Scope scope = directive.getResolution().getScope();
		final Resolver resolver = scope.resolver();
		final Value<?> moduleValue = module().value(resolver);

		if (!moduleValue.getKnowledge().isKnown()) {
			directive.getLogger().unresolvedValue(
					directive.getLocation(),
					"module");
			return;
		}

		final String moduleId = stringValue(moduleValue);

		final Value<?> objectValue = object().value(resolver);

		if (!objectValue.getKnowledge().isKnown()) {
			directive.getLogger().unresolvedValue(
					directive.getLocation(),
					"object");
			return;
		}

		final String pathString = stringValue(objectValue);
		final Name moduleName;

		if (moduleId != null) {
			moduleName = caseInsensitiveName(moduleId);
			if (!moduleName.isValid()) {
				invalidModuleName(directive, moduleId);
				return;
			}
		} else if (pathString == null) {
			noModuleNoObject(directive);
			return;
		} else {
			moduleName = null;
		}

		final Value<?> aliasValue = alias().value(resolver);

		if (!aliasValue.getKnowledge().isKnown()) {
			directive.getLogger().unresolvedValue(
					directive.getLocation(),
					"alias");
			return;
		}

		final String explicitAlias = stringValue(aliasValue);
		final PathWithAlias path =
				directive.getContext().getCompiler().compilePath(
						directive.getScope(),
						moduleName,
						directive,
						pathString);

		if (path == null) {
			return;
		}

		final Name alias;

		if (explicitAlias != null) {

			final Name aliasName = caseInsensitiveName(explicitAlias);

			if (aliasName.isValid()) {
				alias = aliasName;
			} else {
				alias = null;
				directive.getLogger().error(
						"invalid_object_alias",
						directive,
						"Invalid object alias: '%s'",
						explicitAlias);
			}
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
				fieldName(MODULE).key(getScope()).toPath().dereference();

		return this.module = path.bind(this, getScope()).target(distribute());
	}

	private final Ref object() {
		if (this.object != null) {
			return this.object;
		}

		final Path path =
				fieldName(OBJECT).key(getScope()).toPath().dereference();

		return this.object = path.bind(this, getScope()).target(distribute());
	}

	private final Ref alias() {
		if (this.alias != null) {
			return this.alias;
		}

		final Path path =
				fieldName(ALIAS).key(getScope()).toPath().dereference();

		return this.alias = path.bind(this, getScope()).target(distribute());
	}

	static void invalidModuleName(Ref location, String name) {
		location.getLogger().error(
				"invalid_module_name",
				location,
				"Invalid module name: '%s'",
				name);
	}

	static void noModuleNoObject(Ref location) {
		location.getLogger().error(
				"no_module_no_object",
				location,
				"Either module or object should be specified");
	}

	static String stringValue(Value<?> value) {
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
