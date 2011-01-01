/*
    Compiler JNI Bindings to LLVM
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
#include "o42a/llvm/BackendModule.h"

#include "o42a/llvm/debug.h"

#include "llvm/DerivedTypes.h"
#include "llvm/LLVMContext.h"
#include "llvm/PassManager.h"
#include "llvm/Analysis/Verifier.h"
#include "llvm/Assembly/PrintModulePass.h"
#include "llvm/CodeGen/AsmPrinter.h"
#include "llvm/MC/MCContext.h"
#include "llvm/MC/MCStreamer.h"
#include "llvm/Support/CommandLine.h"
#include "llvm/Support/FormattedStream.h"
#include "llvm/System/Host.h"
#include "llvm/Target/SubtargetFeature.h"
#include "llvm/Target/TargetData.h"
#include "llvm/Target/TargetMachine.h"
#include "llvm/Target/TargetRegistry.h"
#include "llvm/Target/TargetSelect.h"
#include "llvm/Transforms/Scalar.h"


using namespace llvm;


namespace o42ac {

static cl::opt<std::string> OutputFilename(
		"o",
		cl::Prefix,
		cl::ValueRequired,
		cl::desc("Override output filename"),
		cl::value_desc("filename"));

BackendModule::BackendModule(StringRef ModuleID, LLVMContext &context) :
		Module(ModuleID, context),
		targetData(NULL),
		functionPassManager(NULL) {
}

BackendModule::~BackendModule() {
	if (this->targetData) {
		delete this->targetData;
	}
	if (this->functionPassManager) {
		delete this->functionPassManager;
	}
}

BackendModule *BackendModule::createBackend(StringRef &ModuleID) {
	return new BackendModule(ModuleID, *new LLVMContext());
}

const TargetData &BackendModule::getTargetData() const {
	if (this->targetData) {
		return *this->targetData;
	}

	OTRACE("--------- Initializing\n");
	InitializeAllTargetInfos();
	InitializeAllTargets();
	InitializeAllAsmPrinters();

	this->targetData = new TargetData(this);

	return *this->targetData;
}

bool BackendModule::validateFunction(Function *const function) {
	OTRACE("--------- Validating " << function->getName() << "\n");

	if (verifyFunction(*function, PrintMessageAction)) {
		ODEBUG("--------- Validation failed: " << function->getName() << "\n");
		ODDUMP(function);
		ODEBUG("---------\n");

		return false;
	}

	OTRACE("--------- Validation succeed: " << function->getName() << "\n");

	if (!this->functionPassManager) {
		this->functionPassManager = new FunctionPassManager(this);
		this->functionPassManager->add(new TargetData(this->getTargetData()));
		// Do simple "peephole" optimizations and bit-twiddling optzns.
		this->functionPassManager->add(createInstructionCombiningPass());
		// Reassociate expressions.
		this->functionPassManager->add(createReassociatePass());
		// Eliminate Common SubExpressions.
		this->functionPassManager->add(createGVNPass());
		// Simplify the control flow graph (deleting unreachable blocks, etc).
		this->functionPassManager->add(createCFGSimplificationPass());
	}

	this->functionPassManager->run(*function);

	return true;
}

bool BackendModule::writeCode() {

	OTRACE("========= " << this->getModuleIdentifier() << " verification\n");

	if (verifyModule(*this, PrintMessageAction)) {
		return false;
	}

	OTRACE("========== " << this->getModuleIdentifier() << " generation\n");

	PassManager pm;

	std::string triple = getTargetTriple();
	std::string features;

	if (triple.empty()) {
		triple = sys::getHostTriple();

		StringMap<bool> featureMap;
		SubtargetFeatures f;

		f.getDefaultSubtargetFeatures(sys::getHostCPUName(), Triple(triple));

		f.setCPU(sys::getHostCPUName());
		if (sys::getHostCPUFeatures(featureMap)) {
			for (StringMapIterator<bool> it = featureMap.begin();
					it != featureMap.end();) {
				f.AddFeature(it->getKey(), it->getValue());
			}
		}

		features = f.getString();
	}

	std::string error =
			std::string("Target not supported: ").append(triple);

	ODEBUG("Emitting code for " << triple << " (" << features << ")\n");

	const Target *const target = TargetRegistry::lookupTarget(triple, error);

	if (!target) {
		return false;
	}

	TargetMachine *const machine =
			target->createTargetMachine(getTargetTriple(), features);

	std::string output = OutputFilename.getValue();
	formatted_raw_ostream *out;
	std::auto_ptr<formatted_raw_ostream> outPtr;

	if (output.empty()) {
		out = &fouts();
	} else {

		std::string errorInfo;

		raw_fd_ostream *const ostream =
				new raw_fd_ostream(
						OutputFilename.c_str(),
						errorInfo,
						raw_fd_ostream::F_Binary);

		if (!errorInfo.empty()) {
			errs() << errorInfo << '\n';
			return false;
		}

		out = new formatted_raw_ostream(*ostream, true);
		outPtr.reset(out);
	}

	if (machine->addPassesToEmitFile(
			pm,
			*out,
			TargetMachine::CGFT_AssemblyFile,
			CodeGenOpt::Default)) {
		errs() << "Can not emit code\n";
		return false;
	}

	pm.run(*this);

	return true;
}

void BackendModule::destroyBackend() {

	LLVMContext *context = &this->getContext();

	delete this;
	delete context;
}

} /* o42a */
