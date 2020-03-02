package com.tzx.ams.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 16:44
 * Description:
 */
public class AmsClassVisitor extends ClassVisitor {
    public AmsClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6 ,classVisitor);
    }



    /**
     * 访问类的方法，如果需要修改类方法信息，则可以重写此方法;
     * @param access 表示该域的访问方式，public，private或者static,final等等；
     * @param name 指的是域的名称；
     * @param desc 表示方法的参数类型和返回值类型；
     * @param signature 指的是域的签名，一般是泛型域才会有签名;
     * @param exceptions
     * @return
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new AmsMethodVisitor(methodVisitor, access, name, desc);
    }
}
