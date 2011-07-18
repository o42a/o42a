/*
    Compiler
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
package org.o42a.compiler;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.ModuleRefVisitor.MODULE_REF_VISITOR;
import static org.o42a.compiler.ip.ModuleRefVisitor.SAME_MODULE_REF_VISITOR;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.parser.Grammar.*;

import java.io.IOException;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.module.ModuleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.module.*;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.Source;
import org.o42a.util.io.StringSource;
import org.o42a.util.log.LoggablePosition;
import org.o42a.util.log.Logger;
import org.o42a.util.log.Logs;


public class Compiler implements SourceCompiler {

	private static final Compiler instance = new Compiler();

	public static Compiler compiler() {
		return instance;
	}

	private Compiler() {
	}

	@Override
	public ModuleCompiler compileModule(ObjectSource source) {

		final ModuleNode node = parseModule(source);

		return validate(new ObjectModuleCompiler(source, node));
	}

	@Override
	public FieldCompiler compileField(ObjectSource source) {

		final ModuleNode node = parseModule(source);

		return validate(new FieldModuleCompiler(source, node));
	}

	@Override
	public DefinitionCompiler compileDefinition(DefinitionSource source) {

		final ModuleNode node = parseModule(source);

		return validate(new DefinitionModuleCompiler(source, node));
	}

	@Override
	public Ref compilePath(
			Scope scope,
			String moduleId,
			LocationInfo location,
			String string) {
		if (string == null) {
			return modulePath(moduleId).target(location, scope.distribute());
		}

		if (moduleId == null) {

			final RefNode node = parsePath(
					ref(),
					location,
					scope.getLogger(),
					string);

			if (node == null) {
				return null;
			}

			return node.accept(PLAIN_IP.refVisitor(), scope.distribute());
		}

		final FixedPosition pos =
				new FixedPosition(location.getContext().getSource());
		final MemberRefNode ownerNode = new MemberRefNode(
				null,
				null,
				new NameNode(pos, pos, moduleId),
				null,
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

		return node.accept(
				insideModule(moduleId, scope)
				? SAME_MODULE_REF_VISITOR : MODULE_REF_VISITOR,
				scope.distribute());
	}

	private static boolean insideModule(String moduleId, Scope scope) {

		final Obj module =
				scope.getContext().getIntrinsics().getModule(moduleId);

		if (module == null) {
			return false;
		}

		return module.getScope().contains(scope);
	}

	private ModuleNode parseModule(DefinitionSource source) {

		final ModuleNode moduleNode =
				parse(module(), null, source.getLogger(), source.getSource());

		if (moduleNode != null) {
			return moduleNode;
		}

		final FixedPosition position = new FixedPosition(source.getSource());

		return new ModuleNode(position, position);
	}

	private <T> T parse(
			Parser<T> parser,
			LocationInfo location,
			Logger logger,
			Source source) {
		if (source.isEmpty()) {
			return null;
		}

		final ParserWorker worker;

		if (location != null) {

			final LoggablePosition start = Logs.start(location);

			if (start != null) {
				worker = new ParserWorker(
						source,
						new FixedPosition(
								start.source(),
								start.line(),
								start.column(),
								start.offset()));
			} else {
				worker = new ParserWorker(source);
			}
		} else {
			worker = new ParserWorker(source);
		}

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

		final RefNode node = parse(
				parser,
				location,
				logger,
				new StringSource(string, string));

		if (node == null) {
			logger.invalidReference(location);
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
