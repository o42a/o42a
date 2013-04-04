/*
    Compiler
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
package org.o42a.compiler;

import static org.o42a.compiler.ip.Interpreter.PATH_COMPILER_IP;
import static org.o42a.compiler.ip.access.AccessRules.ACCESS_FROM_DECLARATION;
import static org.o42a.compiler.ip.ref.ModuleRefVisitor.MODULE_REF_VISITOR;
import static org.o42a.compiler.ip.ref.ModuleRefVisitor.SAME_MODULE_REF_VISITOR;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.parser.Grammar.*;
import static org.o42a.util.log.LogDetail.logDetail;

import java.io.IOException;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.file.FileNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.file.*;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.Source;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.StringSource;
import org.o42a.util.log.DetailedLogger;
import org.o42a.util.log.LogDetail;
import org.o42a.util.log.Logger;
import org.o42a.util.string.Name;


public class Compiler implements SourceCompiler {

	private static final Compiler instance = new Compiler();
	private static final LogDetail LOCATION_LOG_DETAIL =
			logDetail("compiler.location", "Location");

	public static Compiler compiler() {
		return instance;
	}

	private Compiler() {
	}

	@Override
	public ModuleCompiler compileModule(ObjectSource source) {

		final FileNode node = parseModule(source);

		return validate(new FileModuleCompiler(source, node));
	}

	@Override
	public FieldCompiler compileField(ObjectSource source) {

		final FileNode node = parseModule(source);

		return validate(new FileFieldCompiler(source, node));
	}

	@Override
	public DefinitionCompiler compileDefinition(DefinitionSource source) {

		final FileNode node = parseModule(source);

		return validate(new FileDefinitionCompiler(source, node));
	}

	@Override
	public PathWithAlias compilePath(
			Scope scope,
			Name moduleName,
			LocationInfo location,
			String string) {
		if (string == null) {
			return new PathWithAlias(
					modulePath(moduleName)
					.bind(location, scope)
					.target(scope.distribute()),
					moduleName);
		}

		if (moduleName == null) {

			final RefNode node = parsePath(
					ref(),
					location,
					scope.getLogger(),
					string);

			if (node == null) {
				return null;
			}

			return pathWithAlias(
					node,
					node.accept(
							PATH_COMPILER_IP.targetRefVisitor(),
							ACCESS_FROM_DECLARATION.distribute(
									scope.distribute())));
		}

		final SourcePosition pos = new SourcePosition(
				location.getLocation().getContext().getSource());
		final MemberRefNode ownerNode = new MemberRefNode(
				null,
				null,
				new NameNode(pos, pos, moduleName),
				null);
		final RefNode node;

		if (string.startsWith("@@")) {
			node = parsePath(
					adapterRef(ownerNode),
					location,
					scope.getLogger(),
					string);
		} else {
			node = parsePath(
					memberRef(ownerNode, false),
					location,
					scope.getLogger(),
					string);
		}
		if (node == null) {
			return null;
		}

		return pathWithAlias(
				node,
				node.accept(
						insideModule(moduleName, scope)
						? SAME_MODULE_REF_VISITOR : MODULE_REF_VISITOR,
						ACCESS_FROM_DECLARATION.distribute(
								scope.distribute())));
	}

	private static PathWithAlias pathWithAlias(RefNode node, Ref path) {
		if (path == null) {
			return null;
		}
		if (node instanceof MemberRefNode) {

			final MemberRefNode memberRefNode = (MemberRefNode) node;
			final NameNode name = memberRefNode.getName();

			if (name != null) {
				return new PathWithAlias(path, name.getName());
			}
		}
		return new PathWithAlias(path, null);
	}

	private static boolean insideModule(Name moduleName, Scope scope) {

		final Obj module =
				scope.getContext().getIntrinsics().getModule(moduleName);

		if (module == null) {
			return false;
		}

		return module.getScope().contains(scope);
	}

	private FileNode parseModule(DefinitionSource source) {

		final FileNode moduleNode =
				parse(file(), source.getLogger(), source.getSource());

		if (moduleNode != null) {
			return moduleNode;
		}

		final SourcePosition position = new SourcePosition(source.getSource());

		return new FileNode(position, position);
	}

	private <T> T parse(Parser<T> parser, Logger logger, Source source) {
		if (source.isEmpty()) {
			return null;
		}

		final ParserWorker worker = new ParserWorker(source);

		worker.setLogger(logger);
		try {
			return worker.parse(parser);
		} finally {
			try {
				worker.close();
			} catch (IOException e) {
				worker.getParserLogger().ioError(
						worker.position(),
						e.getLocalizedMessage());
			}
		}
	}

	private RefNode parsePath(
			Parser<? extends RefNode> parser,
			LocationInfo location,
			CompilerLogger logger,
			String string) {

		final DetailedLogger log = new DetailedLogger(
				logger,
				LOCATION_LOG_DETAIL,
				location.getLocation());
		final RefNode node =
				parse(parser, log, new StringSource(string, string));

		if (node == null) {
			logger.invalidReference(location.getLocation());
		}

		return node;
	}

	private <C extends AbstractDefinitionCompiler<?>> C validate(C compiler) {
		if (compiler.getFileName().isValid()) {
			return compiler;
		}

		final DefinitionSource source = compiler.getSource();

		source.getLogger().error(
				"invalid_file_name",
				source.getSource(),
				"Invalid source file name: %s",
				source.getFileName());

		return null;
	}

}
