package cn.bavelee.freereflection.hook;

import cn.bavelee.freereflection.Log;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Collection;

public class ReflectionTransformer implements ClassFileTransformer, Opcodes {


    private Instrumentation inst;
    private Collection<String> prefixClassNames;
    private Collection<String> fullClassNames;
    private final boolean dumpClass;

    public ReflectionTransformer(Instrumentation inst, Collection<String> prefixClassNames, Collection<String> fullClassNames, boolean dumpClass) {
        this.inst = inst;
        this.prefixClassNames = prefixClassNames;
        this.fullClassNames = fullClassNames;
        this.dumpClass = dumpClass;
    }

    public void unload() {
        if (this.inst != null) {
            this.inst.removeTransformer(this);
            this.inst = null;
        }
        if (this.prefixClassNames != null) {
            this.prefixClassNames.clear();
            this.prefixClassNames = null;
        }
        if (this.fullClassNames != null) {
            this.fullClassNames.clear();
            this.fullClassNames = null;
        }
    }

    private static final String ACCESSIBLE_OBJECT_INTERNAL_NAME = "java/lang/reflect/AccessibleObject";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (ACCESSIBLE_OBJECT_INTERNAL_NAME.equals(className)) {
            try {
                Log.info("Transforming...");
                ClassReader reader = new ClassReader(classfileBuffer);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                        if ("checkCanSetAccessible".equals(name) && "(Ljava/lang/Class;Ljava/lang/Class;Z)Z".equals(descriptor)) {
                            Log.info("Found checkCanSetAccessible");
                            methodVisitor = new AdviceAdapter(Opcodes.ASM9, methodVisitor, access, name, descriptor) {
                                @Override
                                protected void onMethodEnter() {
                                    Log.info("Injecting...");
                                    if (fullClassNames != null) {
                                        for (String fullClassName : fullClassNames) {
                                            Log.info("Whitelist name : " + fullClassName);
                                            returnTrueIfEquals(fullClassName, this);
                                        }
                                    }
                                    if (prefixClassNames != null) {
                                        for (String prefixClassName : prefixClassNames) {
                                            Log.info("Whitelist prefix : " + prefixClassName);
                                            returnTrueIfStartWith(prefixClassName, this);
                                        }
                                    }
                                }
                            };
                        }
                        return methodVisitor;
                    }
                };
                reader.accept(visitor, ClassReader.EXPAND_FRAMES);
                byte[] bytes = writer.toByteArray();
                if (dumpClass) {
                    Log.info("Dumping class...");
                    OutputStream os = new FileOutputStream("AccessibleObject.class");
                    os.write(bytes);
                    os.flush();
                    os.close();
                }
                Log.info("Transform successful");
                return bytes;
            } catch (Throwable e) {
                Log.error("failed to transform : ", className);
                e.printStackTrace();
                unload();
            }
        }
        return null;
    }

    private static void returnTrueIfStartWith(String classNamePrefix, MethodVisitor methodVisitor) {
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        methodVisitor.visitLdcInsn(classNamePrefix);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
        Label label1 = new Label();
        methodVisitor.visitJumpInsn(IFEQ, label1);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(38, label2);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(label1);
    }

    private static void returnTrueIfEquals(String fullClassName, MethodVisitor methodVisitor) {
        methodVisitor.visitLdcInsn(fullClassName);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        Label label3 = new Label();
        methodVisitor.visitJumpInsn(IFEQ, label3);
        Label label4 = new Label();
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(41, label4);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(label3);
    }
}
