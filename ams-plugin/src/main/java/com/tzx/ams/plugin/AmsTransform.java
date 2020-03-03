package com.tzx.ams.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.android.utils.Pair;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 16:59
 * Description: 自定义Transform
 */
public class AmsTransform extends Transform{
    private static final String TAG = "AmsTransform";
    private Map<String, File> modifyMap = new HashMap<>();
    private Project project;
    private static AmsConfig amsConfig;
    public AmsTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return AmsTransform.class.getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;//是否开启增量编译
    }

    public static AmsConfig getAmsConfig() {
        return amsConfig;
    }

    private void initConfig() {
        amsConfig = (AmsConfig) this.project.getExtensions().getByName(AmsConfig.class.getSimpleName());
        Logger.isDebug = amsConfig.isDebug;
        String projectDri = this.project.getProjectDir().getAbsolutePath();
        initfilterClassFile(projectDri);
        initAmsMethodFile(projectDri);
    }

    private void initfilterClassFile(String projectDri) {
        if (amsConfig.filterClassFile != null && amsConfig.filterClassFile.length() != 0) {
            String fileName = projectDri + File.separatorChar + amsConfig.filterClassFile;
            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Logger.log(TAG, "filterClassName:" + line);
                    if (amsConfig.filterClassNameList == null) {
                        amsConfig.filterClassNameList = new ArrayList<>();
                    }
                    if (line.length() > 0) {
                        amsConfig.filterClassNameList.add(line);
                    }
                }
                bufferedReader.close();
                fileReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initAmsMethodFile(String projectDri) {
        if (amsConfig.amsMethodFile != null && amsConfig.amsMethodFile.length() > 0) {
            String fileName = projectDri + File.separatorChar + amsConfig.amsMethodFile;
            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Logger.log(TAG, "amsMethodFile:" + line);
                    if (amsConfig.amsMethodFileList == null) {
                        amsConfig.amsMethodFileList = new ArrayList<>();
                    }
                    if (line.length() > 0) {
                        String[] strings = line.split("#");
                        if (strings.length != 2) {
                            continue;
                        }
                        String method = strings[0];
                        String classname = strings[1];
                        amsConfig.amsMethodFileList.add(Pair.of(method, classname));
                    }
                }
                bufferedReader.close();
                fileReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        //transform方法中才能获取到注册的对象
        initConfig();
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        // 获取输入（消费型输入，需要传递给下一个Transform）
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        for (TransformInput input : inputs) {
            // 遍历输入，分别遍历其中的jar以及directory
            for (JarInput jarInput : input.getJarInputs()) {
                 //对jar文件进行处理
                Logger.log(TAG, "Find jar input: " + jarInput.getName());
                transformJar(transformInvocation, jarInput);
            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                // 对directory进行处理
                Logger.log(TAG, "Find dir input:" + directoryInput.getFile().getName());
                transformDirectory(transformInvocation, directoryInput);
            }
        }
    }


    private void transformJar(TransformInvocation invocation, JarInput input) throws IOException {
        File tempDir = invocation.getContext().getTemporaryDir();
        String destName = input.getFile().getName();
        String hexName = DigestUtils.md5Hex(input.getFile().getAbsolutePath()).substring(0, 8);
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4);
        }
        // 获取输出路径
        File dest = invocation.getOutputProvider()
                //.getContentLocation(input.getFile().getAbsolutePath(), input.getContentTypes(), input.getScopes(), Format.JAR);
                .getContentLocation(destName + "_" + hexName, input.getContentTypes(), input.getScopes(), Format.JAR);
        JarFile originJar = new JarFile(input.getFile());
        //input:/build/intermediates/runtime_library_classes/release/classes.jar
        File outputJar = new File(tempDir, "temp_"+input.getFile().getName());
        //out:/build/tmp/transformClassesWithAmsTransformForRelease/temp_classes.jar
        //dest:/build/intermediates/transforms/AmsTransform/release/26.jar
        JarOutputStream output = new JarOutputStream(new FileOutputStream(outputJar));

        // 遍历原jar文件寻找class文件
        Enumeration<JarEntry> enumeration = originJar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry originEntry = enumeration.nextElement();
            InputStream inputStream = originJar.getInputStream(originEntry);
            String entryName = originEntry.getName();
            if (entryName.endsWith(".class")) {
                JarEntry destEntry = new JarEntry(entryName);
                output.putNextEntry(destEntry);
                byte[] sourceBytes = IOUtils.toByteArray(inputStream);
                // 修改class文件内容
                byte[] modifiedBytes = null;
                if (filterModifyClass(entryName)) {
                    Logger.log(TAG, "Modifyjar:" , entryName);
                    modifiedBytes = modifyClass(sourceBytes);
                }
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                output.write(modifiedBytes);
            }
            output.closeEntry();
        }
        output.close();
        originJar.close();
        // 复制修改后jar到输出路径
        FileUtils.copyFile(outputJar, dest);
    }


    private void transformDirectory(TransformInvocation invocation, DirectoryInput input) throws IOException {
        File tempDir = invocation.getContext().getTemporaryDir();
        // 获取输出路径
        File dest = invocation.getOutputProvider()
                .getContentLocation(input.getName(), input.getContentTypes(), input.getScopes(), Format.DIRECTORY);
        File dir = input.getFile();
        if (dir != null && dir.exists()) {
            //tempDir=build/tmp/transformClassesWithAmsTransformForDebug
            //dir=build/intermediates/javac/debug/compileDebugJavaWithJavac/classes

            traverseDirectory(dir.getAbsolutePath(), tempDir, dir);
            //Map<String, File> modifiedMap = new HashMap<>();
            //traverseDirectory(tempDir, dir, modifiedMap, dir.getAbsolutePath() + File.separator);

            //input.getFile=build/intermediates/javac/debug/compileDebugJavaWithJavac/classes
            //dest=build/intermediates/transforms/AmsTransform/debug/52

            FileUtils.copyDirectory(input.getFile(), dest);

            for (Map.Entry<String, File> entry : modifyMap.entrySet()) {
                File target = new File(dest.getAbsolutePath() + File.separatorChar + entry.getKey().replace('.', File.separatorChar) + ".class");
                if (target.exists()) {
                    target.delete();
                }
                FileUtils.copyFile(entry.getValue(), target);
                entry.getValue().delete();
            }
        }
    }

    /**
     * 遍历目录下面的class文件
     * @param basedir 基准目录，和dir对比需要找到包路径
     * @param tempDir 需要写入的临时目录
     * @param dir class文件目录
     * @throws IOException
     */
    private void traverseDirectory(String basedir, File tempDir, File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                traverseDirectory(basedir, tempDir, file);
            } else if (file.getAbsolutePath().endsWith(".class")) {
                String className = path2ClassName(file.getAbsolutePath()
                        .replace(basedir + File.separator, ""));
                byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(file));
                byte[] modifiedBytes = null;
                if (filterModifyClass(className + ".class")) {
                    Logger.log(TAG, "Modifydir:" , className + ".class");
                    modifiedBytes = modifyClass(sourceBytes);
                }
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                File modified = new File(tempDir, className + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                new FileOutputStream(modified).write(modifiedBytes);
                modifyMap.put(className, modified);
            }
        }
    }

    private boolean filterModifyClass(String className) {
        if (className == null || className.length() == 0) return false;
        String s = className.replace(File.separator, ".");
        if (amsConfig.filterClassNameList != null && amsConfig.filterClassNameList.size() > 0) {
            for (String str: amsConfig.filterClassNameList) {
                if (s.equals(str)) {
                    return false;
                }
            }
        }
        if (amsConfig.filterContainsClassStr != null && amsConfig.filterContainsClassStr.length > 0) {
            for (String str: amsConfig.filterContainsClassStr) {
                if (s.contains(str)) {
                    return false;
                }
            }
        }
        if (amsConfig.filterstartsWithClassStr != null && amsConfig.filterstartsWithClassStr.length > 0) {
            for (String str: amsConfig.filterstartsWithClassStr) {
                if (s.startsWith(str)) {
                    return false;
                }
            }
        }
        return true;
    }

    private byte[] modifyClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new AmsClassVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    static String path2ClassName(String pathName) {
        return pathName.replace(File.separator, ".").replace(".class", "");
    }

}
