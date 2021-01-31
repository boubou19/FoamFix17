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

package pl.asie.foamfix.coremod.patchers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import pl.asie.foamfix.bugfixmod.coremod.patchers.AbstractPatcher;

import java.util.Iterator;

public class GhostBusterHookPatcher extends AbstractPatcher {
    private boolean applied;

    public GhostBusterHookPatcher(String name, String targetClassName, String targetMethodName, String targetMethodDesc) {
        super(name, targetClassName, targetMethodName, targetMethodDesc);
    }

    @Override
    public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
        if (applied) {
            return null;
        }

        InsnList toInject = new InsnList();

        printMessage("Found entry point");
        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
        toInject.add(new VarInsnNode(Opcodes.ILOAD, 1));
        toInject.add(new VarInsnNode(Opcodes.ILOAD, 2));
        toInject.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "pl/asie/foamfix/ghostbuster/GhostBusterLogger",
                "onProvideChunk",
                "(Lnet/minecraft/world/gen/ChunkProviderServer;II)V",
                false
        ));
        successful = true;
        applied = true;

        return toInject;
    }
}
