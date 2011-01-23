/*
    Compiler Core
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
package org.o42a.core.st.sentence;

import static org.o42a.core.ScopePlace.localPlace;
import static org.o42a.util.Place.FIRST_PLACE;

import java.util.List;

import org.o42a.codegen.code.Code;
import org.o42a.core.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.*;
import org.o42a.core.st.action.*;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.Lambda;
import org.o42a.util.Place.Trace;
import org.o42a.util.log.Loggable;


public final class ImperativeBlock extends Block<Imperatives> {

	private static final ReturnConditionVisitor RETURN_CONDITION_VISITOR =
		new ReturnConditionVisitor();

	static ImperativeBlock topLevelImperativeBlock(
			LocationSpec location,
			Distributor distributor,
			Statements<?> enclosing,
			String name,
			ImperativeFactory sentenceFactory,
			Lambda<MemberRegistry, LocalScope> memberRegistry) {

		final LocalScope scope =
			enclosing.getMemberRegistry().newLocalScope(
					location,
					distributor,
					name);

		if (scope == null) {
			return null;
		}

		final MemberRegistry registry;

		if (memberRegistry != null) {
			registry = memberRegistry.get(scope);
		} else {
			registry = new LocalRegistry(scope, enclosing.getMemberRegistry());
		}

		return new ImperativeBlock(
				location,
				scope,
				registry,
				sentenceFactory);
	}

	static ImperativeBlock nestedImperativeBlock(
			LocationSpec location,
			Distributor distributor,
			Statements<?> enclosing,
			boolean parentheses,
			String name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		return new ImperativeBlock(
				location,
				distributor,
				enclosing,
				parentheses,
				name,
				memberRegistry,
				sentenceFactory);
	}

	private final boolean parentheses;
	private final String name;
	private final boolean topLevel;
	private final Trace trace;
	private ValueType<?> valueType;
	private Conditions initialConditions;

	public ImperativeBlock(
			LocationSpec location,
			LocalScope scope,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		this(
				location,
				new BlockDistributor(scope),
				memberRegistry,
				sentenceFactory);
	}

	private ImperativeBlock(
			LocationSpec location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(location, distributor, memberRegistry, sentenceFactory);
		this.parentheses = false;
		this.name = distributor.getScope().toLocal().getName();
		this.topLevel = true;
		this.trace = getPlace().nestedTrace();

		final LocalScopeBase scope = getScope();

		scope.setBlock(this);
	}

	private ImperativeBlock(
			LocationSpec location,
			Distributor distributor,
			Statements<?> enclosing,
			boolean parentheses,
			String name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(
				location,
				distributor,
				enclosing,
				memberRegistry,
				sentenceFactory);
		this.parentheses = parentheses;
		this.name = name;
		this.topLevel = false;
		this.trace = getPlace().nestedTrace();
	}

	public final boolean isTopLevel() {
		return this.topLevel;
	}

	@Override
	public LocalScope getScope() {
		return super.getScope().toLocal();
	}

	@Override
	public ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ImperativeSentence> getSentences() {
		return (List<ImperativeSentence>) super.getSentences();
	}

	@Override
	public final boolean isParentheses() {
		return this.parentheses;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}
		if (!getKind().hasValue()) {
			return this.valueType = ValueType.VOID;
		}

		ValueType<?> result = null;

		for (Sentence<?> sentence : getSentences()) {

			final ValueType<?> type = sentence.valueType(result);

			if (type == null) {
				continue;
			}
			if (result == null) {
				result = type;
				continue;
			}
			if (result != type) {
				getLogger().incompatible(sentence, result);
			}
		}

		return this.valueType = result;
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		assert this.initialConditions == null :
			"Conditions already set for " + this;
		this.initialConditions = conditions;
		return new BlockConditions(conditions, this);
	}

	@Override
	public Definitions define(DefinitionTarget target) {
		return this.initialConditions.apply(
				localDef(this, target.getScope())).toDefinitions();
	}

	@Override
	public Action initialValue(LocalScope scope) {
		for (;;) {

			final Action action = initialSentencesValue(scope);

			if (action != null) {
				return action;
			}
		}
	}

	@Override
	public Action initialCondition(LocalScope scope) {
		return initialValue(scope).accept(RETURN_CONDITION_VISITOR);
	}

	@Override
	public ImperativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		if (!isTopLevel()) {

			final ImperativeBlock reproduction =
				reproducer.getStatements().braces(this, getName());

			reproduceSentences(reproducer, reproduction);

			return null;
		}

		final ImperativeBlock reproduction = new ImperativeBlock(
				this,
				reproducer.distribute(),
				reproducer.getMemberRegistry(),
				getSentenceFactory());

		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	public St wrap(Distributor distributor) {
		if (!isTopLevel()) {
			return this;
		}
		return new BracesWithinDeclaratives(this, distributor, this);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Loggable loggable = getLoggable();

		if (loggable != null) {
			loggable.printContent(out);
			if (out.length() < 2 || out.charAt(0) != '{') {
				out.insert(0, '{').append('}');
			}
		} else {
			out.append("{...}");
		}

		return out.toString();
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new Op(builder, this);
	}

	@Override
	Trace getTrace() {
		return this.trace;
	}

	private boolean thisBlock(String blockName) {
		return blockName == null || blockName.equals(getName());
	}

	private Action initialSentencesValue(LocalScope scope) {
		for (ImperativeSentence sentence : getSentences()) {

			final Action action = sentence.initialValue(scope);
			final LoopAction loopAction =
				action.accept(new LoopActionVisitor());

			switch (loopAction) {
			case DONE:
				if (action.getLogicalValue().isTrue()) {
					continue;
				}
				return new ExecuteCommand(action, action.getLogicalValue());
			case PULL:
				return action;
			case EXIT:
				return new ExecuteCommand(action, action.getLogicalValue());
			case REPEAT:
				return null;
			}

			throw new IllegalStateException("Unhandled action: " + action);
		}

		return new ExecuteCommand(this, LogicalValue.TRUE);
	}

	public static final class BlockDistributor extends Distributor {

		private final LocalScope scope;

		public BlockDistributor(LocalScope scope) {
			this.scope = scope;
		}

		@Override
		public LocalScope getScope() {
			return this.scope;
		}

		@Override
		public Container getContainer() {
			return this.scope;
		}

		@Override
		public ScopePlace getPlace() {
			return localPlace(getScope(), FIRST_PLACE);
		}

	}

	private static final class Op extends StOp {

		private Op(LocalBuilder builder, ImperativeBlock block) {
			super(builder, block);
		}

		@Override
		public void allocate(LocalBuilder builder, Code code) {
			for (ImperativeSentence sentence : getBlock().getSentences()) {
				sentence.allocate(builder, code);
			}
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {
			writeSentences(control, result);
		}

		@Override
		public void writeCondition(Control control) {
			writeSentences(control, null);
		}

		private final ImperativeBlock getBlock() {
			return (ImperativeBlock) getStatement();
		}

		private void writeSentences(Control control, ValOp result) {

			final String name = control.name(getBlock().getName()) + "_blk";

			final Code code = control.addBlock(name);
			final Code next = control.addBlock(name + "_next");
			final Control blockControl;

			if (getBlock().isParentheses()) {
				blockControl = control.parentheses(code, next.head());
			} else {
				blockControl = control.braces(code, next.head(), name);
			}

			final List<ImperativeSentence> sentences =
				getBlock().getSentences();
			final int len = sentences.size();

			for (int i = 0; i < len; ++i) {

				final ImperativeSentence sentence = sentences.get(i);

				sentence.write(blockControl, Integer.toString(i), result);
				if (!blockControl.mayContinue()) {
					control.reachability(blockControl);
					return;
				}
			}

			if (code.exists()) {
				control.code().go(code.head());
				if (next.exists()) {
					next.go(control.code().tail());
				}
				if (!blockControl.isDone()) {
					code.go(control.code().tail());
				}
			}

			control.reachability(blockControl);
		}

	}

	private final class LoopActionVisitor
			extends ActionVisitor<Void, LoopAction> {

		@Override
		public LoopAction visitRepeatLoop(
				RepeatLoop repeatLoop,
				Void p) {
			if (thisBlock(repeatLoop.getBlockName())) {
				return LoopAction.REPEAT;
			}
			return LoopAction.PULL;
		}

		@Override
		public LoopAction visitExitLoop(
				ExitLoop exitLoop,
				Void p) {
			if (thisBlock(exitLoop.getBlockName())) {
				return LoopAction.EXIT;
			}
			return LoopAction.PULL;
		}

		@Override
		public LoopAction visitReturnValue(ReturnValue returnValue, Void p) {
			return LoopAction.PULL;
		}

		@Override
		protected LoopAction visitAction(Action action, Void p) {
			return LoopAction.DONE;
		}

	}

	private static final class ReturnConditionVisitor
			extends ActionVisitor<Void, Action> {

		@Override
		public Action visitReturnValue(ReturnValue returnValue, Void p) {

			final Value<?> result = returnValue.getResult();

			return new ReturnValue(
					returnValue,
					result.getLogicalValue()
					.toCond(returnValue, returnValue.getScope())
					.toValue());
		}

		@Override
		protected Action visitAction(Action action, Void p) {
			return action;
		}
	}

	private enum LoopAction {

		EXIT,
		REPEAT,
		PULL,
		DONE

	}

	private static final class BlockConditions extends Conditions {

		private final Conditions conditions;
		private final ImperativeBlock block;

		BlockConditions(Conditions conditions, ImperativeBlock block) {
			this.conditions = conditions;
			this.block = block;
		}

		@Override
		public Cond prerequisite(Scope scope) {
			return this.conditions.prerequisite(scope);
		}

		@Override
		public Cond condition(Scope scope) {
			return this.conditions.condition(scope).and(
					localDef(this.block, scope).fullCondition());
		}

		@Override
		public String toString() {
			return this.conditions + ", " + this.block;
		}

	}

}
