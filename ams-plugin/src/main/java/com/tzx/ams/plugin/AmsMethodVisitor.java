package com.tzx.ams.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 16:53
 * Description:
 */
public class AmsMethodVisitor extends AdviceAdapter {
    private static final String ANNOTATION_TRACK_METHOD = "Lcom/tzx/ams/TrackMethod;";
    private static final String METHOD_EVENT_MANAGER = "com/tzx/ams/MethodEventManager";
    private final MethodVisitor methodVisitor;
    private final String methodName;

    private boolean needInject;
    private String tag;

    public AmsMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc) {
        super(Opcodes.ASM6, methodVisitor, access, name, desc);
        this.methodVisitor = methodVisitor;
        this.methodName = name;
    }

    /**
     * 访问类的注解
     * @param desc 表示类注解类的描述；
     * @param visible 表示该注解是否运行时可见
     * @return
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);
        if (desc.equals(ANNOTATION_TRACK_METHOD)) {
            needInject = true;
            return new AnnotationVisitor(Opcodes.ASM6, annotationVisitor) {
                /**
                 * @param name 注解key值
                 * @param value value值
                 */
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, value);
                    if (name.equals("tag") && value instanceof String) {
                        tag = (String) value;
                        log(tag + " methodName=" + methodName);
                    }
                }
            };
        }
        return annotationVisitor;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        handleMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        handleMethodExit();
    }

    private void handleMethodEnter() {
        if (needInject && tag != null) {
            //visitMethodInsn:访问方法操作指令
            //opcode：为INVOKESPECIAL,INVOKESTATIC,INVOKEVIRTUAL,INVOKEINTERFACE;
            //owner:方法拥有者的名称;
            //name:方法名称;
            //descriptor:方法描述，参数和返回值;
            //isInterface；是否是接口;
            methodVisitor.visitMethodInsn(INVOKESTATIC, METHOD_EVENT_MANAGER,
                    "getInstance", "()L"+METHOD_EVENT_MANAGER+";", false);
            //visitLdcInsn:访问ldc指令，也就是访问常量池索引；
            //value:必须是非空的Integer,Float,Double,Long,String,或者对象的Type,Array的Type,Method Sort的Type，或者Method Handle常量中的Handle，或者ConstantDynamic;
            methodVisitor.visitLdcInsn(tag);
            methodVisitor.visitLdcInsn(methodName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, METHOD_EVENT_MANAGER,
                    "notifyMethodEnter", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
    }

    private void handleMethodExit() {
        if (needInject && tag != null) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, METHOD_EVENT_MANAGER,
                    "getInstance", "()L"+METHOD_EVENT_MANAGER+";", false);
            methodVisitor.visitLdcInsn(tag);
            methodVisitor.visitLdcInsn(methodName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, METHOD_EVENT_MANAGER,
                    "notifyMethodExit", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
    }

    private void log(String s) {
        System.out.println("AmsMethodVisitor" + ":" + s);
    }
}
