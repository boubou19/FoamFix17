/*
 * Copyright (c) 2015 Vincent Lee
 * Copyright (c) 2020, 2021 Adrian "asie" Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.foamfix.coremod.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.bugfixmod.coremod.BugfixModClassTransformer;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public final class ClassSplicingUtil {
    private ClassSplicingUtil() {

    }

    private static byte[] getClassBytes(final String className) throws IOException {
        ClassLoader loader = ClassSplicingUtil.class.getClassLoader();
        if (loader instanceof LaunchClassLoader) {
            return ((LaunchClassLoader) loader).getClassBytes(className);
        } else {
            throw new RuntimeException("Incompatible class loader for FoamFixTransformer.getClassBytes: " + loader.getClass().getName());
        }
    }

    public static boolean spliceClasses(final ClassNode data, final String className, final boolean addMethods, final String... methods) {
        try {
            final byte[] dataSplice = getClassBytes(className);
            return spliceClasses(data, dataSplice, className, addMethods, methods);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean spliceClasses(final ClassNode nodeData, final byte[] dataSplice, final String className, final boolean addMethods, final String... methods) {
        // BugfixModClassTransformer.instance.logger.info("Splicing from " + className + " to " + targetClassName)
        if (dataSplice == null) {
            throw new RuntimeException("Class " + className + " not found! This is a FoamFix bug!");
        }
        boolean addedGlobal = false;

        final Set<String> methodSet = Sets.newHashSet(methods);
        final List<String> methodList = Lists.newArrayList(methods);

        final ClassReader readerSplice = new ClassReader(dataSplice);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = nodeData.name;
        final String targetClassName = targetClassName2.replace('/', '.');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };

        ClassNode nodeSplice = new ClassNode();
        readerSplice.accept(new RemappingClassAdapter(nodeSplice, remapper), ClassReader.EXPAND_FRAMES);
        for (String s : nodeSplice.interfaces) {
            if (s.contains("IFoamFix")) {
                nodeData.interfaces.add(s);
                BugfixModClassTransformer.instance.logger.info("Added INTERFACE: " + s);
            }
        }

        for (int i = 0; i < nodeSplice.methods.size(); i++) {
            if (methodSet.contains(nodeSplice.methods.get(i).name)) {
                MethodNode mn = nodeSplice.methods.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.methods.size(); j++) {
                    if (nodeData.methods.get(j).name.equals(mn.name)
                            && nodeData.methods.get(j).desc.equals(mn.desc)) {
                        MethodNode oldMn = nodeData.methods.get(j);
                        BugfixModClassTransformer.instance.logger.info("Spliced in METHOD: " + targetClassName + "." + mn.name);
                        nodeData.methods.set(j, mn);
                        if (nodeData.superName != null && nodeData.name.equals(nodeSplice.superName)) {
                            ListIterator<AbstractInsnNode> nodeListIterator = mn.instructions.iterator();
                            while (nodeListIterator.hasNext()) {
                                AbstractInsnNode node = nodeListIterator.next();
                                if (node instanceof MethodInsnNode
                                        && node.getOpcode() == Opcodes.INVOKESPECIAL) {
                                    MethodInsnNode methodNode = (MethodInsnNode) node;
                                    if (targetClassName2.equals(methodNode.owner)) {
                                        methodNode.owner = nodeData.superName;
                                    }
                                }
                            }
                        }

                        oldMn.name = methodList.get((methodList.indexOf(oldMn.name)) & (~1)) + "_foamfix_old";
                        nodeData.methods.add(oldMn);
                        added = true;
                        break;
                    }
                }

                if (!added && addMethods) {
                    BugfixModClassTransformer.instance.logger.info("Added METHOD: " + targetClassName + "." + mn.name);
                    nodeData.methods.add(mn);
                    added = true;
                }
                addedGlobal |= added; // TODO: count occurences
            }
        }

        for (int i = 0; i < nodeSplice.fields.size(); i++) {
            if (methodSet.contains(nodeSplice.fields.get(i).name)) {
                FieldNode mn = nodeSplice.fields.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.fields.size(); j++) {
                    if (nodeData.fields.get(j).name.equals(mn.name)
                            && nodeData.fields.get(j).desc.equals(mn.desc)) {
                        BugfixModClassTransformer.instance.logger.info("Spliced in FIELD: " + targetClassName + "." + mn.name);
                        nodeData.fields.set(j, mn);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    BugfixModClassTransformer.instance.logger.info("Added FIELD: " + targetClassName + "." + mn.name);
                    nodeData.fields.add(mn);
                    added = true;
                }
                addedGlobal |= added; // TODO: count occurences
            }
        }

        return addedGlobal;
    }

    public static ClassNode replaceClasses(final ClassNode data, final String className) {
        try {
            final byte[] dataSplice = getClassBytes(className);
            return replaceClasses(data, dataSplice, className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassNode replaceClasses(final ClassNode nodeData, final byte[] dataSplice, final String className) {
        BugfixModClassTransformer.instance.logger.info("Replacing " + nodeData.name + " with " + className);
        if (dataSplice == null) {
            throw new RuntimeException("Class " + className + " not found! This is a FoamFix bug!");
        }

        final ClassReader readerSplice = new ClassReader(dataSplice);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = nodeData.name;
        final String targetClassName = targetClassName2.replace('/', '.');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };

        ClassNode nodeSplice = new ClassNode();
        readerSplice.accept(new RemappingClassAdapter(nodeSplice, remapper), ClassReader.EXPAND_FRAMES);
        return nodeSplice;
    }
}
