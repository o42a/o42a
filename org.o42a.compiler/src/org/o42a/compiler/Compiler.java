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

import static org.o42a.compiler.ip.Interpreter.contentBuilder;
import static org.o42a.compiler.ip.ModuleRefVisitor.MODULE_REF_VISITOR;
import static org.o42a.compiler.ip.ModuleRefVisitor.SAME_MODULE_REF_VISITOR;
import static org.o42a.compiler.ip.RefVisitor.REF_VISITOR;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;
import static org.o42a.parser.Grammar.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.o42a.ast.FixedPosition;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.compiler.ip.StatementVisitor;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserWorker;
import org.o42a.util.Source;
import org.o42a.util.log.LoggablePosition;
import org.o42a.util.log.Logger;
import org.o42a.util.log.Logs;


public class Compiler implements BlockCompiler {

	private static final Compiler instance = new Compiler();

	public static Compiler compiler() {
		return instance;
	}

	private Compiler() {
	}

	@Override
	public BlockBuilder compileBlock(CompilerContext context) {

		final SentenceNode[] content = parse(
				DECLARATIVE.content(),
				null,
				context.getLogger(),
				context.getSource());

		if (content == null) {

			final FixedPosition position =
				new FixedPosition(context.getSource());

			return emptyBlock(new Location(
					context,
					new ParenthesesNode(position, position)));
		}

		return contentBuilder(
				new StatementVisitor(context),
				new ParenthesesNode(null, content, null));
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

			return node.accept(REF_VISITOR, scope.distribute());
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

	private <T> T parse(
			Parser<T> parser,
			LocationInfo location,
			Logger logger,
			Source source) {

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

		final RefNode node =
			parse(parser, location, logger, new StringSrc(string));

		if (node == null) {
			logger.invalidReference(location);
		}

		return node;
	}

	private static final class StringSrc extends Source {

		private static final long serialVersionUID = -4569448153375422856L;

		private final String string;

		StringSrc(String string) {
			this.string = string;
		}

		@Override
		public String getName() {
			return this.string;
		}

		@Override
		public Reader open() throws IOException {
			return new StringReader(this.string);
		}

	}

}
