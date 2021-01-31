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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import pl.asie.foamfix.bugfixmod.coremod.MappingRegistry;
import pl.asie.foamfix.bugfixmod.coremod.patchers.AbstractPatcher;

import java.util.Iterator;

public class GhostBusterEarlyReturnPatcher extends AbstractPatcher {
	private final int accessPos;
	private final int xyzStartPos;
	private final int radius;
	private final Object returnValue;
	private boolean applied;

	public GhostBusterEarlyReturnPatcher(String targetClassName, String targetMethodName, String targetMethodDesc, int accessPos, int xyzStartPos, int radius, Object returnValue) {
		super("GhostBusterFix", targetClassName, targetMethodName, targetMethodDesc);
		this.accessPos = accessPos;
		this.xyzStartPos = xyzStartPos;
		this.radius = radius;
		this.returnValue = returnValue;
	}

	public static GhostBusterEarlyReturnPatcher updateTick(String targetClassName, int radius) {
		return new GhostBusterEarlyReturnPatcher(
				targetClassName,
				MappingRegistry.getMethodNameFor("Block.updateTick"),
				null,
				1, 2, radius, null
		);
	}

	@Override
	public String getPatcherName() {
		return patcherName + "/" + targetClassName;
	}

	@Override
	public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
		if (applied) {
			return null;
		}

		InsnList list = new InsnList();
		Label l = new Label();
		LabelNode ln = new LabelNode(l);
		list.add(new VarInsnNode(Opcodes.ALOAD, accessPos));
		list.add(new VarInsnNode(Opcodes.ILOAD, xyzStartPos));
		list.add(new VarInsnNode(Opcodes.ILOAD, xyzStartPos + 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, xyzStartPos + 2));
		if (radius == 0) {
			list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"pl/asie/foamfix/ghostbuster/GhostBusterSafeAccessors", "isBlockLoaded",
					"(Lnet/minecraft/world/IBlockAccess;III)Z", false));
		} else {
			switch (radius) {
				case 1:
					list.add(new InsnNode(Opcodes.ICONST_1));
					break;
				case 2:
					list.add(new InsnNode(Opcodes.ICONST_2));
					break;
				case 3:
					list.add(new InsnNode(Opcodes.ICONST_3));
					break;
				case 4:
					list.add(new InsnNode(Opcodes.ICONST_4));
					break;
				case 5:
					list.add(new InsnNode(Opcodes.ICONST_5));
					break;
				default:
					throw new RuntimeException("Invalid ghost buster radius: " + radius);
			}
			list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					"pl/asie/foamfix/ghostbuster/GhostBusterSafeAccessors", "isAreaLoaded",
					"(Lnet/minecraft/world/IBlockAccess;IIII)Z", false));
		}
		list.add(new JumpInsnNode(Opcodes.IFNE, ln));
		if (returnValue != null) {
			if (returnValue instanceof Boolean) {
				list.add(new InsnNode(((boolean) returnValue) ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
				list.add(new InsnNode(Opcodes.IRETURN));
			} else {
				throw new RuntimeException("Invalid ghost buster return value: " + returnValue);
			}
		} else {
			list.add(new InsnNode(Opcodes.RETURN));
		}
		list.add(ln);
		list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

		successful = true;
		applied = true;
		printMessage("Added ghost buster patch (radius = " + radius + ") in " + targetMethodName);
		return list;
	}
}
