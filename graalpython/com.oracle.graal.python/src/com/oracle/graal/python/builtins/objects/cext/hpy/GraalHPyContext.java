/*
 * Copyright (c) 2019, 2023, Oracle and/or its affiliates. All rights reserved.
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
// skip GIL
package com.oracle.graal.python.builtins.objects.cext.hpy;

import static com.oracle.graal.python.util.PythonUtils.EMPTY_TRUFFLESTRING_ARRAY;
import static com.oracle.graal.python.util.PythonUtils.tsArray;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.PNotImplemented;
import com.oracle.graal.python.builtins.objects.cext.capi.CApiContext;
import com.oracle.graal.python.builtins.objects.cext.common.CExtContext;
import com.oracle.graal.python.builtins.objects.cext.common.HandleStack;
import com.oracle.graal.python.builtins.objects.cext.common.LoadCExtException.ApiInitException;
import com.oracle.graal.python.builtins.objects.cext.common.LoadCExtException.ImportException;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodes.HPyGetNativeSpacePointerNode;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodes.PCallHPyFunction;
import com.oracle.graal.python.builtins.objects.cext.hpy.GraalHPyNodesFactory.PCallHPyFunctionNodeGen;
import com.oracle.graal.python.builtins.objects.cext.hpy.HPyExternalFunctionNodes.HPyCheckFunctionResultNode;
import com.oracle.graal.python.builtins.objects.cext.hpy.jni.GraalHPyJNIContext;
import com.oracle.graal.python.builtins.objects.cext.hpy.llvm.GraalHPyLLVMContext;
import com.oracle.graal.python.builtins.objects.dict.PDict;
import com.oracle.graal.python.builtins.objects.ellipsis.PEllipsis;
import com.oracle.graal.python.builtins.objects.exception.PBaseException;
import com.oracle.graal.python.builtins.objects.frame.PFrame;
import com.oracle.graal.python.builtins.objects.function.PArguments;
import com.oracle.graal.python.builtins.objects.function.Signature;
import com.oracle.graal.python.builtins.objects.module.PythonModule;
import com.oracle.graal.python.builtins.objects.object.PythonObject;
import com.oracle.graal.python.builtins.objects.str.StringUtils;
import com.oracle.graal.python.nodes.ErrorMessages;
import com.oracle.graal.python.nodes.PGuards;
import com.oracle.graal.python.nodes.PRootNode;
import com.oracle.graal.python.nodes.call.CallTargetInvokeNode;
import com.oracle.graal.python.nodes.call.GenericInvokeNode;
import com.oracle.graal.python.runtime.AsyncHandler;
import com.oracle.graal.python.runtime.GilNode;
import com.oracle.graal.python.runtime.PythonContext;
import com.oracle.graal.python.runtime.PythonOptions;
import com.oracle.graal.python.runtime.PythonOptions.HPyBackendMode;
import com.oracle.graal.python.runtime.exception.PException;
import com.oracle.graal.python.runtime.exception.PythonThreadKillException;
import com.oracle.graal.python.util.PythonUtils;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;

public final class GraalHPyContext extends CExtContext {

    private static final TruffleLogger LOGGER = PythonLanguage.getLogger(GraalHPyContext.class);

    public static final long SIZEOF_LONG = java.lang.Long.BYTES;

    @TruffleBoundary
    public static GraalHPyContext ensureHPyWasLoaded(Node node, PythonContext context, TruffleString name, TruffleString path) throws IOException, ApiInitException, ImportException {
        if (!context.hasHPyContext()) {
            /*
             * TODO(fa): Currently, you can't have the HPy context without the C API context. This
             * should eventually be possible but requires some refactoring.
             */
            CApiContext.ensureCapiWasLoaded(node, context, name, path);

            try {
                GraalHPyContext hPyContext = context.createHPyContext(GraalHPyLLVMContext.loadLLVMLibrary(context));
                assert hPyContext == context.getHPyContext();
                return hPyContext;
            } catch (PException e) {
                /*
                 * Python exceptions that occur during the HPy API initialization are just passed
                 * through.
                 */
                throw e.getExceptionForReraise(false);
            } catch (Exception e) {
                throw new ApiInitException(CExtContext.wrapJavaException(e, node), name, ErrorMessages.HPY_LOAD_ERROR);
            }
        }
        return context.getHPyContext();
    }

    /**
     * This method loads an HPy extension module and will initialize the corresponding native
     * contexts if necessary.
     *
     * @param location The node that's requesting this operation. This is required for reporting
     *            correct source code location in case exceptions occur.
     * @param context The Python context object.
     * @param name The name of the module to load (also just required for creating appropriate error
     *            messages).
     * @param path The path of the C extension module to load (usually something ending with
     *            {@code .so} or {@code .pyd} or similar).
     * @param checkResultNode An adopted node instance. This is necessary because the result check
     *            could raise an exception and only an adopted node will report useful source
     *            locations.
     * @return A Python module.
     * @throws IOException If the specified file cannot be loaded.
     * @throws ApiInitException If the corresponding native context could not be initialized.
     * @throws ImportException If an exception occurred during C extension initialization.
     */
    @TruffleBoundary
    public static Object loadHPyModule(Node location, PythonContext context, TruffleString name, TruffleString path, boolean debug,
                    HPyCheckFunctionResultNode checkResultNode) throws IOException, ApiInitException, ImportException {

        /*
         * Unfortunately, we need eagerly initialize the HPy context because the ctors of the
         * extension may already require some symbols defined in the HPy API or C API.
         */
        GraalHPyContext hpyUniversalContext = GraalHPyContext.ensureHPyWasLoaded(location, context, name, path);
        GraalHPyNativeContext backend = hpyUniversalContext.backend;
        Object llvmLibrary = backend.loadExtensionLibrary(location, context, name, path);
        TruffleString basename = getBaseName(name);
        TruffleString hpyInitFuncName = StringUtils.cat(T_HPY_INIT, basename);
        boolean saved = hpyUniversalContext.debugMode;
        hpyUniversalContext.debugMode = debug;
        try {
            Object nativeResult = backend.initHPyModule(llvmLibrary, hpyInitFuncName, name, path, debug);
            return checkResultNode.execute(context.getThreadState(context.getLanguage()), name, nativeResult);
        } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
            throw new ImportException(CExtContext.wrapJavaException(e, location), name, path, ErrorMessages.CANNOT_INITIALIZE_WITH, path, basename, "");
        } finally {
            hpyUniversalContext.debugMode = saved;
        }
    }

    public long createNativeArguments(Object[] delegate, InteropLibrary delegateLib) {
        return backend.createNativeArguments(delegate, delegateLib);
    }

    public void freeNativeArgumentsArray(int nargs) {
        backend.freeNativeArgumentsArray(nargs);
    }

    public interface HPyUpcall {
        String getName();
    }

    /**
     * Enum of C types used in the HPy API. These type names need to stay in sync with the
     * declarations in 'hpytypes.h'.
     */
    public enum LLVMType {
        HPyFunc_noargs,
        HPyFunc_o,
        HPyFunc_varargs,
        HPyFunc_keywords,
        HPyFunc_unaryfunc,
        HPyFunc_binaryfunc,
        HPyFunc_ternaryfunc,
        HPyFunc_inquiry,
        HPyFunc_lenfunc,
        HPyFunc_ssizeargfunc,
        HPyFunc_ssizessizeargfunc,
        HPyFunc_ssizeobjargproc,
        HPyFunc_ssizessizeobjargproc,
        HPyFunc_objobjargproc,
        HPyFunc_freefunc,
        HPyFunc_getattrfunc,
        HPyFunc_getattrofunc,
        HPyFunc_setattrfunc,
        HPyFunc_setattrofunc,
        HPyFunc_reprfunc,
        HPyFunc_hashfunc,
        HPyFunc_richcmpfunc,
        HPyFunc_getiterfunc,
        HPyFunc_iternextfunc,
        HPyFunc_descrgetfunc,
        HPyFunc_descrsetfunc,
        HPyFunc_initproc,
        HPyFunc_getter,
        HPyFunc_setter,
        HPyFunc_objobjproc,
        HPyFunc_traverseproc,
        HPyFunc_destructor,
        HPyFunc_getbufferproc,
        HPyFunc_releasebufferproc,
        HPyFunc_destroyfunc,
        HPyModule_init;
    }

    public static final int IMMUTABLE_HANDLE_COUNT = 256;

    private Object[] hpyHandleTable;
    private int nextHandle = 1;

    private Object[] hpyGlobalsTable = new Object[]{GraalHPyHandle.NULL_HANDLE_DELEGATE};
    private final HandleStack freeStack = new HandleStack(16);
    private final GraalHPyNativeContext backend;

    /**
     * This field mirrors value of {@link PythonOptions#HPyEnableJNIFastPaths}. We store it in this
     * final field because the value is also used in non-PE code paths.
     */
    final boolean useNativeFastPaths;

    /**
     * This is set to {@code true} if an HPy extension is initialized (i.e. {@code HPyInit_*} is
     * called) in debug mode. The value is then used to create the right closures for down calls
     * during module ({@code HPyModule_Create}) and type creation ({@code HPyType_FromSpec}). We
     * need this because the debug context is just a wrapper around the universal context, so the
     * module and type creation will look as normal. For reference on how other implementations do
     * it:
     * <p>
     * CPython stores the HPy context into global C variable {@code _ctx_for_trampolines} defined by
     * {@code HPy_MODINIT}. This variable belongs to the HPy extension and the context is loaded
     * from it when calling HPy extension functions.
     * </p>
     * <p>
     * PyPy has a different structure but basically also uses a global state (see file
     * {@code interp_hpy.py}). When initializing the module, the appropriate <em>handle manager</em>
     * is used. The manager then decides which trampolines are used to call HPy extensions and the
     * trampolines pick the appropriate context.
     * </p>
     */
    private boolean debugMode;

    /**
     * Few well known Python objects that are also HPyContext constants are guaranteed to always get
     * the same handle.
     */
    public static final int SINGLETON_HANDLE_NONE = 1;
    public static final int SINGLETON_HANDLE_NOT_IMPLEMENTED = 2;
    public static final int SINGLETON_HANDLE_ELIPSIS = 3;

    /**
     * The global reference queue is a list consisting of {@link GraalHPyHandleReference} objects.
     * It is used to keep those objects (which are weak refs) alive until they are enqueued in the
     * corresponding reference queue. The list instance referenced by this variable is exclusively
     * owned by the main thread (i.e. the main thread may operate on the list without
     * synchronization). The HPy reference cleaner thread (see
     * {@link GraalHPyReferenceCleanerRunnable}) will consume this instance using an atomic
     * {@code getAndSet} operation. At this point, the ownership is transferred to the cleaner
     * thread.
     */
    public final AtomicReference<GraalHPyHandleReference> references = new AtomicReference<>(null);
    private ReferenceQueue<Object> nativeSpaceReferenceQueue;
    @CompilationFinal private RootCallTarget referenceCleanerCallTarget;
    private Thread hpyReferenceCleanerThread;

    private long nativeSpacePointers;

    private final ScheduledExecutorService scheduler;

    public GraalHPyContext(PythonContext context, Object hpyLibrary) throws Exception {
        super(context, hpyLibrary, false /* TODO: provide proper value */);
        CompilerAsserts.neverPartOfCompilation();
        PythonLanguage language = context.getLanguage();
        int traceUpcallsInterval = language.getEngineOption(PythonOptions.HPyTraceUpcalls);
        Boolean useNativeFastPaths = language.getEngineOption(PythonOptions.HPyEnableJNIFastPaths);
        HPyBackendMode backendMode = language.getEngineOption(PythonOptions.HPyBackend);

        nextHandle = GraalHPyBoxing.SINGLETON_HANDLE_MAX + 1;
        hpyHandleTable = new Object[IMMUTABLE_HANDLE_COUNT * 2];

        // initialize singleton handles
        hpyHandleTable[0] = GraalHPyHandle.NULL_HANDLE_DELEGATE;
        hpyHandleTable[SINGLETON_HANDLE_NONE] = PNone.NONE;
        hpyHandleTable[SINGLETON_HANDLE_NOT_IMPLEMENTED] = PNotImplemented.NOT_IMPLEMENTED;
        hpyHandleTable[SINGLETON_HANDLE_ELIPSIS] = PEllipsis.INSTANCE;

        LOGGER.config("Using HPy backend:" + backendMode.name());
        if (backendMode == HPyBackendMode.JNI) {
            this.useNativeFastPaths = useNativeFastPaths;
            backend = new GraalHPyJNIContext(this, traceUpcallsInterval > 0);
        } else if (backendMode == HPyBackendMode.NFI) {
            throw CompilerDirectives.shouldNotReachHere("not yet implemented");
        } else if (backendMode == HPyBackendMode.LLVM) {
            // TODO(fa): we currently don't use native fast paths with the LLVM backend
            this.useNativeFastPaths = false;
            backend = new GraalHPyLLVMContext(this, traceUpcallsInterval > 0);
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        backend.initNativeContext();

        // createMembers already assigns numeric handles to "singletons"
        nextHandle = IMMUTABLE_HANDLE_COUNT;

        assert getHPyHandleForObject(PNone.NONE) == SINGLETON_HANDLE_NONE;
        assert getHPyHandleForObject(PEllipsis.INSTANCE) == SINGLETON_HANDLE_ELIPSIS;
        assert getHPyHandleForObject(PNotImplemented.NOT_IMPLEMENTED) == SINGLETON_HANDLE_NOT_IMPLEMENTED;

        if (traceUpcallsInterval > 0) {
            scheduler = Executors.newScheduledThreadPool(1);
            startUpcallsDaemon(traceUpcallsInterval);
        } else {
            scheduler = null;
        }
    }

    /**
     * Reference cleaner action that will be executed by the {@link AsyncHandler}.
     */
    private static final class GraalHPyHandleReferenceCleanerAction implements AsyncHandler.AsyncAction {

        private final GraalHPyHandleReference[] nativeObjectReferences;

        public GraalHPyHandleReferenceCleanerAction(GraalHPyHandleReference[] nativeObjectReferences) {
            this.nativeObjectReferences = nativeObjectReferences;
        }

        @Override
        public void execute(PythonContext context) {
            Object[] pArguments = PArguments.create(1);
            PArguments.setArgument(pArguments, 0, nativeObjectReferences);
            GenericInvokeNode.getUncached().execute(context.getHPyContext().getReferenceCleanerCallTarget(), pArguments);
        }
    }

    private RootCallTarget getReferenceCleanerCallTarget() {
        if (referenceCleanerCallTarget == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            referenceCleanerCallTarget = PythonUtils.getOrCreateCallTarget(new HPyNativeSpaceCleanerRootNode(getContext()));
        }
        return referenceCleanerCallTarget;
    }

    /**
     * This is the HPy cleaner thread runnable. It will run in parallel to the main thread, collect
     * references from the corresponding reference queue, and eventually call
     * {@link HPyNativeSpaceCleanerRootNode}. For this, the cleaner thread consumes the
     * {@link #references} list by exchanging it with an empty one (for a description of the
     * exchanging process, see also {@link #references}).
     */
    static final class GraalHPyReferenceCleanerRunnable implements Runnable {
        private static final TruffleLogger LOGGER = PythonLanguage.getLogger(GraalHPyReferenceCleanerRunnable.class);
        private final ReferenceQueue<?> referenceQueue;
        private GraalHPyHandleReference cleanerList;

        GraalHPyReferenceCleanerRunnable(ReferenceQueue<?> referenceQueue) {
            this.referenceQueue = referenceQueue;
        }

        @Override
        public void run() {
            try {
                PythonContext pythonContext = PythonContext.get(null);
                PythonLanguage language = pythonContext.getLanguage();
                GraalHPyContext hPyContext = pythonContext.getHPyContext();
                RootCallTarget callTarget = hPyContext.getReferenceCleanerCallTarget();
                PDict dummyGlobals = pythonContext.factory().createDict();
                boolean isLoggable = LOGGER.isLoggable(Level.FINE);
                /*
                 * Intentionally retrieve the thread state every time since this will kill the
                 * thread if shutting down.
                 */
                while (!pythonContext.getThreadState(language).isShuttingDown()) {
                    Reference<?> reference = null;
                    try {
                        reference = referenceQueue.remove();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    ArrayList<GraalHPyHandleReference> refs = new ArrayList<>();
                    do {
                        if (reference instanceof GraalHPyHandleReference) {
                            refs.add((GraalHPyHandleReference) reference);
                        }
                        // consume all
                        reference = referenceQueue.poll();
                    } while (reference != null);

                    if (isLoggable) {
                        LOGGER.fine(PythonUtils.formatJString("Collected references: %d", refs.size()));
                    }

                    /*
                     * To avoid race conditions, we take the whole references list such that we can
                     * solely process it. At this point, the references list is owned by the main
                     * thread and this will now transfer ownership to the cleaner thread. The list
                     * will be replaced by an empty list (which will then be owned by the main
                     * thread).
                     */
                    GraalHPyHandleReference refList;
                    int retries = 0;
                    do {
                        /*
                         * If 'refList' is null then the main is currently updating it. So, we need
                         * to repeat until we get something. The written empty list will just be
                         * lost.
                         */
                        refList = hPyContext.references.getAndSet(null);
                    } while (refList == null && retries++ < 3);

                    if (!refs.isEmpty()) {
                        try {
                            Object[] arguments = PArguments.create(3);
                            PArguments.setGlobals(arguments, dummyGlobals);
                            PArguments.setException(arguments, PException.NO_EXCEPTION);
                            PArguments.setCallerFrameInfo(arguments, PFrame.Reference.EMPTY);
                            PArguments.setArgument(arguments, 0, refs.toArray(new GraalHPyHandleReference[0]));
                            PArguments.setArgument(arguments, 1, refList);
                            PArguments.setArgument(arguments, 2, cleanerList);
                            cleanerList = (GraalHPyHandleReference) CallTargetInvokeNode.invokeUncached(callTarget, arguments);
                        } catch (PException e) {
                            /*
                             * Since the cleaner thread is not running any Python code, we should
                             * never receive a Python exception. If it happens, consider that to be
                             * a problem (however, it is not fatal problem).
                             */
                            if (e.getUnreifiedException() instanceof PBaseException managedException) {
                                e.setMessage(managedException.getFormattedMessage());
                            }
                            LOGGER.warning("HPy reference cleaner thread received a Python exception: " + e);
                        }
                    }
                }
            } catch (PythonThreadKillException e) {
                // this is exception shuts down the thread
                LOGGER.fine("HPy reference cleaner thread received exit signal.");
            } catch (ControlFlowException e) {
                LOGGER.warning("HPy reference cleaner thread received unexpected control flow exception.");
            } catch (Exception e) {
                LOGGER.severe("HPy reference cleaner thread received fatal exception: " + e);
            }
            LOGGER.fine("HPy reference cleaner thread is exiting.");
        }
    }

    /**
     * Root node that actually runs the destroy functions for the native memory of unreachable
     * Python objects.
     */
    private static final class HPyNativeSpaceCleanerRootNode extends PRootNode {
        private static final Signature SIGNATURE = new Signature(-1, false, -1, false, tsArray("refs"), EMPTY_TRUFFLESTRING_ARRAY);
        private static final TruffleLogger LOGGER = PythonLanguage.getLogger(GraalHPyContext.HPyNativeSpaceCleanerRootNode.class);

        @Child private PCallHPyFunction callBulkFree;

        HPyNativeSpaceCleanerRootNode(PythonContext context) {
            super(context.getLanguage());
        }

        @Override
        public Object execute(VirtualFrame frame) {
            /*
             * This node is not running any Python code in the sense that it does not run any code
             * that would run in CPython's interpreter loop. So, we don't need to do a
             * calleeContext.enter/exit since we should never get any Python exception.
             */

            GraalHPyHandleReference[] handleReferences = (GraalHPyHandleReference[]) PArguments.getArgument(frame, 0);
            GraalHPyHandleReference refList = (GraalHPyHandleReference) PArguments.getArgument(frame, 1);
            GraalHPyHandleReference oldRefList = (GraalHPyHandleReference) PArguments.getArgument(frame, 2);
            long startTime = 0;
            long middleTime = 0;
            final int n = handleReferences.length;
            boolean loggable = LOGGER.isLoggable(Level.FINE);

            if (loggable) {
                startTime = System.currentTimeMillis();
            }

            GraalHPyContext context = PythonContext.get(this).getHPyContext();

            if (CompilerDirectives.inInterpreter()) {
                com.oracle.truffle.api.nodes.LoopNode.reportLoopCount(this, n);
            }

            // mark queued references as cleaned
            for (int i = 0; i < n; i++) {
                handleReferences[i].cleaned = true;
            }

            // remove marked references from the global reference list such that they can die
            GraalHPyHandleReference prev = null;
            for (GraalHPyHandleReference cur = refList; cur != null; cur = cur.next) {
                if (cur.cleaned) {
                    if (prev != null) {
                        prev.next = cur.next;
                    } else {
                        // new head
                        refList = cur.next;
                    }
                } else {
                    prev = cur;
                }
            }

            /*
             * Merge the received reference list into the existing one or just take it if there
             * wasn't one before.
             */
            if (prev != null) {
                // if prev exists, it now points to the tail
                prev.next = oldRefList;
            } else {
                refList = oldRefList;
            }

            if (loggable) {
                middleTime = System.currentTimeMillis();
            }

            NativeSpaceArrayWrapper nativeSpaceArrayWrapper = new NativeSpaceArrayWrapper(handleReferences);
            if (callBulkFree == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callBulkFree = insert(PCallHPyFunctionNodeGen.create());
            }
            callBulkFree.call(context, GraalHPyNativeSymbol.GRAAL_HPY_BULK_FREE, nativeSpaceArrayWrapper, nativeSpaceArrayWrapper.getArraySize());

            if (loggable) {
                final long countDuration = middleTime - startTime;
                final long duration = System.currentTimeMillis() - middleTime;
                LOGGER.fine(PythonUtils.formatJString("Cleaned references: %d", n));
                LOGGER.fine(PythonUtils.formatJString("Count duration: %d", countDuration));
                LOGGER.fine(PythonUtils.formatJString("Duration: %d", duration));
            }
            return refList;
        }

        @Override
        public Signature getSignature() {
            return SIGNATURE;
        }

        @Override
        public String getName() {
            return "hpy_native_reference_cleaner";
        }

        @Override
        public boolean isInternal() {
            return true;
        }

        @Override
        public boolean isPythonInternal() {
            return true;
        }
    }

    public long getWcharSize() {
        return backend.getWcharSize();
    }

    public void initHPyDebugContext() throws ApiInitException {
        backend.initHPyDebugContext();
    }

    public PythonModule getHPyDebugModule() throws ImportException {
        return backend.getHPyDebugModule();
    }

    boolean isDebugMode() {
        return debugMode;
    }

    public GraalHPyNativeContext getBackend() {
        return backend;
    }

    @SuppressWarnings("static-method")
    public GraalHPyHandle createHandle(Object delegate) {
        return GraalHPyHandle.create(delegate);
    }

    @SuppressWarnings("static-method")
    public GraalHPyHandle createField(Object delegate, int idx) {
        return GraalHPyHandle.createField(delegate, idx);
    }

    public int createGlobal(Object delegate, int idx) {
        assert !GilNode.getUncached().acquire(PythonContext.get(null)) : "Gil not held when creating global";
        final int newIdx;
        if (idx <= 0) {
            newIdx = allocateHPyGlobal();
        } else {
            newIdx = idx;
        }
        hpyGlobalsTable[newIdx] = delegate;
        if (useNativeFastPaths) {
            mirrorGlobalNativeSpacePointerToNative(delegate, newIdx);
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(PythonUtils.formatJString("allocating HPy global %d (object: %s)", newIdx, delegate));
        }
        return newIdx;
    }

    int getEndIndexOfGlobalTable() {
        for (int i = hpyGlobalsTable.length - 1; i > 0; i--) {
            if (hpyGlobalsTable[i] != null) {
                return i + 1;
            }
        }
        return hpyGlobalsTable.length;
    }

    void initBatchGlobals(int startIdx, int nModuleGlobals) {
        if (nModuleGlobals == 0) {
            return;
        }
        int gtLen = hpyGlobalsTable.length;
        int endIdx = startIdx + nModuleGlobals;
        if (endIdx >= gtLen) {
            int newSize = endIdx + 1;
            LOGGER.fine(() -> "resizing HPy globals table to " + newSize);
            hpyGlobalsTable = Arrays.copyOf(hpyGlobalsTable, newSize);
            if (useNativeFastPaths) {
                reallocateNativeSpacePointersMirror(hpyHandleTable.length, gtLen);
            }
        }
        Arrays.fill(hpyGlobalsTable, startIdx, endIdx, GraalHPyHandle.NULL_HANDLE_DELEGATE);
        if (useNativeFastPaths) {
            GraalHPyNativeCache.initGlobalsNativeSpacePointer(nativeSpacePointers, hpyHandleTable.length, startIdx, nModuleGlobals);
        }
    }

    @TruffleBoundary
    private int allocateHPyGlobal() {
        int handle = 0;
        for (int i = 1; i < hpyGlobalsTable.length; i++) {
            if (hpyGlobalsTable[i] == null) {
                handle = i;
                break;
            }
        }
        if (handle == 0) {
            // resize
            handle = hpyGlobalsTable.length;
            int newSize = Math.max(16, hpyGlobalsTable.length * 2);
            LOGGER.fine(() -> "resizing HPy globals table to " + newSize);
            hpyGlobalsTable = Arrays.copyOf(hpyGlobalsTable, newSize);
            if (useNativeFastPaths) {
                reallocateNativeSpacePointersMirror(hpyHandleTable.length, handle);
            }
        }
        return handle;
    }

    private int resizeHandleTable() {
        CompilerAsserts.neverPartOfCompilation();
        assert nextHandle == hpyHandleTable.length;
        int oldSize = hpyHandleTable.length;
        int newSize = Math.max(16, hpyHandleTable.length * 2);
        LOGGER.fine(() -> "resizing HPy handle table to " + newSize);
        hpyHandleTable = Arrays.copyOf(hpyHandleTable, newSize);
        if (useNativeFastPaths) {
            reallocateNativeSpacePointersMirror(oldSize, hpyGlobalsTable.length);
        }
        return nextHandle++;
    }

    public int getHPyHandleForObject(Object object) {
        assert !(object instanceof GraalHPyHandle);
        int singletonHandle = getHPyHandleForSingleton(object);
        if (singletonHandle != -1) {
            return singletonHandle;
        }
        return getHPyHandleForNonSingleton(object);
    }

    public static int getHPyHandleForSingleton(Object object) {
        CompilerAsserts.neverPartOfCompilation();
        assert !(object instanceof GraalHPyHandle);
        return GraalHPyContextFactory.GetHPyHandleForSingletonNodeGen.getUncached().execute(object);
    }

    /**
     * Allocates a handle for the given object. This method is intended to be used by the
     * appropriate backend to initialize the context handles (i.e. handles available in
     * {@code HPyContext *}; e.g. {@code HPyContext.h_None}). Following properties/restrictions
     * apply:
     * <ul>
     * <li>This method *MUST NOT* be called after the context initialization was finished.</li>
     * <li>The handles are not mirrored to the native cache even if {@link #useNativeFastPaths}.
     * This should be done in a bulk operation after all context handles have been allocated.</li>
     * <li>{@code object} must not be a singleton handle (i.e.
     * {@link #getHPyHandleForSingleton(Object)} must return {@code -1}).</li>
     * </ul>
     */
    public int getHPyContextHandle(Object object) {
        CompilerAsserts.neverPartOfCompilation();
        assert getHPyHandleForSingleton(object) == -1;
        assert freeStack.getTop() == 0;
        assert nextHandle < hpyHandleTable.length;
        if (nextHandle >= IMMUTABLE_HANDLE_COUNT) {
            throw CompilerDirectives.shouldNotReachHere("attempting to create context handle after initialization");
        }
        int i = nextHandle++;
        assert hpyHandleTable[i] == null;
        hpyHandleTable[i] = object;
        return i;
    }

    public int getHPyHandleForNonSingleton(Object object) {
        assert !(object instanceof GraalHPyHandle);
        // find free association

        int handle = freeStack.pop();
        if (handle == -1) {
            if (nextHandle < hpyHandleTable.length) {
                handle = nextHandle++;
            } else {
                CompilerDirectives.transferToInterpreter();
                handle = resizeHandleTable();
            }
        }

        assert 0 <= handle && handle < hpyHandleTable.length;
        assert hpyHandleTable[handle] == null;

        hpyHandleTable[handle] = object;
        if (useNativeFastPaths) {
            mirrorNativeSpacePointerToNative(object, handle);
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(PythonUtils.formatJString("allocating HPy handle %d (object: %s)", handle, object));
        }
        return handle;
    }

    @GenerateUncached
    @ImportStatic(PGuards.class)
    public abstract static class GetHPyHandleForSingleton extends Node {
        public abstract int execute(Object delegateObject);

        @Specialization(guards = "isNoValue(x)")
        static int doNoValue(@SuppressWarnings("unused") PNone x) {
            return 0;
        }

        @Specialization(guards = "!isNoValue(x)")
        static int doNone(@SuppressWarnings("unused") PNone x) {
            return SINGLETON_HANDLE_NONE;
        }

        @Specialization
        static int doElipsis(@SuppressWarnings("unused") PEllipsis x) {
            return SINGLETON_HANDLE_ELIPSIS;
        }

        @Specialization
        static int doNotImplemented(@SuppressWarnings("unused") PNotImplemented x) {
            return SINGLETON_HANDLE_NOT_IMPLEMENTED;
        }

        @Fallback
        static int doOthers(@SuppressWarnings("unused") Object delegate) {
            return -1;
        }
    }

    @TruffleBoundary
    private void mirrorNativeSpacePointerToNative(Object delegate, int handleID) {
        assert useNativeFastPaths;
        long l;
        if (delegate instanceof PythonObject) {
            Object nativeSpace = HPyGetNativeSpacePointerNode.doPythonObject((PythonObject) delegate);
            try {
                l = nativeSpace instanceof Long ? ((long) nativeSpace) : InteropLibrary.getUncached().asPointer(nativeSpace);
            } catch (UnsupportedMessageException e) {
                throw CompilerDirectives.shouldNotReachHere();
            }
        } else {
            l = 0;
        }
        GraalHPyNativeCache.putHandleNativeSpacePointer(nativeSpacePointers, handleID, l);
    }

    @TruffleBoundary
    private void mirrorGlobalNativeSpacePointerToNative(Object delegate, int globalID) {
        assert useNativeFastPaths;
        long l;
        if (delegate instanceof PythonObject) {
            Object nativeSpace = HPyGetNativeSpacePointerNode.doPythonObject((PythonObject) delegate);
            try {
                l = nativeSpace instanceof Long ? ((long) nativeSpace) : InteropLibrary.getUncached().asPointer(nativeSpace);
            } catch (UnsupportedMessageException e) {
                throw CompilerDirectives.shouldNotReachHere();
            }
        } else {
            l = 0;
        }
        GraalHPyNativeCache.putGlobalNativeSpacePointer(nativeSpacePointers, hpyHandleTable.length, globalID, l);
    }

    @TruffleBoundary
    private void reallocateNativeSpacePointersMirror(int oldHandleTabelSize, int oldGlobalsTableSize) {
        assert useNativeFastPaths;
        nativeSpacePointers = GraalHPyNativeCache.reallocateNativeCache(nativeSpacePointers, oldHandleTabelSize, hpyHandleTable.length, oldGlobalsTableSize, hpyGlobalsTable.length);
        backend.setNativeCache(nativeSpacePointers);
    }

    /**
     * Allocates a native array (element size is {@link #SIZEOF_LONG} for as many elements as in
     * {@link #hpyHandleTable} and writes the native space pointers of all objects in the handle
     * table into this array. The pointer of the array is then set to
     * {@code ((HPyContext) ctx)->_private} and meant to be used by the {@code ctx_Cast}'s upcall
     * stub to avoid an expensive upcall.
     */
    @TruffleBoundary
    void allocateNativeSpacePointersMirror() {
        long arrayPtr = GraalHPyNativeCache.allocateNativeCache(hpyHandleTable.length, hpyGlobalsTable.length);

        // publish pointer value (needed for initialization)
        nativeSpacePointers = arrayPtr;

        // write existing values to mirror; start at 1 to omit the NULL handle
        for (int i = 1; i < hpyHandleTable.length; i++) {
            Object delegate = hpyHandleTable[i];
            if (delegate != null) {
                mirrorNativeSpacePointerToNative(delegate, i);
            }
        }

        // commit pointer value for native usage
        backend.setNativeCache(arrayPtr);
    }

    public Object getObjectForHPyHandle(int handle) {
        assert !GilNode.getUncached().acquire(PythonContext.get(null)) : "Gil not held when resolving object from handle";
        assert !GraalHPyBoxing.isBoxedInt(handle) && !GraalHPyBoxing.isBoxedDouble(handle) : "trying to lookup boxed primitive";
        return hpyHandleTable[handle];
    }

    public Object getObjectForHPyGlobal(int handle) {
        assert !GilNode.getUncached().acquire(PythonContext.get(null)) : "Gil not held when resolving object from global";
        assert !GraalHPyBoxing.isBoxedInt(handle) && !GraalHPyBoxing.isBoxedDouble(handle) : "trying to lookup boxed primitive";
        return hpyGlobalsTable[handle];
    }

    public boolean releaseHPyHandleForObject(int handle) {
        assert !GilNode.getUncached().acquire(PythonContext.get(null)) : "Gil not held when releasing handle";
        assert handle != 0 : "NULL handle cannot be released";
        assert hpyHandleTable[handle] != null : PythonUtils.formatJString("releasing handle that has already been released: %d", handle);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(PythonUtils.formatJString("releasing HPy handle %d (object: %s)", handle, hpyHandleTable[handle]));
        }
        if (handle < IMMUTABLE_HANDLE_COUNT) {
            return false;
        }
        hpyHandleTable[handle] = null;
        freeStack.push(handle);
        return true;
    }

    /**
     * A weak reference to an object that has an associated HPy native space (
     * {@link PythonHPyObject}).
     */
    static final class GraalHPyHandleReference extends WeakReference<Object> {

        private final Object nativeSpace;
        private final Object destroyFunc;

        boolean cleaned;
        private GraalHPyHandleReference next;

        public GraalHPyHandleReference(Object referent, ReferenceQueue<Object> q, Object nativeSpace, Object destroyFunc) {
            super(referent, q);
            this.nativeSpace = nativeSpace;
            this.destroyFunc = destroyFunc;
        }

        public Object getNativeSpace() {
            return nativeSpace;
        }

        public Object getDestroyFunc() {
            return destroyFunc;
        }

        public GraalHPyHandleReference getNext() {
            return next;
        }

        public void setNext(GraalHPyHandleReference next) {
            this.next = next;
        }
    }

    /**
     * Registers an HPy native space of a Python object.<br/>
     * Use this method to register a native memory that is associated with a Python object in order
     * to ensure that the native memory will be free'd when the owning Python object dies.<br/>
     * This works by creating a weak reference to the Python object, using a thread that
     * concurrently polls the reference queue. If threading is allowed, cleaning will be done fully
     * concurrent on a cleaner thread. If not, an async action will be scheduled to free the native
     * memory. Hence, the destroy function could also be executed on the cleaner thread.
     *
     * @param pythonObject The Python object that has associated native memory.
     * @param dataPtr The pointer object of the native memory.
     * @param destroyFunc The destroy function to call when the Python object is unreachable (may be
     *            {@code null}; in this case, bare {@code free} will be used).
     */
    @TruffleBoundary
    public void createHandleReference(Object pythonObject, Object dataPtr, Object destroyFunc) {
        GraalHPyHandleReference newHead = new GraalHPyHandleReference(pythonObject, ensureReferenceQueue(), dataPtr, destroyFunc);
        references.getAndAccumulate(newHead, (prev, x) -> {
            x.next = prev;
            return x;
        });
    }

    private ReferenceQueue<Object> ensureReferenceQueue() {
        if (nativeSpaceReferenceQueue == null) {
            ReferenceQueue<Object> referenceQueue = createReferenceQueue();
            nativeSpaceReferenceQueue = referenceQueue;
            return referenceQueue;
        }
        return nativeSpaceReferenceQueue;
    }

    @TruffleBoundary
    private ReferenceQueue<Object> createReferenceQueue() {
        final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

        // lazily register the runnable that concurrently collects the queued references
        Env env = getContext().getEnv();
        if (env.isCreateThreadAllowed()) {
            Thread thread = env.createThread(new GraalHPyReferenceCleanerRunnable(referenceQueue), null, getContext().getThreadGroup());
            // Make the cleaner thread a daemon; it should not prevent JVM shutdown.
            thread.setDaemon(true);
            thread.start();
            hpyReferenceCleanerThread = thread;
        } else {
            getContext().registerAsyncAction(() -> {
                Reference<?> reference = null;
                if (PythonOptions.AUTOMATIC_ASYNC_ACTIONS) {
                    try {
                        reference = referenceQueue.remove();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    referenceQueue.poll();
                }

                ArrayList<GraalHPyHandleReference> refs = new ArrayList<>();
                do {
                    if (reference instanceof GraalHPyHandleReference) {
                        refs.add((GraalHPyHandleReference) reference);
                    }
                    // consume all
                    reference = referenceQueue.poll();
                } while (reference != null);

                if (!refs.isEmpty()) {
                    return new GraalHPyHandleReferenceCleanerAction(refs.toArray(new GraalHPyHandleReference[0]));
                }

                return null;
            });
        }
        return referenceQueue;
    }

    @TruffleBoundary
    @Override
    protected Store initializeSymbolCache() {
        PythonLanguage language = getContext().getLanguage();
        Shape symbolCacheShape = language.getHPySymbolCacheShape();
        // We will always get an empty shape from the language and we do always add same key-value
        // pairs (in the same order). So, in the end, each context should get the same shape.
        Store s = new Store(symbolCacheShape);
        for (GraalHPyNativeSymbol sym : GraalHPyNativeSymbol.getValues()) {
            DynamicObjectLibrary.getUncached().put(s, sym, PNone.NO_VALUE);
        }
        return s;
    }

    /**
     * Join the reference cleaner thread.
     */
    public void finalizeContext() {
        Thread thread = this.hpyReferenceCleanerThread;
        if (thread != null) {
            if (thread.isAlive() && !thread.isInterrupted()) {
                thread.interrupt();
            }
            try {
                thread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        backend.finalizeNativeContext();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void startUpcallsDaemon(long interval) {
        scheduler.scheduleAtFixedRate(() -> {
            HPyUpcall[] upcalls = backend.getUpcalls();
            int[] counts = backend.getUpcallCounts();
            StringBuilder sb = new StringBuilder();
            sb.append("========= HPy context upcall counts (").append(backend.getName()).append(')');
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] != 0) {
                    sb.append(String.format("  %40s[%3d]: %d\n", upcalls[i].getName(), i, counts[i]));
                }
            }
            System.out.print(sb);
            System.out.flush();
        }, interval, interval, TimeUnit.MILLISECONDS);
    }
}
