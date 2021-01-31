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

package pl.asie.foamfix.forkage.coremod.patchers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.bugfixmod.coremod.patchers.AbstractPatcher;

import java.util.Iterator;
import java.util.ListIterator;

public class SoundSystemUnpauseFixPatcher extends AbstractPatcher {
    public SoundSystemUnpauseFixPatcher(String name, String targetClassName) {
        super(name, targetClassName, "", "");
    }

    @Override
    public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
        return null;
    }

    @Override
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void patchClassNode(ClassNode classNode) {
        classNode.fields.add(new FieldNode(
                Opcodes.ACC_PRIVATE,
                "foamFix_pausedChannels",
                "Ljava/util/Set;", null, null
        ));

        int patches = 0;

        for (MethodNode methodNode : classNode.methods) {
            if ("<init>".equals(methodNode.name)) {
                ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node.getOpcode() == Opcodes.RETURN) {
                        it.previous();
                        it.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        it.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/google/common/collect/Sets",
                                "newHashSet",
                                "()Ljava/util/HashSet;",
                                false
                        ));
                        it.add(new FieldInsnNode(
                                Opcodes.PUTFIELD,
                                classNode.name,
                                "foamFix_pausedChannels",
                                "Ljava/util/Set;"
                        ));
                        it.next();
                        printMessage("Patch 1/5 applied!");
                        patches |= (1 << 0);
                    }
                }
            }

            if ("func_148614_c".equals(methodNode.name) || "stopAllSounds".equals(methodNode.name)) {
                ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node.getOpcode() == Opcodes.INVOKEINTERFACE
                        && node instanceof MethodInsnNode
                        && ((MethodInsnNode) node).owner.equals("java/util/Map")
                        && ((MethodInsnNode) node).name.equals("clear")
                    ) {
                        it.previous();
                        it.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        it.add(new FieldInsnNode(
                                Opcodes.GETFIELD,
                                classNode.name,
                                "foamFix_pausedChannels",
                                "Ljava/util/Set;"
                        ));
                        it.add(new MethodInsnNode(
                                Opcodes.INVOKEINTERFACE,
                                "java/util/Set",
                                "clear",
                                "()V",
                                true
                        ));
                        it.next();
                        printMessage("Patch 2/5 applied!");
                        patches |= (1 << 1);
                        break;
                    }
                }
            }

            if ("func_148610_e".equals(methodNode.name) || "pauseAllSounds".equals(methodNode.name)) {
                ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && node instanceof MethodInsnNode
                            && ((MethodInsnNode) node).owner.equals("net/minecraft/client/audio/SoundManager$SoundSystemStarterThread")
                            && ((MethodInsnNode) node).name.equals("pause")
                    ) {
                        it.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        it.add(new FieldInsnNode(
                                Opcodes.GETFIELD,
                                classNode.name,
                                "foamFix_pausedChannels",
                                "Ljava/util/Set;"
                        ));
                        it.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        it.add(new MethodInsnNode(
                                Opcodes.INVOKEINTERFACE,
                                "java/util/Set",
                                "add",
                                "(Ljava/lang/Object;)Z",
                                true
                        ));
                        it.add(new InsnNode(Opcodes.POP));
                        printMessage("Patch 3/5 applied!");
                        patches |= (1 << 2);
                    }
                }
            }

            if ("func_148604_f".equals(methodNode.name) || "resumeAllSounds".equals(methodNode.name)) {
                ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode node = it.next();
                    if (node.getOpcode() == Opcodes.GETFIELD
                        && node instanceof FieldInsnNode
                        && ((FieldInsnNode) node).owner.equals(classNode.name)
                        && (((FieldInsnNode) node).name.equals("playingSounds") || ((FieldInsnNode) node).name.equals("field_148629_h"))
                    ) {
                        ((FieldInsnNode) node).name = "foamFix_pausedChannels";
                        ((FieldInsnNode) node).desc = "Ljava/util/Set;";
                        it.next();
                        it.remove();
                        printMessage("Patch 4/5 applied!");
                        patches |= (1 << 3);
                    }

                    if (node.getOpcode() == Opcodes.RETURN) {
                        it.previous();
                        it.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        it.add(new FieldInsnNode(
                                Opcodes.GETFIELD,
                                classNode.name,
                                "foamFix_pausedChannels",
                                "Ljava/util/Set;"
                        ));
                        it.add(new MethodInsnNode(
                                Opcodes.INVOKEINTERFACE,
                                "java/util/Set",
                                "clear",
                                "()V",
                                true
                        ));
                        it.next();
                        printMessage("Patch 5/5 applied!");
                        patches |= (1 << 4);
                    }
                }
            }
        }

        successful = (patches == 0x1F);
    }
}
