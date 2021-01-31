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
import pl.asie.foamfix.bugfixmod.coremod.MappingRegistry;
import pl.asie.foamfix.bugfixmod.coremod.patchers.AbstractPatcher;
import pl.asie.foamfix.bugfixmod.coremod.patchers.ModificationPatcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// https://github.com/MinecraftForkage/MinecraftForkage/commit/b8b345c19bab9641e62234fec355a4f54ab86681
public class JarDiscovererMemoryLeakFixPatcher extends AbstractPatcher implements ModificationPatcher {
	public JarDiscovererMemoryLeakFixPatcher(String name, String targetClassName, String targetMethodName, String targetMethodDesc) {
		super(name, targetClassName, targetMethodName, targetMethodDesc);
	}

	@Override
	public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
		return null;
	}

	@Override
	public void modifyInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet, InsnList instructions) {
		if (currentInstruction instanceof MethodInsnNode
			&& currentInstruction.getOpcode() == Opcodes.INVOKEVIRTUAL
			&& ((MethodInsnNode) currentInstruction).owner.equals("java/util/jar/JarFile")
			&& ((MethodInsnNode) currentInstruction).name.equals("getInputStream")
		) {
			AbstractInsnNode ins1 = currentInstruction;
			AbstractInsnNode ins2 = instructionSet.next();
			AbstractInsnNode ins3 = instructionSet.next();
			if (ins2 instanceof MethodInsnNode
				&& ins2.getOpcode() == Opcodes.INVOKESPECIAL
				&& ((MethodInsnNode) ins2).owner.equals("cpw/mods/fml/common/discovery/asm/ASMModParser")
				&& ((MethodInsnNode) ins2).name.equals("<init>")
				&& ins3.getOpcode() == Opcodes.ASTORE
			) {
				instructions.insert(ins1, new InsnNode(Opcodes.DUP_X2));
				instructions.insert(ins3, new MethodInsnNode(
						Opcodes.INVOKEVIRTUAL,
						"java/io/InputStream",
						"close",
						"()V",
						false
				));
				successful = true;
			}
		}
	}
}
