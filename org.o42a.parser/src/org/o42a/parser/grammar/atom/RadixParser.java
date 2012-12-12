package org.o42a.parser.grammar.atom;

import static org.o42a.ast.atom.Radix.BINARY_RADIX;
import static org.o42a.ast.atom.Radix.HEXADECIMAL_RADIX;

import org.o42a.ast.atom.Radix;
import org.o42a.ast.atom.SignNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


final class RadixParser implements Parser<SignNode<Radix>> {

	static final RadixParser RADIX = new RadixParser();

	private RadixParser() {
	}

	@Override
	public SignNode<Radix> parse(ParserContext context) {
		if (context.next() != '0') {
			return null;
		}

		final SourcePosition start = context.current().fix();
		final Radix radix;

		switch (context.next()) {
		case 'x':
		case 'X':
			radix = HEXADECIMAL_RADIX;
			break;
		case 'b':
		case 'B':
			radix = BINARY_RADIX;
			break;
		default:
			return null;
		}

		context.acceptAll();

		return new SignNode<Radix>(start, context.current().fix(), radix);
	}

}
