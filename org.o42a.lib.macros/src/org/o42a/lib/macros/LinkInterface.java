/*
    Standard Macros
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
package org.o42a.lib.macros;

import org.o42a.common.macro.AnnotatedMacro;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.macro.MacroExpander;
import org.o42a.util.log.LogInfo;


@SourcePath(relativeTo = MacrosModule.class, value = "interface__.o42a")
final class LinkInterface extends AnnotatedMacro {

	private Ref link;

	LinkInterface(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Path expand(MacroExpander expander) {
		return linkInterface(expander);
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		return linkInterface(expander);
	}

	private Path linkInterface(MacroExpander expander) {

		final TypeParameters<KnownLink> linkParameters = linkParameters(expander);

		if (linkParameters == null) {
			return null;
		}

		return interfaceRef(expander, linkParameters).getPath().getPath();
	}

	private TypeParameters<KnownLink> linkParameters(MacroExpander expander) {

		final Scope scope = expander.getMacroObject().getScope();
		final Obj target =
				link().upgradeScope(scope).resolve(scope.resolver())	.toObject();

		if (target == null) {
			// Log the error unconditionally.
			notLink(expander, expander.getExplicitLogger());
			return null;
		}

		final TypeParameters<KnownLink> linkParameters =
				target.type().getParameters().toLinkParameters();

		if (linkParameters == null) {
			// Conditionally report the error.
			notLink(expander, expander.getLogger());
			return null;
		}

		return linkParameters;
	}

	private TypeRef interfaceRef(
			MacroExpander expander,
			TypeParameters<KnownLink> linkParameters) {

		final Scope scope = expander.getMacroObject().getScope();
		final PrefixPath prefix =
				link().getPath().rebuildIn(scope).toPrefix(scope);

		return linkParameters.getValueType()
				.toLinkType()
				.interfaceRef(linkParameters)
				.prefixWith(prefix);
	}

	private Ref link() {
		if (this.link != null) {
			return this.link;
		}
		return this.link = LinkSubjectDep.linkRef(this);
	}

	private void notLink(LogInfo location, CompilerLogger logger) {
		logger.error(
				"not_link_interface",
				location,
				"Can only obtain interface from link or variable");
	}

}
