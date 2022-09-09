/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.graal.python.compiler;

import static com.oracle.graal.python.compiler.OpCodes.ADD_TO_COLLECTION;
import static com.oracle.graal.python.compiler.OpCodes.BINARY_OP;
import static com.oracle.graal.python.compiler.OpCodes.BINARY_SUBSCR;
import static com.oracle.graal.python.compiler.OpCodes.BUILD_SLICE;
import static com.oracle.graal.python.compiler.OpCodes.CALL_FUNCTION;
import static com.oracle.graal.python.compiler.OpCodes.CALL_FUNCTION_KW;
import static com.oracle.graal.python.compiler.OpCodes.CALL_FUNCTION_VARARGS;
import static com.oracle.graal.python.compiler.OpCodes.CALL_METHOD;
import static com.oracle.graal.python.compiler.OpCodes.CALL_METHOD_VARARGS;
import static com.oracle.graal.python.compiler.OpCodes.CLOSURE_FROM_STACK;
import static com.oracle.graal.python.compiler.OpCodes.COLLECTION_ADD_COLLECTION;
import static com.oracle.graal.python.compiler.OpCodes.COLLECTION_ADD_STACK;
import static com.oracle.graal.python.compiler.OpCodes.COLLECTION_FROM_COLLECTION;
import static com.oracle.graal.python.compiler.OpCodes.COLLECTION_FROM_STACK;
import static com.oracle.graal.python.compiler.OpCodes.CollectionBits;
import static com.oracle.graal.python.compiler.OpCodes.DELETE_ATTR;
import static com.oracle.graal.python.compiler.OpCodes.DELETE_DEREF;
import static com.oracle.graal.python.compiler.OpCodes.DELETE_FAST;
import static com.oracle.graal.python.compiler.OpCodes.DELETE_GLOBAL;
import static com.oracle.graal.python.compiler.OpCodes.DELETE_NAME;
import static com.oracle.graal.python.compiler.OpCodes.DELETE_SUBSCR;
import static com.oracle.graal.python.compiler.OpCodes.DUP_TOP;
import static com.oracle.graal.python.compiler.OpCodes.END_EXC_HANDLER;
import static com.oracle.graal.python.compiler.OpCodes.EXIT_WITH;
import static com.oracle.graal.python.compiler.OpCodes.FORMAT_VALUE;
import static com.oracle.graal.python.compiler.OpCodes.FOR_ITER;
import static com.oracle.graal.python.compiler.OpCodes.GET_AWAITABLE;
import static com.oracle.graal.python.compiler.OpCodes.GET_ITER;
import static com.oracle.graal.python.compiler.OpCodes.IMPORT_FROM;
import static com.oracle.graal.python.compiler.OpCodes.IMPORT_NAME;
import static com.oracle.graal.python.compiler.OpCodes.IMPORT_STAR;
import static com.oracle.graal.python.compiler.OpCodes.JUMP_BACKWARD;
import static com.oracle.graal.python.compiler.OpCodes.JUMP_FORWARD;
import static com.oracle.graal.python.compiler.OpCodes.JUMP_IF_FALSE_OR_POP;
import static com.oracle.graal.python.compiler.OpCodes.JUMP_IF_TRUE_OR_POP;
import static com.oracle.graal.python.compiler.OpCodes.KWARGS_DICT_MERGE;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_ASSERTION_ERROR;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_ATTR;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_BIGINT;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_BUILD_CLASS;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_BYTE;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_BYTES;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_CLASSDEREF;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_CLOSURE;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_COMPLEX;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_CONST;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_CONST_COLLECTION;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_DEREF;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_DOUBLE;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_ELLIPSIS;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_FALSE;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_FAST;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_GLOBAL;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_INT;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_LONG;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_METHOD;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_NAME;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_NONE;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_STRING;
import static com.oracle.graal.python.compiler.OpCodes.LOAD_TRUE;
import static com.oracle.graal.python.compiler.OpCodes.MAKE_FUNCTION;
import static com.oracle.graal.python.compiler.OpCodes.MAKE_KEYWORD;
import static com.oracle.graal.python.compiler.OpCodes.MATCH_EXC_OR_JUMP;
import static com.oracle.graal.python.compiler.OpCodes.POP_AND_JUMP_IF_FALSE;
import static com.oracle.graal.python.compiler.OpCodes.POP_AND_JUMP_IF_TRUE;
import static com.oracle.graal.python.compiler.OpCodes.POP_EXCEPT;
import static com.oracle.graal.python.compiler.OpCodes.POP_TOP;
import static com.oracle.graal.python.compiler.OpCodes.PRINT_EXPR;
import static com.oracle.graal.python.compiler.OpCodes.PUSH_EXC_INFO;
import static com.oracle.graal.python.compiler.OpCodes.RAISE_VARARGS;
import static com.oracle.graal.python.compiler.OpCodes.RESUME_YIELD;
import static com.oracle.graal.python.compiler.OpCodes.RETURN_VALUE;
import static com.oracle.graal.python.compiler.OpCodes.ROT_THREE;
import static com.oracle.graal.python.compiler.OpCodes.ROT_TWO;
import static com.oracle.graal.python.compiler.OpCodes.SEND;
import static com.oracle.graal.python.compiler.OpCodes.SETUP_ANNOTATIONS;
import static com.oracle.graal.python.compiler.OpCodes.SETUP_WITH;
import static com.oracle.graal.python.compiler.OpCodes.STORE_ATTR;
import static com.oracle.graal.python.compiler.OpCodes.STORE_DEREF;
import static com.oracle.graal.python.compiler.OpCodes.STORE_FAST;
import static com.oracle.graal.python.compiler.OpCodes.STORE_GLOBAL;
import static com.oracle.graal.python.compiler.OpCodes.STORE_NAME;
import static com.oracle.graal.python.compiler.OpCodes.STORE_SUBSCR;
import static com.oracle.graal.python.compiler.OpCodes.THROW;
import static com.oracle.graal.python.compiler.OpCodes.TUPLE_FROM_LIST;
import static com.oracle.graal.python.compiler.OpCodes.UNARY_OP;
import static com.oracle.graal.python.compiler.OpCodes.UNPACK_EX;
import static com.oracle.graal.python.compiler.OpCodes.UNPACK_SEQUENCE;
import static com.oracle.graal.python.compiler.OpCodes.UNWRAP_EXC;
import static com.oracle.graal.python.compiler.OpCodes.YIELD_VALUE;
import static com.oracle.graal.python.nodes.StringLiterals.T_EMPTY_STRING;
import static com.oracle.graal.python.util.PythonUtils.toTruffleStringUncached;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.pegparser.ErrorCallback;
import com.oracle.graal.python.pegparser.ErrorCallback.ErrorType;
import com.oracle.graal.python.pegparser.FExprParser;
import com.oracle.graal.python.pegparser.FutureFeature;
import com.oracle.graal.python.pegparser.InputType;
import com.oracle.graal.python.pegparser.NodeFactory;
import com.oracle.graal.python.pegparser.NodeFactoryImp;
import com.oracle.graal.python.pegparser.Parser;
import com.oracle.graal.python.pegparser.ParserTokenizer;
import com.oracle.graal.python.pegparser.scope.Scope;
import com.oracle.graal.python.pegparser.scope.ScopeEnvironment;
import com.oracle.graal.python.pegparser.sst.AliasTy;
import com.oracle.graal.python.pegparser.sst.ArgTy;
import com.oracle.graal.python.pegparser.sst.ArgumentsTy;
import com.oracle.graal.python.pegparser.sst.BoolOpTy;
import com.oracle.graal.python.pegparser.sst.CmpOpTy;
import com.oracle.graal.python.pegparser.sst.ComprehensionTy;
import com.oracle.graal.python.pegparser.sst.ConstantValue;
import com.oracle.graal.python.pegparser.sst.ExceptHandlerTy;
import com.oracle.graal.python.pegparser.sst.ExprContextTy;
import com.oracle.graal.python.pegparser.sst.ExprTy;
import com.oracle.graal.python.pegparser.sst.KeywordTy;
import com.oracle.graal.python.pegparser.sst.MatchCaseTy;
import com.oracle.graal.python.pegparser.sst.ModTy;
import com.oracle.graal.python.pegparser.sst.PatternTy;
import com.oracle.graal.python.pegparser.sst.SSTNode;
import com.oracle.graal.python.pegparser.sst.SSTreeVisitor;
import com.oracle.graal.python.pegparser.sst.StmtTy;
import com.oracle.graal.python.pegparser.sst.TypeIgnoreTy;
import com.oracle.graal.python.pegparser.sst.UnaryOpTy;
import com.oracle.graal.python.pegparser.sst.WithItemTy;
import com.oracle.graal.python.pegparser.tokenizer.SourceRange;
import com.oracle.graal.python.util.PythonUtils;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.strings.TruffleString;

/**
 * Compiler for bytecode interpreter.
 */
public class Compiler implements SSTreeVisitor<Void> {
    public static final int BYTECODE_VERSION = 25;

    private final ErrorCallback errorCallback;

    ScopeEnvironment env;
    EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
    EnumSet<FutureFeature> futureFeatures = EnumSet.noneOf(FutureFeature.class);
    int futureLineno = -1;
    int optimizationLevel = 0;
    int nestingLevel = 0;
    CompilationUnit unit;
    List<CompilationUnit> stack = new ArrayList<>();
    private boolean interactive;

    public enum Flags {
    }

    public Compiler(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    public CompilationUnit compile(ModTy mod, EnumSet<Flags> flags, int optimizationLevel) {
        this.flags = flags;
        if (mod instanceof ModTy.Module) {
            parseFuture(((ModTy.Module) mod).body);
        } else if (mod instanceof ModTy.Interactive) {
            parseFuture(((ModTy.Interactive) mod).body);
        }
        this.env = ScopeEnvironment.analyze(mod, errorCallback, futureFeatures);
        this.optimizationLevel = optimizationLevel;
        enterScope("<module>", CompilationScope.Module, mod);
        mod.accept(this);
        CompilationUnit topUnit = unit;
        exitScope();
        return topUnit;
    }

    private void parseFuture(StmtTy[] modBody) {
        if (modBody == null || modBody.length == 0) {
            return;
        }
        boolean done = false;
        int prevLine = 0;
        int i = 0;
        if (getDocstring(modBody) != null) {
            i++;
        }
        for (; i < modBody.length; i++) {
            StmtTy s = modBody[i];
            int line = s.getSourceRange().startLine;
            if (done && line > prevLine) {
                return;
            }
            prevLine = line;
            if (s instanceof StmtTy.ImportFrom) {
                StmtTy.ImportFrom importFrom = (StmtTy.ImportFrom) s;
                if ("__future__".equals(importFrom.module)) {
                    if (done) {
                        errorCallback.onError(ErrorType.Syntax, s.getSourceRange(), "from __future__ imports must occur at the beginning of the file");
                    }
                    parseFutureFeatures(importFrom, futureFeatures);
                    futureLineno = line;
                } else {
                    done = true;
                }
            } else {
                done = true;
            }
        }
    }

    private void parseFutureFeatures(StmtTy.ImportFrom node, EnumSet<FutureFeature> features) {
        for (AliasTy alias : node.names) {
            if (alias.name != null) {
                switch (alias.name) {
                    case "nested_scopes":
                    case "generators":
                    case "division":
                    case "absolute_import":
                    case "with_statement":
                    case "print_function":
                    case "unicode_literals":
                    case "generator_stop":
                        break;
                    case "barry_as_FLUFL":
                        features.add(FutureFeature.BARRY_AS_BDFL);
                        break;
                    case "annotations":
                        features.add(FutureFeature.ANNOTATIONS);
                        break;
                    case "braces":
                        errorCallback.onError(ErrorType.Syntax, node.getSourceRange(), "not a chance");
                        break;
                    default:
                        errorCallback.onError(ErrorType.Syntax, node.getSourceRange(), "future feature %s is not defined", alias.name);
                        break;
                }
            }
        }
    }

    private List<Instruction> quickeningStack = new ArrayList<>();

    void pushOp(Instruction insn) {
        if (insn.opcode == FOR_ITER) {
            quickeningStack.add(insn);
            return;
        }
        if (insn.target != null && insn.opcode.getNumberOfProducedStackItems(insn.arg, insn.followingArgs, true) > 0) {
            // TODO support control flow
            quickeningStack.clear();
            return;
        }
        int consumed = insn.opcode.getNumberOfConsumedStackItems(insn.arg, insn.followingArgs, false);
        int produced = insn.opcode.getNumberOfProducedStackItems(insn.arg, insn.followingArgs, false);
        byte canQuickenInputTypes = insn.opcode.canQuickenInputTypes();
        List<Instruction> inputs = null;
        if (consumed > 0) {
            if (canQuickenInputTypes != 0) {
                inputs = new ArrayList<>();
                for (int i = 0; i < consumed; i++) {
                    Instruction input = popQuickeningStack();
                    if (input == null) {
                        /*
                         * This happens when we cleared the stack after a jump or other
                         * not-yet-supported scenario
                         */
                        canQuickenInputTypes = 0;
                        break;
                    }
                    canQuickenInputTypes &= input.opcode.canQuickenOutputTypes();
                    inputs.add(input);
                }
            } else {
                for (int i = 0; i < consumed; i++) {
                    popQuickeningStack();
                }
            }
        }
        if (produced > 0) {
            if (produced > 1) {
                // TODO instructions that produce multiple values?
                quickeningStack.clear();
                return;
            }
            quickeningStack.add(insn);
        }
        if (canQuickenInputTypes != 0) {
            insn.quickeningGeneralizeList = inputs;
        }
        if (canQuickenInputTypes != 0 && inputs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                inputs.get(i).quickenOutput = canQuickenInputTypes;
            }
        }
    }

    Instruction popQuickeningStack() {
        if (quickeningStack.size() == 0) {
            return null;
        }
        return quickeningStack.remove(quickeningStack.size() - 1);
    }

    // helpers

    private void enterScope(String name, CompilationScope scopeType, SSTNode node) {
        enterScope(name, scopeType, node, 0, 0, 0, false, false);
    }

    private void enterScope(String name, CompilationScope scope, SSTNode node, ArgumentsTy args) {
        int argc, pargc, kwargc;
        boolean splat, kwSplat;
        if (args == null) {
            argc = pargc = kwargc = 0;
            splat = kwSplat = false;
        } else {
            argc = args.args == null ? 0 : args.args.length;
            pargc = args.posOnlyArgs == null ? 0 : args.posOnlyArgs.length;
            kwargc = args.kwOnlyArgs == null ? 0 : args.kwOnlyArgs.length;
            splat = args.varArg != null;
            kwSplat = args.kwArg != null;
        }
        enterScope(name, scope, node, argc, pargc, kwargc, splat, kwSplat);
    }

    private void enterScope(String name, CompilationScope scopeType, SSTNode node, int argc, int pargc, int kwargc,
                    boolean hasSplat, boolean hasKwSplat) {
        if (unit != null) {
            stack.add(unit);
        }
        unit = new CompilationUnit(scopeType, env.lookupScope(node), name, unit, stack.size(), argc, pargc, kwargc,
                        hasSplat, hasKwSplat, node.getSourceRange());
        nestingLevel++;
    }

    private void exitScope() {
        nestingLevel--;
        if (!stack.isEmpty()) {
            unit = stack.remove(stack.size() - 1);
        } else {
            unit = null;
        }
    }

    private void checkForbiddenName(String id, ExprContextTy context) {
        if (context == ExprContextTy.Store) {
            if (id.equals("__debug__")) {
                errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "cannot assign to __debug__");
            }
        }
        if (context == ExprContextTy.Del) {
            if (id.equals("__debug__")) {
                errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "cannot delete __debug__");
            }
        }
    }

    private boolean containsAnnotations(StmtTy[] stmts) {
        if (stmts == null) {
            return false;
        }
        for (StmtTy stmt : stmts) {
            if (containsAnnotations(stmt)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnnotations(StmtTy stmt) {
        if (stmt instanceof StmtTy.AnnAssign) {
            return true;
        } else if (stmt instanceof StmtTy.For) {
            return containsAnnotations(((StmtTy.For) stmt).body) || containsAnnotations(((StmtTy.For) stmt).orElse);
        } else if (stmt instanceof StmtTy.While) {
            return containsAnnotations(((StmtTy.While) stmt).body) || containsAnnotations(((StmtTy.While) stmt).orElse);
        } else if (stmt instanceof StmtTy.If) {
            return containsAnnotations(((StmtTy.If) stmt).body) || containsAnnotations(((StmtTy.If) stmt).orElse);
        } else if (stmt instanceof StmtTy.With) {
            return containsAnnotations(((StmtTy.With) stmt).body);
        } else if (stmt instanceof StmtTy.Try) {
            StmtTy.Try tryStmt = (StmtTy.Try) stmt;
            if (tryStmt.handlers != null) {
                for (ExceptHandlerTy h : tryStmt.handlers) {
                    if (containsAnnotations(((ExceptHandlerTy.ExceptHandler) h).body)) {
                        return true;
                    }
                }
            }
            return containsAnnotations(tryStmt.body) || containsAnnotations(tryStmt.finalBody) || containsAnnotations(tryStmt.orElse);
        }
        return false;
    }

    private Void addOp(OpCodes code) {
        addOp(code, 0, null, unit.currentLocation);
        return null;
    }

    private void addOp(OpCodes code, Block target) {
        addOp(code, target, null);
    }

    private void addConditionalJump(OpCodes code, Block target) {
        int profileIndex = unit.conditionProfileCount;
        unit.conditionProfileCount += 2;
        /*
         * Intentionally ignoring overflow in the conversion of the index. If the unit has more than
         * 2^16 conditionals it most likely wouldn't compile anyway, so there's not much harm if
         * there's some false sharing of profiles.
         */
        byte[] follwingArgs = new byte[2];
        ByteArraySupport.littleEndian().putShort(follwingArgs, 0, (short) profileIndex);
        addOp(code, target, follwingArgs);
    }

    private void addOp(OpCodes code, Block target, byte[] follwingArgs) {
        Block b = unit.currentBlock;
        Instruction insn = new Instruction(code, 0, follwingArgs, target, unit.currentLocation);
        b.instr.add(insn);
        pushOp(insn);
    }

    private Void addOp(OpCodes code, int arg) {
        addOp(code, arg, null, unit.currentLocation);
        return null;
    }

    private Void addOp(OpCodes code, int arg, byte[] followingArgs) {
        addOp(code, arg, followingArgs, unit.currentLocation);
        return null;
    }

    private void addOp(OpCodes code, int arg, byte[] followingArgs, SourceRange location) {
        Block b = unit.currentBlock;
        Instruction insn = new Instruction(code, arg, followingArgs, null, location);
        b.instr.add(insn);
        pushOp(insn);
    }

    private Void addOpName(OpCodes code, HashMap<String, Integer> dict, String name) {
        String mangled = ScopeEnvironment.mangle(unit.privateName, name);
        addOpObject(code, dict, mangled);
        return null;
    }

    private <T> void addOpObject(OpCodes code, HashMap<T, Integer> dict, T obj) {
        int arg = addObject(dict, obj);
        addOp(code, arg);
    }

    private void addDerefVariableOpcode(ExprContextTy ctx, int idx) {
        switch (ctx) {
            case Load:
                addOp(unit.scope.isClass() ? LOAD_CLASSDEREF : LOAD_DEREF, idx);
                break;
            case Store:
                addOp(STORE_DEREF, idx);
                break;
            case Del:
                addOp(DELETE_DEREF, idx);
                break;
        }
    }

    private void addFastVariableOpcode(ExprContextTy ctx, int idx) {
        switch (ctx) {
            case Load:
                addOp(LOAD_FAST, idx);
                break;
            case Store:
                addOp(STORE_FAST, idx);
                break;
            case Del:
                addOp(DELETE_FAST, idx);
                break;
        }
    }

    private void addGlobalVariableOpcode(ExprContextTy ctx, int idx) {
        switch (ctx) {
            case Load:
                addOp(LOAD_GLOBAL, idx);
                break;
            case Store:
                addOp(STORE_GLOBAL, idx);
                break;
            case Del:
                addOp(DELETE_GLOBAL, idx);
                break;
        }
    }

    private void addNameVariableOpcode(ExprContextTy ctx, int idx) {
        switch (ctx) {
            case Load:
                addOp(LOAD_NAME, idx);
                break;
            case Store:
                addOp(STORE_NAME, idx);
                break;
            case Del:
                addOp(DELETE_NAME, idx);
                break;
        }
    }

    private void addNameOp(String name, ExprContextTy ctx) {
        checkForbiddenName(name, ctx);

        String mangled = ScopeEnvironment.mangle(unit.privateName, name);
        EnumSet<Scope.DefUse> uses = unit.scope.getUseOfName(mangled);

        if (uses != null) {
            if (uses.contains(Scope.DefUse.Free)) {
                addDerefVariableOpcode(ctx, addObject(unit.freevars, mangled));
                return;
            } else if (uses.contains(Scope.DefUse.Cell)) {
                addDerefVariableOpcode(ctx, addObject(unit.cellvars, mangled));
                return;
            } else if (uses.contains(Scope.DefUse.Local)) {
                if (unit.scope.isFunction()) {
                    addFastVariableOpcode(ctx, addObject(unit.varnames, mangled));
                    return;
                }
            } else if (uses.contains(Scope.DefUse.GlobalImplicit)) {
                if (unit.scope.isFunction()) {
                    addGlobalVariableOpcode(ctx, addObject(unit.names, mangled));
                    return;
                }
            } else if (uses.contains(Scope.DefUse.GlobalExplicit)) {
                addGlobalVariableOpcode(ctx, addObject(unit.names, mangled));
                return;
            }
        }
        addNameVariableOpcode(ctx, addObject(unit.names, mangled));
    }

    private <T> int addObject(HashMap<T, Integer> dict, T o) {
        Integer v = dict.get(o);
        if (v == null) {
            v = dict.size();
            dict.put(o, v);
        }
        return v;
    }

    private TruffleString getDocstring(StmtTy[] body) {
        if (optimizationLevel >= 2) {
            return null;
        }
        if (body != null && body.length > 0) {
            StmtTy stmt = body[0];
            if (stmt instanceof StmtTy.Expr) {
                ExprTy expr = ((StmtTy.Expr) stmt).value;
                if (expr instanceof ExprTy.Constant) {
                    ConstantValue value = ((ExprTy.Constant) expr).value;
                    if (value.kind == ConstantValue.Kind.RAW) {
                        return value.getRaw(TruffleString.class);
                    }
                }
            }
        }
        return null;
    }

    private SourceRange setLocation(SourceRange location) {
        SourceRange savedLocation = unit.currentLocation;
        unit.currentLocation = location;
        return savedLocation;
    }

    private SourceRange setLocation(SSTNode node) {
        return setLocation(node.getSourceRange());
    }

    private class Collector {
        protected final int typeBits;
        protected final int stackItemsPerItem;
        protected int stackItems = 0;
        protected boolean collectionOnStack = false;

        public Collector(int typeBits) {
            this.typeBits = typeBits;
            stackItemsPerItem = typeBits == CollectionBits.KIND_DICT ? 2 : 1;
        }

        public Collector(int typeBits, int stackItems) {
            this(typeBits);
            this.stackItems = stackItems;
        }

        public void appendItem() {
            stackItems += stackItemsPerItem;
            if (stackItems + stackItemsPerItem > CollectionBits.KIND_MASK) {
                doFlushStack();
            }
        }

        public void flushStackIfNecessary() {
            if (stackItems > 0) {
                doFlushStack();
            }
        }

        protected void doFlushStack() {
            assert stackItems <= CollectionBits.KIND_MASK;
            if (collectionOnStack) {
                addOp(COLLECTION_ADD_STACK, typeBits | stackItems);
            } else {
                addOp(COLLECTION_FROM_STACK, typeBits | stackItems);
            }
            collectionOnStack = true;
            stackItems = 0;
        }

        public void appendCollection() {
            assert stackItems == 0;
            if (collectionOnStack) {
                addOp(COLLECTION_ADD_COLLECTION, typeBits);
            } else {
                addOp(COLLECTION_FROM_COLLECTION, typeBits);
            }
            collectionOnStack = true;
        }

        public void finishCollection() {
            if (stackItems > 0 || !collectionOnStack) {
                doFlushStack();
            }
        }

        public boolean isEmpty() {
            return stackItems == 0 && !collectionOnStack;
        }
    }

    private class KwargsMergingDictCollector extends Collector {
        public KwargsMergingDictCollector(OpCodes callOp) {
            super(CollectionBits.KIND_DICT);
            /*
             * We're making assumptions about the stack layout below this instruction to obtain the
             * callable for error reporting. If we ever add more keywords call instructions, we need
             * to adjust the implementation to be able to get the callable.
             */
            assert callOp == CALL_FUNCTION_KW;
        }

        @Override
        protected void doFlushStack() {
            addOp(COLLECTION_FROM_STACK, typeBits | stackItems);
            if (collectionOnStack) {
                addOp(KWARGS_DICT_MERGE);
            }
            collectionOnStack = true;
            stackItems = 0;
        }

        @Override
        public void appendCollection() {
            assert stackItems == 0;
            if (collectionOnStack) {
                addOp(KWARGS_DICT_MERGE);
            } else {
                addOp(COLLECTION_FROM_COLLECTION, typeBits);
            }
            collectionOnStack = true;
        }
    }

    private void collectIntoArray(ExprTy[] nodes, int bits, int alreadyOnStack) {
        Collector collector = new Collector(bits, alreadyOnStack);
        if (nodes != null) {
            for (ExprTy e : nodes) {
                if (e instanceof ExprTy.Starred) {
                    // splat
                    collector.flushStackIfNecessary();
                    ((ExprTy.Starred) e).value.accept(this);
                    collector.appendCollection();
                } else {
                    e.accept(this);
                    collector.appendItem();
                }
            }
        }
        collector.finishCollection();
    }

    private void collectIntoArray(ExprTy[] nodes, int bits) {
        collectIntoArray(nodes, bits, 0);
    }

    private void collectIntoDict(ExprTy[] keys, ExprTy[] values) {
        Collector collector = new Collector(CollectionBits.KIND_DICT);
        if (keys != null) {
            assert keys.length == values.length;
            for (int i = 0; i < keys.length; i++) {
                ExprTy key = keys[i];
                ExprTy value = values[i];
                if (key == null) {
                    // splat
                    collector.flushStackIfNecessary();
                    value.accept(this);
                    collector.appendCollection();
                } else {
                    key.accept(this);
                    value.accept(this);
                    collector.appendItem();
                }
            }
        }
        collector.finishCollection();
    }

    private void validateKeywords(KeywordTy[] keywords) {
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].arg != null) {
                checkForbiddenName(keywords[i].arg, ExprContextTy.Store);
                for (int j = i + 1; j < keywords.length; j++) {
                    if (keywords[i].arg.equals(keywords[j].arg)) {
                        // TODO: cbasca 3.10 error message "keyword argument repeated: " +
                        // keywords[i].arg
                        errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "keyword argument repeated");
                    }
                }
            }
        }
    }

    private void collectKeywords(KeywordTy[] keywords, OpCodes callOp) {
        validateKeywords(keywords);
        boolean hasSplat = false;
        for (KeywordTy k : keywords) {
            if (k.arg == null) {
                hasSplat = true;
                break;
            }
        }
        if (!hasSplat) {
            Collector collector = new Collector(CollectionBits.KIND_KWORDS);
            for (KeywordTy k : keywords) {
                k.accept(this);
                collector.appendItem();
            }
            collector.finishCollection();
        } else if (keywords.length == 1) {
            // Just one splat, no need for merging
            keywords[0].value.accept(this);
            addOp(COLLECTION_FROM_COLLECTION, CollectionBits.KIND_KWORDS);
        } else {
            /*
             * We need to emit bytecodes for proper keywords merging with checking for duplicate
             * keys. We accumulate them in an intermediate dict.
             */
            Collector collector = new KwargsMergingDictCollector(callOp);
            for (KeywordTy k : keywords) {
                if (k.arg == null) {
                    // splat
                    collector.flushStackIfNecessary();
                    k.value.accept(this);
                    collector.appendCollection();
                } else {
                    addOp(LOAD_STRING, addObject(unit.constants, toTruffleStringUncached(k.arg)));
                    k.value.accept(this);
                    collector.appendItem();
                }
            }
            collector.finishCollection();
            addOp(COLLECTION_FROM_COLLECTION, CollectionBits.KIND_KWORDS);
        }
    }

    private void makeClosure(CodeUnit code, int makeFunctionFlags) {
        int flags = makeFunctionFlags;
        if (code.freevars.length > 0) {
            // add the closure
            for (TruffleString tfv : code.freevars) {
                String fv = tfv.toJavaStringUncached();
                // special case for class scopes
                int arg;
                if (unit.scopeType == CompilationScope.Class && "__class__".equals(fv) || unit.scope.getUseOfName(fv).contains(Scope.DefUse.Cell)) {
                    arg = unit.cellvars.get(fv);
                } else {
                    arg = unit.freevars.get(fv);
                }
                addOp(LOAD_CLOSURE, arg);
            }
            addOp(CLOSURE_FROM_STACK, code.freevars.length);
            flags |= OpCodes.MakeFunctionFlags.HAS_CLOSURE;
        }
        addObject(unit.constants, code.qualname);
        addOp(MAKE_FUNCTION, addObject(unit.constants, code), new byte[]{(byte) flags});
    }

    // visiting

    private void visitBody(StmtTy[] stmts, boolean returnValue) {
        if (stmts != null) {
            if (unit.scope.isModule() && stmts.length > 0) {
                /*
                 * Set current line number to the line number of first statement. This way line
                 * number for SETUP_ANNOTATIONS will always coincide with the line number of first
                 * "real" statement in module. If body is empty, then lineno will be set later in
                 * assemble.
                 */
                setLocation(stmts[0]);
            }
            if (containsAnnotations(stmts)) {
                addOp(SETUP_ANNOTATIONS);
            }
            if (stmts.length > 0) {
                int i = 0;
                TruffleString docstring = getDocstring(stmts);
                if (docstring != null) {
                    i++;
                    StmtTy.Expr stmt = (StmtTy.Expr) stmts[0];
                    stmt.value.accept(this);
                    addNameOp("__doc__", ExprContextTy.Store);
                }
                for (; i < stmts.length - 1; i++) {
                    stmts[i].accept(this);
                }
                /*
                 * To support interop eval we need to return the value of the last statement even if
                 * we're in file mode. Also used when parsing with arguments.
                 */
                StmtTy lastStatement = stmts[stmts.length - 1];
                if (returnValue && lastStatement instanceof StmtTy.Expr) {
                    ((StmtTy.Expr) lastStatement).value.accept(this);
                    addOp(RETURN_VALUE);
                    return;
                } else {
                    lastStatement.accept(this);
                }
            }
        }
        if (returnValue) {
            addOp(LOAD_NONE);
            addOp(RETURN_VALUE);
        }
    }

    @Override
    public Void visit(AliasTy node) {
        addOp(LOAD_BYTE, 0);
        addOp(LOAD_CONST, addObject(unit.constants, PythonUtils.EMPTY_TRUFFLESTRING_ARRAY));
        addOpName(IMPORT_NAME, unit.names, node.name);
        if (node.asName != null) {
            int dotIdx = node.name.indexOf('.');
            if (dotIdx >= 0) {
                while (true) {
                    int pos = dotIdx + 1;
                    dotIdx = node.name.indexOf('.', pos);
                    int end = dotIdx >= 0 ? dotIdx : node.name.length();
                    String attr = node.name.substring(pos, end);
                    addOpObject(IMPORT_FROM, unit.names, attr);
                    if (dotIdx < 0) {
                        break;
                    }
                    addOp(ROT_TWO);
                    addOp(POP_TOP);
                }
                addNameOp(node.asName, ExprContextTy.Store);
                addOp(POP_TOP);
            } else {
                addNameOp(node.asName, ExprContextTy.Store);
            }
        } else {
            int dotIdx = node.name.indexOf('.');
            if (dotIdx >= 0) {
                addNameOp(node.name.substring(0, dotIdx), ExprContextTy.Store);
            } else {
                addNameOp(node.name, ExprContextTy.Store);
            }
        }
        return null;
    }

    @Override
    public Void visit(ArgTy node) {
        throw new IllegalStateException("Should not be visited");
    }

    @Override
    public Void visit(ArgumentsTy node) {
        throw new IllegalStateException("Should not be visited");
    }

    @Override
    public Void visit(ComprehensionTy node) {
        throw new IllegalStateException("Should not be visited");
    }

    @Override
    public Void visit(ExprTy.Attribute node) {
        SourceRange savedLocation = setLocation(node);
        try {
            node.value.accept(this);
            switch (node.context) {
                case Store:
                    checkForbiddenName(node.attr, node.context);
                    return addOpName(STORE_ATTR, unit.names, node.attr);
                case Del:
                    return addOpName(DELETE_ATTR, unit.names, node.attr);
                case Load:
                default:
                    return addOpName(LOAD_ATTR, unit.names, node.attr);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Await node) {
        SourceRange savedLocation = setLocation(node);
        // TODO if !IS_TOP_LEVEL_AWAIT
        if (!unit.scope.isFunction()) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'await' outside function");
        }
        if (unit.scopeType != CompilationScope.AsyncFunction && unit.scopeType != CompilationScope.Comprehension) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'await' outside async function");
        }
        try {
            node.value.accept(this);
            addOp(GET_AWAITABLE);
            addOp(LOAD_NONE);
            addYieldFrom();
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.BinOp node) {
        SourceRange savedLocation = setLocation(node);
        try {
            node.left.accept(this);
            node.right.accept(this);
            switch (node.op) {
                case Add:
                    addOp(BINARY_OP, BinaryOps.ADD.ordinal());
                    break;
                case Sub:
                    addOp(BINARY_OP, BinaryOps.SUB.ordinal());
                    break;
                case Mult:
                    addOp(BINARY_OP, BinaryOps.MUL.ordinal());
                    break;
                case MatMult:
                    addOp(BINARY_OP, BinaryOps.MATMUL.ordinal());
                    break;
                case Div:
                    addOp(BINARY_OP, BinaryOps.TRUEDIV.ordinal());
                    break;
                case Mod:
                    addOp(BINARY_OP, BinaryOps.MOD.ordinal());
                    break;
                case Pow:
                    addOp(BINARY_OP, BinaryOps.POW.ordinal());
                    break;
                case LShift:
                    addOp(BINARY_OP, BinaryOps.LSHIFT.ordinal());
                    break;
                case RShift:
                    addOp(BINARY_OP, BinaryOps.RSHIFT.ordinal());
                    break;
                case BitOr:
                    addOp(BINARY_OP, BinaryOps.OR.ordinal());
                    break;
                case BitXor:
                    addOp(BINARY_OP, BinaryOps.XOR.ordinal());
                    break;
                case BitAnd:
                    addOp(BINARY_OP, BinaryOps.AND.ordinal());
                    break;
                case FloorDiv:
                    addOp(BINARY_OP, BinaryOps.FLOORDIV.ordinal());
                    break;
                default:
                    throw new IllegalStateException("Unknown binary operation " + node.op);
            }
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.BoolOp node) {
        SourceRange savedLocation = setLocation(node);
        try {
            Block end = new Block();
            ExprTy[] values = node.values;
            OpCodes op;
            if (node.op == BoolOpTy.And) {
                op = JUMP_IF_FALSE_OR_POP;
            } else {
                op = JUMP_IF_TRUE_OR_POP;
            }
            for (int i = 0; i < values.length - 1; i++) {
                ExprTy v = values[i];
                v.accept(this);
                addConditionalJump(op, end);
            }
            values[values.length - 1].accept(this);
            unit.useNextBlock(end);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    private static boolean isAttributeLoad(ExprTy node) {
        return node instanceof ExprTy.Attribute && ((ExprTy.Attribute) node).context == ExprContextTy.Load;
    }

    private static boolean hasOnlyPlainArgs(ExprTy[] args, KeywordTy[] keywords) {
        if (keywords.length > 0) {
            return false;
        }
        for (ExprTy arg : args) {
            if (arg instanceof ExprTy.Starred) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Void visit(ExprTy.Call node) {
        SourceRange savedLocation = setLocation(node);
        try {
            // n.b.: we do things completely different from python for calls
            OpCodes op = CALL_FUNCTION_VARARGS;
            int opArg = 0;
            boolean shortCall;
            final ExprTy func = node.func;
            final ExprTy[] args = node.args;
            final KeywordTy[] keywords = node.keywords;

            if (isAttributeLoad(func) && keywords.length == 0) {
                ((ExprTy.Attribute) func).value.accept(this);
                op = CALL_METHOD_VARARGS;
                String mangled = ScopeEnvironment.mangle(unit.privateName, ((ExprTy.Attribute) func).attr);
                opArg = addObject(unit.names, mangled);
                shortCall = args.length <= 3;
            } else {
                func.accept(this);
                shortCall = args.length <= 4;
            }

            if (hasOnlyPlainArgs(args, keywords) && shortCall) {
                if (op == CALL_METHOD_VARARGS) {
                    addOp(LOAD_METHOD, opArg);
                    op = CALL_METHOD;
                } else {
                    op = CALL_FUNCTION;
                }
                opArg = args.length;
                // fast calls without extra arguments array
                visitSequence(args);
                return addOp(op, opArg);
            } else {
                return callHelper(op, opArg, 0, args, keywords);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    private Void callHelper(OpCodes op, int opArg, int alreadyOnStack, ExprTy[] args, KeywordTy[] keywords) {
        collectIntoArray(args, CollectionBits.KIND_OBJECT, op == CALL_METHOD_VARARGS ? 1 + alreadyOnStack : alreadyOnStack);
        if (keywords.length > 0) {
            assert op == CALL_FUNCTION_VARARGS;
            collectKeywords(keywords, CALL_FUNCTION_KW);
            return addOp(CALL_FUNCTION_KW);
        } else {
            return addOp(op, opArg);
        }
    }

    private void addCompareOp(CmpOpTy op) {
        switch (op) {
            case Eq:
                addOp(BINARY_OP, BinaryOps.EQ.ordinal());
                break;
            case NotEq:
                addOp(BINARY_OP, BinaryOps.NE.ordinal());
                break;
            case Lt:
                addOp(BINARY_OP, BinaryOps.LT.ordinal());
                break;
            case LtE:
                addOp(BINARY_OP, BinaryOps.LE.ordinal());
                break;
            case Gt:
                addOp(BINARY_OP, BinaryOps.GT.ordinal());
                break;
            case GtE:
                addOp(BINARY_OP, BinaryOps.GE.ordinal());
                break;
            case Is:
                addOp(BINARY_OP, BinaryOps.IS.ordinal());
                break;
            case IsNot:
                addOp(BINARY_OP, BinaryOps.IS.ordinal());
                addOp(UNARY_OP, UnaryOps.NOT.ordinal());
                break;
            case In:
                addOp(BINARY_OP, BinaryOps.IN.ordinal());
                break;
            case NotIn:
                addOp(BINARY_OP, BinaryOps.IN.ordinal());
                addOp(UNARY_OP, UnaryOps.NOT.ordinal());
                break;
            default:
                throw new IllegalStateException("Unknown comparison operation " + op);
        }
    }

    @Override
    public Void visit(ExprTy.Compare node) {
        SourceRange savedLocation = setLocation(node);
        try {
            node.left.accept(this);
            if (node.comparators.length == 1) {
                visitSequence(node.comparators);
                addCompareOp(node.ops[0]);
            } else {
                Block cleanup = new Block();
                int i;
                for (i = 0; i < node.comparators.length - 1; i++) {
                    node.comparators[i].accept(this);
                    addOp(DUP_TOP);
                    addOp(ROT_THREE);
                    addCompareOp(node.ops[i]);
                    addConditionalJump(JUMP_IF_FALSE_OR_POP, cleanup);
                }
                node.comparators[i].accept(this);
                addCompareOp(node.ops[i]);
                Block end = new Block();
                addOp(JUMP_FORWARD, end);
                unit.useNextBlock(cleanup);
                addOp(ROT_TWO);
                addOp(POP_TOP);
                unit.useNextBlock(end);
            }
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Constant node) {
        SourceRange savedLocation = setLocation(node);
        try {
            switch (node.value.kind) {
                case ARBITRARY_PYTHON_OBJECT:
                    // TODO GR-40165: the code object being compiled must not be shared
                    return addOp(LOAD_CONST, addObject(unit.constants, node.value.getArbitraryPythonObject()));
                case NONE:
                    return addOp(LOAD_NONE);
                case ELLIPSIS:
                    return addOp(LOAD_ELLIPSIS);
                case BOOLEAN:
                    return addOp(node.value.getBoolean() ? LOAD_TRUE : LOAD_FALSE);
                case LONG:
                    return addLoadNumber(node.value.getLong());
                case DOUBLE:
                    return addOp(LOAD_DOUBLE, addObject(unit.primitiveConstants, Double.doubleToRawLongBits(node.value.getDouble())));
                case COMPLEX:
                    return addOp(LOAD_COMPLEX, addObject(unit.constants, node.value.getComplex()));
                case BIGINTEGER:
                    return addOp(LOAD_BIGINT, addObject(unit.constants, node.value.getBigInteger()));
                case RAW:
                    return addOp(LOAD_STRING, addObject(unit.constants, node.value.getRaw(TruffleString.class)));
                case BYTES:
                    return addOp(LOAD_BYTES, addObject(unit.constants, node.value.getBytes()));
                default:
                    throw new IllegalStateException("Unknown constant kind " + node.value.kind);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    private Void addLoadNumber(long value) {
        if (value == (byte) value) {
            return addOp(LOAD_BYTE, (byte) value);
        } else if (value == (int) value) {
            return addOp(LOAD_INT, addObject(unit.primitiveConstants, value));
        } else {
            return addOp(LOAD_LONG, addObject(unit.primitiveConstants, value));
        }
    }

    @Override
    public Void visit(ExprTy.Dict node) {
        SourceRange savedLocation = setLocation(node);
        try {
            collectIntoDict(node.keys, node.values);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.DictComp node) {
        return visitComprehension(node, "<dictcomp>", node.generators, node.key, node.value, ComprehensionType.DICT);
    }

    @Override
    public Void visit(ExprTy.FormattedValue node) {
        SourceRange savedLocation = setLocation(node);
        try {
            node.value.accept(this);
            int oparg;
            switch (node.conversion) {
                case 's':
                    oparg = FormatOptions.FVC_STR;
                    break;
                case 'r':
                    oparg = FormatOptions.FVC_REPR;
                    break;
                case 'a':
                    oparg = FormatOptions.FVC_ASCII;
                    break;
                case -1:
                    oparg = FormatOptions.FVC_NONE;
                    break;
                default:
                    // TODO GR-40162 raise SystemError
                    throw new IllegalStateException("Unknown format conversion");
            }
            if (node.formatSpec != null) {
                node.formatSpec.accept(this);
                oparg |= FormatOptions.FVS_HAVE_SPEC;
            }
            addOp(FORMAT_VALUE, oparg);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.GeneratorExp node) {
        return visitComprehension(node, "<genexpr>", node.generators, node.element, null, ComprehensionType.GENEXPR);
    }

    @Override
    public Void visit(ExprTy.IfExp node) {
        SourceRange savedLocation = setLocation(node);
        try {
            Block end = new Block();
            Block next = new Block();
            jumpIf(node.test, next, false);
            node.body.accept(this);
            addOp(JUMP_FORWARD, end);
            unit.useNextBlock(next);
            node.orElse.accept(this);
            unit.useNextBlock(end);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.JoinedStr node) {
        SourceRange savedLocation = setLocation(node);
        try {
            // TODO add optimized op for small chains
            addOp(LOAD_STRING, addObject(unit.constants, T_EMPTY_STRING));
            addOpName(LOAD_METHOD, unit.names, "join");
            collectIntoArray(node.values, CollectionBits.KIND_LIST);
            addOp(CALL_METHOD, 1);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Lambda node) {
        SourceRange savedLocation = setLocation(node);
        try {
            checkForbiddenArgs(node.args);
            int makeFunctionFlags = collectDefaults(node.args);
            enterScope("<lambda>", CompilationScope.Lambda, node, node.args);
            /* Make None the first constant, so the lambda can't have a docstring. */
            addObject(unit.constants, PNone.NONE);
            CodeUnit code;
            try {
                node.body.accept(this);
                addOp(RETURN_VALUE);
                code = unit.assemble();
            } finally {
                exitScope();
            }
            makeClosure(code, makeFunctionFlags);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    private Void unpackInto(ExprTy[] elements) {
        boolean unpack = false;
        for (int i = 0; i < elements.length; i++) {
            ExprTy e = elements[i];
            if (e instanceof ExprTy.Starred) {
                if (unpack) {
                    errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "multiple starred expressions in assignment");
                }
                unpack = true;
                int n = elements.length;
                int countAfter = n - i - 1;
                if (countAfter != (byte) countAfter) {
                    errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "too many expressions in star-unpacking assignment");
                }
                addOp(UNPACK_EX, i, new byte[]{(byte) countAfter});
            }
        }
        if (!unpack) {
            addOp(UNPACK_SEQUENCE, elements.length);
        }
        for (ExprTy e : elements) {
            if (e instanceof ExprTy.Starred) {
                ((ExprTy.Starred) e).value.accept(this);
            } else if (e != null) {
                e.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(ExprTy.List node) {
        SourceRange savedLocation = setLocation(node);
        try {
            switch (node.context) {
                case Store:
                    return unpackInto(node.elements);
                case Load:
                    boolean emittedConstant = tryCollectConstantCollection(node.elements, CollectionBits.KIND_LIST);
                    if (emittedConstant) {
                        return null;
                    }
                    collectIntoArray(node.elements, CollectionBits.KIND_LIST);
                    return null;
                case Del:
                default:
                    return visitSequence(node.elements);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    private enum ComprehensionType {
        LIST(CollectionBits.KIND_LIST),
        SET(CollectionBits.KIND_SET),
        DICT(CollectionBits.KIND_DICT),
        GENEXPR(-1);

        public final int typeBits;

        ComprehensionType(int typeBits) {
            this.typeBits = typeBits;
        }
    }

    @Override
    public Void visit(ExprTy.ListComp node) {
        return visitComprehension(node, "<listcomp>", node.generators, node.element, null, ComprehensionType.LIST);
    }

    private Void visitComprehension(ExprTy node, String name, ComprehensionTy[] generators, ExprTy element, ExprTy value, ComprehensionType type) {
        /*
         * Create an inner anonymous function to run the comprehension. It takes the outermost
         * iterator as an argument and returns the accumulated sequence
         */
        SourceRange savedLocation = setLocation(node);
        try {
            enterScope(name, CompilationScope.Comprehension, node, 1, 0, 0, false, false);
            if (type != ComprehensionType.GENEXPR) {
                // The result accumulator, empty at the beginning
                addOp(COLLECTION_FROM_STACK, type.typeBits);
            }
            visitComprehensionGenerator(generators, 0, element, value, type);
            if (type != ComprehensionType.GENEXPR) {
                addOp(RETURN_VALUE);
            }
            CodeUnit code = unit.assemble();
            exitScope();
            makeClosure(code, 0);
            generators[0].iter.accept(this);
            addOp(GET_ITER);
            addOp(CALL_FUNCTION, 1);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    private void visitComprehensionGenerator(ComprehensionTy[] generators, int i, ExprTy element, ExprTy value, ComprehensionType type) {
        ComprehensionTy gen = generators[i];
        if (i == 0) {
            /* The iterator is the function argument for the outermost generator */
            addOp(LOAD_FAST, 0);
        } else {
            /* Create the iterator for nested iteration */
            gen.iter.accept(this);
            addOp(GET_ITER);
        }
        Block start = new Block();
        Block ifCleanup = new Block();
        Block anchor = new Block();
        unit.useNextBlock(start);
        addOp(FOR_ITER, anchor);
        gen.target.accept(this);
        for (ExprTy ifExpr : gen.ifs) {
            jumpIf(ifExpr, ifCleanup, false);
        }
        if (i + 1 < generators.length) {
            visitComprehensionGenerator(generators, i + 1, element, value, type);
        }
        if (i == generators.length - 1) {
            /* The last generator produces the resulting element to be appended/yielded */
            element.accept(this);
            int collectionStackDepth = generators.length + 1;
            if (value != null) {
                value.accept(this);
                collectionStackDepth++;
            }
            if (type == ComprehensionType.GENEXPR) {
                addOp(YIELD_VALUE);
                addOp(RESUME_YIELD);
                addOp(POP_TOP);
            } else {
                /*
                 * There is an iterator for every generator on the stack. We need to append to the
                 * collection that's below them
                 */
                if (collectionStackDepth > CollectionBits.KIND_MASK) {
                    errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "too many levels of nested comprehensions");
                }
                addOp(ADD_TO_COLLECTION, collectionStackDepth | type.typeBits);
            }
        }
        unit.useNextBlock(ifCleanup);
        addOp(JUMP_BACKWARD, start);
        unit.useNextBlock(anchor);
    }

    @Override
    public Void visit(ExprTy.Name node) {
        SourceRange savedLocation = setLocation(node);
        try {
            addNameOp(node.id, node.context);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.NamedExpr node) {
        SourceRange savedLocation = setLocation(node);
        try {
            node.value.accept(this);
            addOp(DUP_TOP);
            node.target.accept(this);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Set node) {
        SourceRange savedLocation = setLocation(node);
        try {
            collectIntoArray(node.elements, CollectionBits.KIND_SET);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.SetComp node) {
        return visitComprehension(node, "<setcomp>", node.generators, node.element, null, ComprehensionType.SET);
    }

    @Override
    public Void visit(ExprTy.Slice node) {
        SourceRange savedLocation = setLocation(node);
        try {
            int n = 2;
            if (node.lower != null) {
                node.lower.accept(this);
            } else {
                addOp(LOAD_NONE);
            }
            if (node.upper != null) {
                node.upper.accept(this);
            } else {
                addOp(LOAD_NONE);
            }
            if (node.step != null) {
                node.step.accept(this);
                n++;
            }
            addOp(BUILD_SLICE, n);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Starred node) {
        // Valid occurrences are handled by other visitors
        if (node.context == ExprContextTy.Store) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "starred assignment target must be in a list or tuple");
        } else {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "can't use starred expression here");
        }
        return null;
    }

    @Override
    public Void visit(ExprTy.Subscript node) {
        SourceRange savedLocation = setLocation(node);
        try {
            node.value.accept(this);
            node.slice.accept(this);
            switch (node.context) {
                case Load:
                    return addOp(BINARY_SUBSCR);
                case Store:
                    return addOp(STORE_SUBSCR);
                case Del:
                default:
                    return addOp(DELETE_SUBSCR);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Tuple node) {
        SourceRange savedLocation = setLocation(node);
        try {
            switch (node.context) {
                case Store:
                    return unpackInto(node.elements);
                case Load:
                    /*
                     * We don't have mutation operations for tuples, so if we cannot construct the
                     * tuple within a single instruction, we construct a list and convert it to a
                     * tuple.
                     */
                    boolean useList = false;
                    if (node.elements != null) {
                        if (node.elements.length > CollectionBits.KIND_MASK) {
                            useList = true;
                        } else {
                            for (ExprTy e : node.elements) {
                                if (e instanceof ExprTy.Starred) {
                                    useList = true;
                                    break;
                                }
                            }
                        }
                    }
                    boolean emittedConstant = tryCollectConstantCollection(node.elements, CollectionBits.KIND_TUPLE);
                    if (emittedConstant) {
                        return null;
                    }
                    if (!useList) {
                        collectIntoArray(node.elements, CollectionBits.KIND_TUPLE);
                    } else {
                        collectIntoArray(node.elements, CollectionBits.KIND_LIST);
                        addOp(TUPLE_FROM_LIST);
                    }
                    return null;
                case Del:
                default:
                    return visitSequence(node.elements);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    private boolean tryCollectConstantCollection(ExprTy[] elements, int collectionKind) {
        /*
         * We try to store the whole tuple as a Java array constant when all the elements are
         * constant and context-independent.
         */
        if (elements == null || elements.length == 0) {
            return false;
        }

        int constantType = -1;
        List<Object> constants = new ArrayList<>();

        for (ExprTy e : elements) {
            if (e instanceof ExprTy.Constant) {
                ExprTy.Constant c = (ExprTy.Constant) e;
                if (c.value.kind == ConstantValue.Kind.BOOLEAN) {
                    constantType = determineConstantType(constantType, CollectionBits.ELEMENT_BOOLEAN);
                    constants.add(c.value.getBoolean());
                } else if (c.value.kind == ConstantValue.Kind.LONG) {
                    long val = c.value.getLong();
                    if (val == (int) val) {
                        constantType = determineConstantType(constantType, CollectionBits.ELEMENT_INT);
                    } else {
                        constantType = determineConstantType(constantType, CollectionBits.ELEMENT_LONG);
                    }
                    constants.add(val);
                } else if (c.value.kind == ConstantValue.Kind.DOUBLE) {
                    constantType = determineConstantType(constantType, CollectionBits.ELEMENT_DOUBLE);
                    constants.add(c.value.getDouble());
                } else if (c.value.kind == ConstantValue.Kind.RAW) {
                    constantType = determineConstantType(constantType, CollectionBits.ELEMENT_OBJECT);
                    constants.add(c.value.getRaw(TruffleString.class));
                } else if (c.value.kind == ConstantValue.Kind.NONE) {
                    constantType = determineConstantType(constantType, CollectionBits.ELEMENT_OBJECT);
                    constants.add(PNone.NONE);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        Object newConstant = null;
        switch (constantType) {
            case CollectionBits.ELEMENT_OBJECT:
                newConstant = constants.toArray(new Object[0]);
                break;
            case CollectionBits.ELEMENT_INT: {
                int[] a = new int[constants.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = (int) (long) constants.get(i);
                }
                newConstant = a;
                break;
            }
            case CollectionBits.ELEMENT_LONG: {
                long[] a = new long[constants.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = (long) constants.get(i);
                }
                newConstant = a;
                break;
            }
            case CollectionBits.ELEMENT_BOOLEAN: {
                boolean[] a = new boolean[constants.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = (boolean) constants.get(i);
                }
                newConstant = a;
                break;
            }
            case CollectionBits.ELEMENT_DOUBLE: {
                double[] a = new double[constants.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = (double) constants.get(i);
                }
                newConstant = a;
                break;
            }
        }
        addOp(LOAD_CONST_COLLECTION, addObject(unit.constants, newConstant), new byte[]{(byte) (constantType | collectionKind)});
        return true;
    }

    int determineConstantType(int existing, int type) {
        if (existing == -1 || existing == type) {
            return type;
        }
        if (existing == CollectionBits.ELEMENT_LONG && type == CollectionBits.ELEMENT_INT || existing == CollectionBits.ELEMENT_INT && type == CollectionBits.ELEMENT_LONG) {
            return CollectionBits.ELEMENT_LONG;
        }
        return CollectionBits.ELEMENT_OBJECT;
    }

    @Override
    public Void visit(ExprTy.UnaryOp node) {
        SourceRange savedLocation = setLocation(node);
        try {
            // Basic constant folding for unary negation
            if (node.op == UnaryOpTy.USub && node.operand instanceof ExprTy.Constant) {
                ExprTy.Constant c = (ExprTy.Constant) node.operand;
                if (c.value.kind == ConstantValue.Kind.LONG) {
                    long v = c.value.getLong();
                    if (v != Long.MIN_VALUE) {
                        return visit(new ExprTy.Constant(ConstantValue.ofLong(-v), null, c.getSourceRange()));
                    } else {
                        return visit(new ExprTy.Constant(ConstantValue.ofBigInteger(BigInteger.valueOf(v).negate()), null, c.getSourceRange()));
                    }
                } else if (c.value.kind == ConstantValue.Kind.DOUBLE) {
                    return visit(new ExprTy.Constant(ConstantValue.ofDouble(-c.value.getDouble()), null, c.getSourceRange()));
                } else if (c.value.kind == ConstantValue.Kind.BIGINTEGER) {
                    return visit(new ExprTy.Constant(ConstantValue.ofBigInteger(c.value.getBigInteger().negate()), null, c.getSourceRange()));
                }
            }
            node.operand.accept(this);
            switch (node.op) {
                case UAdd:
                    return addOp(UNARY_OP, UnaryOps.POSITIVE.ordinal());
                case Invert:
                    return addOp(UNARY_OP, UnaryOps.INVERT.ordinal());
                case Not:
                    return addOp(UNARY_OP, UnaryOps.NOT.ordinal());
                case USub:
                    return addOp(UNARY_OP, UnaryOps.NEGATIVE.ordinal());
                default:
                    throw new IllegalStateException("Unknown unary operation " + node.op);
            }
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.Yield node) {
        SourceRange savedLocation = setLocation(node);
        try {
            if (!unit.scope.isFunction()) {
                errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'yield' outside function");
            }
            if (node.value != null) {
                node.value.accept(this);
            } else {
                addOp(LOAD_NONE);
            }
            addOp(YIELD_VALUE);
            addOp(RESUME_YIELD);
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    @Override
    public Void visit(ExprTy.YieldFrom node) {
        SourceRange savedLocation = setLocation(node);
        try {
            if (!unit.scope.isFunction()) {
                errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'yield' outside function");
            }
            if (unit.scopeType == CompilationScope.AsyncFunction) {
                errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'yield from' inside async function");
            }
            node.value.accept(this);
            // TODO GET_YIELD_FROM_ITER
            addOp(GET_ITER);
            addOp(LOAD_NONE);
            addYieldFrom();
            return null;
        } finally {
            setLocation(savedLocation);
        }
    }

    private void addYieldFrom() {
        Block start = new Block();
        Block yield = new Block();
        Block resume = new Block();
        Block exit = new Block();
        Block exceptionHandler = new Block();
        unit.useNextBlock(start);
        addOp(SEND, exit);
        unit.useNextBlock(yield);
        addOp(YIELD_VALUE);
        unit.pushBlock(new BlockInfo.TryExcept(resume, exceptionHandler));
        unit.useNextBlock(resume);
        addOp(RESUME_YIELD);
        addOp(JUMP_BACKWARD, start);
        unit.popBlock();
        unit.useNextBlock(exceptionHandler);
        addOp(THROW, exit);
        addOp(JUMP_BACKWARD, yield);
        unit.useNextBlock(exit);
    }

    @Override
    public Void visit(KeywordTy node) {
        node.value.accept(this);
        setLocation(node);
        return addOp(MAKE_KEYWORD, addObject(unit.constants, toTruffleStringUncached(node.arg)));
    }

    @Override
    public Void visit(ModTy.Expression node) {
        node.body.accept(this);
        addOp(RETURN_VALUE);
        return null;
    }

    @Override
    public Void visit(ModTy.FunctionType node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Void visit(ModTy.Interactive node) {
        if (containsAnnotations(node.body)) {
            addOp(SETUP_ANNOTATIONS);
        }
        interactive = true;
        visitSequence(node.body);
        addOp(LOAD_NONE);
        addOp(RETURN_VALUE);
        return null;
    }

    @Override
    public Void visit(ModTy.Module node) {
        visitBody(node.body, true);
        return null;
    }

    @Override
    public Void visit(TypeIgnoreTy.TypeIgnore node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Void visit(StmtTy.AnnAssign node) {
        setLocation(node);
        if (node.value != null) {
            node.value.accept(this);
            node.target.accept(this);
        }
        if (node.target instanceof ExprTy.Name) {
            String name = ((ExprTy.Name) node.target).id;
            checkForbiddenName(name, ExprContextTy.Store);
            /* If we have a simple name in a module or class, store annotation. */
            if (node.isSimple &&
                            (unit.scopeType == CompilationScope.Module || unit.scopeType == CompilationScope.Class)) {
                boolean futureAnnotations = futureFeatures.contains(FutureFeature.ANNOTATIONS);
                if (futureAnnotations) {
                    visitAnnexpr(node.annotation);
                } else {
                    node.annotation.accept(this);
                }
                addNameOp("__annotations__", ExprContextTy.Load);
                String mangled = ScopeEnvironment.mangle(unit.privateName, name);
                addOp(LOAD_STRING, addObject(unit.constants, toTruffleStringUncached(mangled)));
                addOp(STORE_SUBSCR);
            }
        } else if (node.target instanceof ExprTy.Attribute) {
            ExprTy.Attribute attr = (ExprTy.Attribute) node.target;
            checkForbiddenName(attr.attr, ExprContextTy.Store);
            if (attr.value != null) {
                checkAnnExpr(attr.value);
            }
        } else if (node.target instanceof ExprTy.Subscript) {
            ExprTy.Subscript subscript = (ExprTy.Subscript) node.target;
            if (subscript.value != null) {
                checkAnnExpr(subscript.value);
            }
            checkAnnSubscr(subscript.slice);
        } else {
            errorCallback.onError(ErrorType.Syntax, node.getSourceRange(), "invalid node type for annotated assignment");
        }
        if (!node.isSimple) {
            boolean futureAnnotations = futureFeatures.contains(FutureFeature.ANNOTATIONS);
            /*
             * Annotations of complex targets does not produce anything under annotations future.
             * Annotations are only evaluated in a module or class.
             */
            if (!futureAnnotations && (unit.scopeType == CompilationScope.Module || unit.scopeType == CompilationScope.Class)) {
                checkAnnExpr(node.annotation);
            }
        }
        return null;
    }

    private void checkAnnExpr(ExprTy expr) {
        expr.accept(this);
        addOp(POP_TOP);
    }

    private void checkAnnSubscr(ExprTy expr) {
        if (expr instanceof ExprTy.Slice) {
            ExprTy.Slice slice = (ExprTy.Slice) expr;
            if (slice.lower != null) {
                checkAnnExpr(slice.lower);
            }
            if (slice.upper != null) {
                checkAnnExpr(slice.upper);
            }
            if (slice.step != null) {
                checkAnnExpr(slice.step);
            }
        } else if (expr instanceof ExprTy.Tuple) {
            ExprTy.Tuple tuple = (ExprTy.Tuple) expr;
            for (int i = 0; i < tuple.elements.length; i++) {
                checkAnnSubscr(tuple.elements[i]);
            }
        } else {
            checkAnnExpr(expr);
        }
    }

    @Override
    public Void visit(StmtTy.Assert node) {
        // TODO warn when test is a tuple
        if (optimizationLevel > 0) {
            return null;
        }
        setLocation(node);
        Block end = new Block();
        jumpIf(node.test, end, true);
        addOp(LOAD_ASSERTION_ERROR);
        if (node.msg != null) {
            node.msg.accept(this);
            addOp(CALL_FUNCTION, 1);
        }
        addOp(RAISE_VARARGS, 1);
        unit.useNextBlock(end);
        return null;
    }

    @Override
    public Void visit(StmtTy.Assign node) {
        setLocation(node);
        node.value.accept(this);
        for (int i = 0; i < node.targets.length; i++) {
            if (i != node.targets.length - 1) {
                addOp(DUP_TOP);
            }
            node.targets[i].accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StmtTy.AsyncFor node) {
        setLocation(node);
        return emitNotImplemented("async for");
    }

    // TODO temporary helper so that stuff that's not implemented can compile into stubs
    private Void emitNotImplemented(String what) {
        addGlobalVariableOpcode(ExprContextTy.Load, addObject(unit.names, "NotImplementedError"));
        addOp(LOAD_STRING, addObject(unit.constants, toTruffleStringUncached(what)));
        addOp(CALL_FUNCTION, 1);
        addOp(RAISE_VARARGS, 1);
        return null;
    }

    @Override
    public Void visit(StmtTy.AsyncFunctionDef node) {
        return visitFunctionDef(node, node.name, node.args, node.body, node.decoratorList, node.returns, true);
    }

    @Override
    public Void visit(StmtTy.AsyncWith node) {
        setLocation(node);
        return emitNotImplemented("async with");
    }

    @Override
    public Void visit(StmtTy.AugAssign node) {
        ExprTy target = node.target;
        SourceRange savedLocation = setLocation(target);
        if (target instanceof ExprTy.Attribute) {
            ExprTy.Attribute attr = (ExprTy.Attribute) target;
            attr.value.accept(this);
            addOp(DUP_TOP);
            addOpName(LOAD_ATTR, unit.names, attr.attr);
        } else if (target instanceof ExprTy.Subscript) {
            ExprTy.Subscript subscript = (ExprTy.Subscript) target;
            subscript.value.accept(this);
            addOp(DUP_TOP);
            subscript.slice.accept(this);
            addOp(DUP_TOP);
            addOp(ROT_THREE);
            addOp(BINARY_SUBSCR);
        } else if (target instanceof ExprTy.Name) {
            ExprTy.Name name = (ExprTy.Name) target;
            addNameOp(name.id, ExprContextTy.Load);
        } else {
            // TODO this raises SystemError under CPython
            throw new IllegalArgumentException("invalid node type for augmented assignment");
        }
        setLocation(node);
        node.value.accept(this);
        switch (node.op) {
            case Add:
                addOp(BINARY_OP, BinaryOps.INPLACE_ADD.ordinal());
                break;
            case Sub:
                addOp(BINARY_OP, BinaryOps.INPLACE_SUB.ordinal());
                break;
            case Mult:
                addOp(BINARY_OP, BinaryOps.INPLACE_MUL.ordinal());
                break;
            case MatMult:
                addOp(BINARY_OP, BinaryOps.INPLACE_MATMUL.ordinal());
                break;
            case Div:
                addOp(BINARY_OP, BinaryOps.INPLACE_TRUEDIV.ordinal());
                break;
            case Mod:
                addOp(BINARY_OP, BinaryOps.INPLACE_MOD.ordinal());
                break;
            case Pow:
                addOp(BINARY_OP, BinaryOps.INPLACE_POW.ordinal());
                break;
            case LShift:
                addOp(BINARY_OP, BinaryOps.INPLACE_LSHIFT.ordinal());
                break;
            case RShift:
                addOp(BINARY_OP, BinaryOps.INPLACE_RSHIFT.ordinal());
                break;
            case BitOr:
                addOp(BINARY_OP, BinaryOps.INPLACE_OR.ordinal());
                break;
            case BitXor:
                addOp(BINARY_OP, BinaryOps.INPLACE_XOR.ordinal());
                break;
            case BitAnd:
                addOp(BINARY_OP, BinaryOps.INPLACE_AND.ordinal());
                break;
            case FloorDiv:
                addOp(BINARY_OP, BinaryOps.INPLACE_FLOORDIV.ordinal());
                break;
            default:
                throw new IllegalStateException("Unknown binary inplace operation " + node.op);
        }
        setLocation(target);
        if (target instanceof ExprTy.Attribute) {
            ExprTy.Attribute attr = (ExprTy.Attribute) target;
            addOp(ROT_TWO);
            addOpName(STORE_ATTR, unit.names, attr.attr);
        } else if (target instanceof ExprTy.Subscript) {
            addOp(ROT_THREE);
            addOp(STORE_SUBSCR);
        } else {
            ExprTy.Name name = (ExprTy.Name) target;
            addNameOp(name.id, ExprContextTy.Store);
        }
        setLocation(savedLocation);
        return null;
    }

    @Override
    public Void visit(StmtTy.ClassDef node) {
        setLocation(node);
        visitSequence(node.decoratorList);

        enterScope(node.name, CompilationScope.Class, node, 0, 0, 0, false, false);
        addNameOp("__name__", ExprContextTy.Load);
        addNameOp("__module__", ExprContextTy.Store);
        addOp(LOAD_STRING, addObject(unit.constants, toTruffleStringUncached(unit.qualName)));
        addNameOp("__qualname__", ExprContextTy.Store);

        visitBody(node.body, false);

        if (unit.scope.needsClassClosure()) {
            int idx = unit.cellvars.get("__class__");
            addOp(LOAD_CLOSURE, idx);
            addOp(DUP_TOP);
            addNameOp("__classcell__", ExprContextTy.Store);
        } else {
            addOp(LOAD_NONE);
        }
        addOp(RETURN_VALUE);
        CodeUnit co = unit.assemble();
        exitScope();

        addOp(LOAD_BUILD_CLASS);
        makeClosure(co, 0);
        addOp(LOAD_STRING, addObject(unit.constants, toTruffleStringUncached(node.name)));

        callHelper(CALL_FUNCTION_VARARGS, 0, 2, node.bases, node.keywords);

        if (node.decoratorList != null) {
            ExprTy[] decoratorList = node.decoratorList;
            for (int i = 0; i < decoratorList.length; i++) {
                addOp(CALL_FUNCTION, 1);
            }
        }

        addNameOp(node.name, ExprContextTy.Store);

        return null;
    }

    @Override
    public Void visit(StmtTy.Delete node) {
        setLocation(node);
        visitSequence(node.targets);
        return null;
    }

    @Override
    public Void visit(StmtTy.Expr node) {
        setLocation(node);
        if (interactive && nestingLevel <= 1) {
            node.value.accept(this);
            addOp(PRINT_EXPR);
        } else if (!(node.value instanceof ExprTy.Constant)) {
            node.value.accept(this);
            addOp(POP_TOP);
        }
        return null;
    }

    @Override
    public Void visit(StmtTy.For node) {
        setLocation(node);
        Block head = new Block();
        Block body = new Block();
        Block end = new Block();
        Block orelse = node.orElse != null ? new Block() : end;

        node.iter.accept(this);
        addOp(GET_ITER);

        unit.useNextBlock(head);
        addOp(FOR_ITER, orelse);

        unit.useNextBlock(body);
        unit.pushBlock(new BlockInfo.For(head, end));
        try {
            node.target.accept(this);
            visitSequence(node.body);
            addOp(JUMP_BACKWARD, head);
        } finally {
            unit.popBlock();
        }

        if (node.orElse != null) {
            unit.useNextBlock(orelse);
            visitSequence(node.orElse);
        }

        unit.useNextBlock(end);
        return null;
    }

    @Override
    public Void visit(StmtTy.FunctionDef node) {
        return visitFunctionDef(node, node.name, node.args, node.body, node.decoratorList, node.returns, false);
    }

    private Void visitFunctionDef(StmtTy node, String name, ArgumentsTy args, StmtTy[] body, ExprTy[] decoratorList, ExprTy returns, boolean isAsync) {
        setLocation(node);
        checkForbiddenArgs(args);

        // visit decorators
        visitSequence(decoratorList);

        // visit defaults outside the function scope
        int makeFunctionFlags = collectDefaults(args);

        boolean hasAnnotations = visitAnnotations(args, returns);
        if (hasAnnotations) {
            makeFunctionFlags |= OpCodes.MakeFunctionFlags.HAS_ANNOTATIONS;
        }

        CompilationScope scopeType = isAsync ? CompilationScope.AsyncFunction : CompilationScope.Function;
        enterScope(name, scopeType, node, args);

        CodeUnit code;
        try {
            TruffleString docString = getDocstring(body);
            addObject(unit.constants, docString == null ? PNone.NONE : docString);
            visitSequence(body);
            code = unit.assemble();
        } finally {
            exitScope();
        }

        makeClosure(code, makeFunctionFlags);

        if (decoratorList != null) {
            for (int i = 0; i < decoratorList.length; i++) {
                addOp(CALL_FUNCTION, 1);
            }
        }

        addNameOp(name, ExprContextTy.Store);
        return null;
    }

    private boolean visitAnnotations(ArgumentsTy args, ExprTy returns) {
        Collector collector = new Collector(CollectionBits.KIND_DICT);
        if (args != null) {
            visitArgAnnotations(collector, args.args);
            visitArgAnnotations(collector, args.posOnlyArgs);
            if (args.varArg != null) {
                visitArgAnnotation(collector, args.varArg.arg, args.varArg.annotation);
            }
            visitArgAnnotations(collector, args.kwOnlyArgs);
            if (args.kwArg != null) {
                visitArgAnnotation(collector, args.kwArg.arg, args.kwArg.annotation);
            }
        }
        visitArgAnnotation(collector, "return", returns);
        if (collector.isEmpty()) {
            return false;
        }
        collector.finishCollection();
        return true;
    }

    private void visitArgAnnotations(Collector collector, ArgTy[] args) {
        for (int i = 0; i < args.length; i++) {
            visitArgAnnotation(collector, args[i].arg, args[i].annotation);
        }
    }

    private void visitArgAnnotation(Collector collector, String name, ExprTy annotation) {
        if (annotation != null) {
            String mangled = ScopeEnvironment.mangle(unit.privateName, name);
            addOp(LOAD_STRING, addObject(unit.constants, toTruffleStringUncached(mangled)));
            if (futureFeatures.contains(FutureFeature.ANNOTATIONS)) {
                visitAnnexpr(annotation);
            } else {
                if (annotation instanceof ExprTy.Starred) {
                    // *args: *Ts (where Ts is a TypeVarTuple).
                    // Do [annotation_value] = [*Ts].
                    ((ExprTy.Starred) annotation).value.accept(this);
                    addOp(UNPACK_SEQUENCE, 1);
                } else {
                    annotation.accept(this);
                }
            }
            collector.appendItem();
        }
    }

    private void visitAnnexpr(ExprTy annotation) {
        addOp(LOAD_STRING, addObject(unit.constants, Unparser.unparse(annotation)));
    }

    private int collectDefaults(ArgumentsTy args) {
        int makeFunctionFlags = 0;
        if (args != null) {
            if (args.defaults != null && args.defaults.length > 0) {
                collectIntoArray(args.defaults, CollectionBits.KIND_OBJECT);
                makeFunctionFlags |= OpCodes.MakeFunctionFlags.HAS_DEFAULTS;
            }
            if (args.kwDefaults != null && args.kwDefaults.length > 0) {
                ArrayList<KeywordTy> defs = new ArrayList<>();
                for (int i = 0; i < args.kwOnlyArgs.length; i++) {
                    ArgTy arg = args.kwOnlyArgs[i];
                    ExprTy def = args.kwDefaults[i];
                    if (def != null) {
                        String mangled = ScopeEnvironment.mangle(unit.privateName, arg.arg);
                        defs.add(new KeywordTy(mangled, def, arg.getSourceRange()));
                    }
                }
                collectKeywords(defs.toArray(KeywordTy[]::new), null);
                makeFunctionFlags |= OpCodes.MakeFunctionFlags.HAS_KWONLY_DEFAULTS;
            }
        }
        return makeFunctionFlags;
    }

    private void checkForbiddenArgs(ArgumentsTy args) {
        if (args != null) {
            if (args.posOnlyArgs != null) {
                for (ArgTy arg : args.posOnlyArgs) {
                    checkForbiddenName(arg.arg, ExprContextTy.Store);
                }
            }
            if (args.args != null) {
                for (ArgTy arg : args.args) {
                    checkForbiddenName(arg.arg, ExprContextTy.Store);
                }
            }
            if (args.kwOnlyArgs != null) {
                for (ArgTy arg : args.kwOnlyArgs) {
                    checkForbiddenName(arg.arg, ExprContextTy.Store);
                }
            }
            if (args.varArg != null) {
                checkForbiddenName(args.varArg.arg, ExprContextTy.Store);
            }
            if (args.kwArg != null) {
                checkForbiddenName(args.kwArg.arg, ExprContextTy.Store);
            }
        }
    }

    @Override
    public Void visit(StmtTy.Global node) {
        setLocation(node);
        return null;
    }

    @Override
    public Void visit(StmtTy.If node) {
        setLocation(node);
        Block then = new Block();
        Block end = new Block();
        Block alt = node.orElse != null && node.orElse.length > 0 ? new Block() : end;
        jumpIf(node.test, alt, false);
        unit.useNextBlock(then);
        visitSequence(node.body);
        if (alt != end) {
            addOp(JUMP_FORWARD, end);
            unit.useNextBlock(alt);
            visitSequence(node.orElse);
        }
        unit.useNextBlock(end);
        return null;
    }

    private void jumpIf(ExprTy test, Block next, boolean jumpIfTrue) {
        // TODO Optimize for various test types, such as short-circuit operators
        // See compiler_jump_if in CPython
        test.accept(this);
        if (jumpIfTrue) {
            addConditionalJump(POP_AND_JUMP_IF_TRUE, next);
        } else {
            addConditionalJump(POP_AND_JUMP_IF_FALSE, next);
        }
    }

    @Override
    public Void visit(StmtTy.Import node) {
        setLocation(node);
        return visitSequence(node.names);
    }

    @Override
    public Void visit(StmtTy.ImportFrom node) {
        setLocation(node);
        addLoadNumber(node.level);
        TruffleString[] names = new TruffleString[node.names.length];
        for (int i = 0; i < node.names.length; i++) {
            names[i] = toTruffleStringUncached(node.names[i].name);
        }
        if (node.getSourceRange().startLine > futureLineno && "__future__".equals(node.module)) {
            errorCallback.onError(ErrorType.Syntax, node.getSourceRange(), "from __future__ imports must occur at the beginning of the file");
        }
        String moduleName = node.module != null ? node.module : "";
        if ("*".equals(node.names[0].name)) {
            addOpName(IMPORT_STAR, unit.names, moduleName);
        } else {
            addOp(LOAD_CONST, addObject(unit.constants, names));
            addOpName(IMPORT_NAME, unit.names, moduleName);
            for (AliasTy alias : node.names) {
                addOpName(IMPORT_FROM, unit.names, alias.name);
                String storeName = alias.asName != null ? alias.asName : alias.name;
                addNameOp(storeName, ExprContextTy.Store);

            }
            addOp(POP_TOP);
        }
        return null;
    }

    @Override
    public Void visit(StmtTy.Match node) {
        setLocation(node);
        return emitNotImplemented("match");
    }

    @Override
    public Void visit(MatchCaseTy node) {
        return emitNotImplemented("case");
    }

    @Override
    public Void visit(PatternTy.MatchAs node) {
        return emitNotImplemented("match as");
    }

    @Override
    public Void visit(PatternTy.MatchClass node) {
        return emitNotImplemented("match class");
    }

    @Override
    public Void visit(PatternTy.MatchMapping node) {
        return emitNotImplemented("match mapping");
    }

    @Override
    public Void visit(PatternTy.MatchOr node) {
        return emitNotImplemented("match or");
    }

    @Override
    public Void visit(PatternTy.MatchSequence node) {
        return emitNotImplemented("match sequence");
    }

    @Override
    public Void visit(PatternTy.MatchSingleton node) {
        return emitNotImplemented("match singleton");
    }

    @Override
    public Void visit(PatternTy.MatchStar node) {
        return emitNotImplemented("match star");
    }

    @Override
    public Void visit(PatternTy.MatchValue node) {
        return emitNotImplemented("match value");
    }

    @Override
    public Void visit(StmtTy.Nonlocal node) {
        setLocation(node);
        return null;
    }

    @Override
    public Void visit(StmtTy.Raise node) {
        setLocation(node);
        int argc = 0;
        if (node.exc != null) {
            argc++;
            node.exc.accept(this);
            if (node.cause != null) {
                argc++;
                node.cause.accept(this);
            }
        }
        addOp(RAISE_VARARGS, argc);
        unit.useNextBlock(new Block());
        return null;
    }

    @Override
    public Void visit(StmtTy.Return node) {
        setLocation(node);
        if (!unit.scope.isFunction()) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'return' outside function");
        }
        if (node.value != null && unit.scope.isGenerator() && unit.scope.isCoroutine()) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'return' with value in async generator");
        }
        if (node.value == null) {
            unwindBlockStack(UnwindType.RETURN_CONST);
            addOp(LOAD_NONE);
        } else if (node.value instanceof ExprTy.Constant) {
            unwindBlockStack(UnwindType.RETURN_CONST);
            node.value.accept(this);
        } else {
            node.value.accept(this);
            unwindBlockStack(UnwindType.RETURN_VALUE);
        }
        return addOp(RETURN_VALUE);
    }

    private void jumpToFinally(Block finalBlock, Block end) {
        if (finalBlock != null) {
            addOp(JUMP_FORWARD, finalBlock);
        } else {
            addOp(JUMP_FORWARD, end);
        }
    }

    @Override
    public Void visit(StmtTy.Try node) {
        setLocation(node);
        Block tryBody = new Block();
        Block end = new Block();
        boolean hasFinally = node.finalBody != null;
        boolean hasHandlers = node.handlers != null && node.handlers.length > 0;
        assert hasFinally || hasHandlers;
        Block exceptionHandlerBlock = null;
        Block finallyBlockNormal = null;
        Block finallyBlockExcept = null;
        Block finallyBlockExceptWithSavedExc = null;
        Block elseBlock = node.orElse != null ? new Block() : null;
        if (hasFinally) {
            finallyBlockNormal = new Block();
            finallyBlockExcept = new Block();
            finallyBlockExceptWithSavedExc = new Block();
            unit.pushBlock(new BlockInfo.TryFinally(tryBody, finallyBlockExcept, node.finalBody));
        }
        if (hasHandlers) {
            exceptionHandlerBlock = new Block();
            unit.pushBlock(new BlockInfo.TryExcept(tryBody, exceptionHandlerBlock));
        }
        // try block
        unit.useNextBlock(tryBody);
        visitSequence(node.body);
        if (hasHandlers) {
            unit.popBlock();
        }
        if (elseBlock != null) {
            addOp(JUMP_FORWARD, elseBlock);
        } else {
            jumpToFinally(finallyBlockNormal, end);
        }

        // except clauses
        if (hasHandlers) {
            unit.useNextBlock(exceptionHandlerBlock);
            /* This puts saved exception under the current exception */
            addOp(PUSH_EXC_INFO);
            /* The stack is now [*, savedException, currentException] */
            boolean hasBareExcept = false;
            Block nextHandler = new Block();
            /*
             * We need to save and restore the outer exception state. In order to restore it even in
             * case of an exception, we need an internal finally handler for this.
             */
            Block commonCleanupHandler = new Block();
            unit.pushBlock(new BlockInfo.ExceptHandler(nextHandler, commonCleanupHandler));
            /*
             * We use this offset to unwind the exception from the except block, we don't need it on
             * the stack
             */
            commonCleanupHandler.unwindOffset = -1;
            for (int i = 0; i < node.handlers.length; i++) {
                ExceptHandlerTy.ExceptHandler handler = ((ExceptHandlerTy.ExceptHandler) node.handlers[i]);
                setLocation(handler);
                if (hasBareExcept) {
                    errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "default 'except:' must be last");
                }
                unit.useNextBlock(nextHandler);

                if (i < node.handlers.length - 1) {
                    nextHandler = new Block();
                } else {
                    if (hasFinally) {
                        // Keep the saved state, it's the same
                        nextHandler = finallyBlockExceptWithSavedExc;
                    } else {
                        nextHandler = commonCleanupHandler;
                    }
                }

                Block bindingCleanerExcept = null;
                String bindingName = handler.name;
                if (handler.type != null) {
                    handler.type.accept(this);
                    addConditionalJump(MATCH_EXC_OR_JUMP, nextHandler);
                    if (bindingName != null) {
                        addOp(UNWRAP_EXC);
                        addNameOp(bindingName, ExprContextTy.Store);
                        Block handlerWithBinding = new Block();
                        bindingCleanerExcept = new Block();
                        unit.pushBlock(new BlockInfo.HandlerBindingCleanup(handlerWithBinding, bindingCleanerExcept, bindingName));
                        unit.useNextBlock(handlerWithBinding);
                    } else {
                        addOp(POP_TOP);
                    }
                } else {
                    hasBareExcept = true;
                    addOp(POP_TOP);
                }
                visitSequence(handler.body);
                if (bindingName != null) {
                    unit.popBlock();
                    unit.useNextBlock(new Block());
                    addOp(LOAD_NONE);
                    addNameOp(bindingName, ExprContextTy.Store);
                    addNameOp(bindingName, ExprContextTy.Del);
                }
                addOp(POP_EXCEPT);
                jumpToFinally(finallyBlockNormal, end);
                if (bindingName != null) {
                    unit.useNextBlock(bindingCleanerExcept);
                    addOp(LOAD_NONE);
                    addNameOp(bindingName, ExprContextTy.Store);
                    addNameOp(bindingName, ExprContextTy.Del);
                    cleanupOnExceptionInHandler(hasFinally, finallyBlockExcept);
                }
            }
            unit.popBlock();
            unit.useNextBlock(commonCleanupHandler);
            cleanupOnExceptionInHandler(hasFinally, finallyBlockExcept);
        }

        if (elseBlock != null) {
            unit.useNextBlock(elseBlock);
            visitSequence(node.orElse);
            jumpToFinally(finallyBlockNormal, end);
        }

        /*
         * We emit two copies of the finally block, one that is executed when there is no exception
         * and one that is executed when an exception occurs. The latter needs to deal with the
         * exception state. We start with the latter.
         */
        if (hasFinally) {
            unit.popBlock();
            unit.useNextBlock(finallyBlockExcept);
            /* This puts saved exception under the current exception */
            addOp(PUSH_EXC_INFO);
            Block cleanupHandler = new Block();
            cleanupHandler.unwindOffset = -1;
            unit.pushBlock(new BlockInfo.FinallyHandler(finallyBlockExceptWithSavedExc, cleanupHandler));
            /* The stack is [*, savedException, currentException] */
            unit.useNextBlock(finallyBlockExceptWithSavedExc);
            visitSequence(node.finalBody);
            unit.popBlock();
            unit.useNextBlock(cleanupHandler);
            addOp(END_EXC_HANDLER);

            /* Now emit the finally for the no-exception case */
            unit.useNextBlock(finallyBlockNormal);
            visitSequence(node.finalBody);
        }

        unit.useNextBlock(end);

        return null;
    }

    private void cleanupOnExceptionInHandler(boolean hasFinally, Block finallyBlockExcept) {
        if (hasFinally) {
            addOp(ROT_TWO);
            /*
             * POP the saved exception state, the finally block needs to push a new one. The saved
             * state will be the same, but PUSH_EXC_INFO also updates the current exception info
             * which is now different.
             */
            addOp(POP_EXCEPT);
            addOp(JUMP_FORWARD, finallyBlockExcept);
        } else {
            /*
             * We can also reach this code by falling off the except handlers if no types matched
             * and there is no finally
             */
            addOp(END_EXC_HANDLER);
        }
    }

    @Override
    public Void visit(ExceptHandlerTy.ExceptHandler node) {
        throw new IllegalStateException("should not reach here");
    }

    @Override
    public Void visit(StmtTy.While node) {
        setLocation(node);
        Block test = new Block();
        Block body = new Block();
        Block end = new Block();
        Block orelse = node.orElse != null ? new Block() : end;
        unit.useNextBlock(test);
        jumpIf(node.test, orelse, false);
        unit.useNextBlock(body);
        unit.pushBlock(new BlockInfo.While(test, end));
        try {
            visitSequence(node.body);
            addOp(JUMP_BACKWARD, test);
        } finally {
            unit.popBlock();
        }
        if (node.orElse != null) {
            unit.useNextBlock(orelse);
            visitSequence(node.orElse);
        }
        unit.useNextBlock(end);
        return null;
    }

    @Override
    public Void visit(StmtTy.With node) {
        setLocation(node);
        visitWith(node, 0);
        unit.useNextBlock(new Block());
        return null;
    }

    private void visitWith(StmtTy.With node, int itemIndex) {
        Block body = new Block();
        Block handler = new Block();

        WithItemTy item = node.items[itemIndex];
        item.contextExpr.accept(this);
        addOp(SETUP_WITH);
        unit.pushBlock(new BlockInfo.With(body, handler, node));

        unit.useNextBlock(body);
        /*
         * Unwind one more stack item than it normally would to get rid of the context manager that
         * is not needed in the finally block
         */
        handler.unwindOffset = -1;
        if (item.optionalVars != null) {
            item.optionalVars.accept(this);
        } else {
            addOp(POP_TOP);
        }
        if (itemIndex < node.items.length - 1) {
            visitWith(node, itemIndex + 1);
        } else {
            visitSequence(node.body);
        }
        addOp(LOAD_NONE);
        unit.popBlock();

        unit.useNextBlock(handler);
        setLocation(node);
        addOp(EXIT_WITH);
    }

    @Override
    public Void visit(WithItemTy node) {
        throw new IllegalStateException("should not reach here");
    }

    private enum UnwindType {
        BREAK,
        CONTINUE,
        RETURN_VALUE,
        RETURN_CONST,
    }

    private BlockInfo.Loop unwindBlockStack(UnwindType type) {
        final BlockInfo savedInfo = unit.blockInfo;
        try {
            BlockInfo info = unit.blockInfo;
            while (info != null) {
                unit.blockInfo = info.outer;
                if (info instanceof BlockInfo.For) {
                    if (type == UnwindType.CONTINUE) {
                        return (BlockInfo.Loop) info;
                    }
                    if (type == UnwindType.RETURN_VALUE) {
                        addOp(ROT_TWO);
                    }
                    addOp(POP_TOP);
                    if (type == UnwindType.BREAK) {
                        return (BlockInfo.Loop) info;
                    }
                } else if (info instanceof BlockInfo.While) {
                    if (type == UnwindType.BREAK || type == UnwindType.CONTINUE) {
                        return (BlockInfo.Loop) info;
                    }
                } else if (info instanceof BlockInfo.With) {
                    unit.useNextBlock(new Block());
                    BlockInfo.With with = (BlockInfo.With) info;
                    setLocation(with.node);
                    if (type == UnwindType.RETURN_VALUE) {
                        addOp(ROT_THREE);
                    }
                    addOp(LOAD_NONE);
                    addOp(EXIT_WITH);
                } else if (info instanceof BlockInfo.TryFinally) {
                    unit.useNextBlock(new Block());
                    if (type == UnwindType.RETURN_VALUE) {
                        /*
                         * If we're returning, the finally block may "cancel" the return using
                         * break/continue. In that case, we need to pop the return value.
                         */
                        unit.pushBlock(new BlockInfo.PopValue());
                    }
                    visitSequence(((BlockInfo.TryFinally) info).body);
                } else if (info instanceof BlockInfo.ExceptHandler) {
                    if (type == UnwindType.RETURN_VALUE) {
                        addOp(ROT_TWO);
                    }
                    addOp(POP_EXCEPT);
                } else if (info instanceof BlockInfo.HandlerBindingCleanup) {
                    String bindingName = ((BlockInfo.HandlerBindingCleanup) info).bindingName;
                    if (bindingName != null) {
                        addOp(LOAD_NONE);
                        addNameOp(bindingName, ExprContextTy.Store);
                        addNameOp(bindingName, ExprContextTy.Del);
                    }
                } else if (info instanceof BlockInfo.FinallyHandler) {
                    if (type == UnwindType.RETURN_VALUE) {
                        addOp(ROT_THREE);
                    }
                    addOp(POP_EXCEPT);
                    addOp(POP_TOP);
                } else if (info instanceof BlockInfo.PopValue) {
                    if (type == UnwindType.RETURN_VALUE) {
                        addOp(ROT_TWO);
                    }
                    addOp(POP_TOP);
                }
                info = info.outer;
            }
        } finally {
            unit.blockInfo = savedInfo;
        }
        return null;
    }

    @Override
    public Void visit(StmtTy.Break node) {
        setLocation(node);
        setLocation(node);
        BlockInfo.Loop info = unwindBlockStack(UnwindType.BREAK);
        if (info == null) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'break' outside loop");
        }
        addOp(JUMP_FORWARD, info.after);
        return null;
    }

    @Override
    public Void visit(StmtTy.Continue node) {
        setLocation(node);
        BlockInfo.Loop info = unwindBlockStack(UnwindType.CONTINUE);
        if (info == null) {
            errorCallback.onError(ErrorType.Syntax, unit.currentLocation, "'continue' not properly in loop");
        }
        addOp(JUMP_BACKWARD, info.start);
        return null;
    }

    @Override
    public Void visit(StmtTy.Pass aThis) {
        return null;
    }

    public static Parser createParser(String src, ErrorCallback errorCb, InputType inputType, boolean interactiveTerminal) {
        NodeFactory nodeFactory = new NodeFactoryImp();
        PythonStringFactoryImpl stringFactory = new PythonStringFactoryImpl();
        FExprParser fexpParser = new FExprParser() {
            @Override
            public ExprTy parse(String code, SourceRange sourceRange) {
                // TODO use sourceRange.startXXX to adjust the locations of the expression nodes
                return (ExprTy) new Parser(new ParserTokenizer(errorCb, code, InputType.FSTRING, false), nodeFactory, this, stringFactory, errorCb, InputType.FSTRING).parse();
            }
        };
        return new Parser(new ParserTokenizer(errorCb, src, inputType, interactiveTerminal), nodeFactory, fexpParser, stringFactory, errorCb, inputType);
    }
}