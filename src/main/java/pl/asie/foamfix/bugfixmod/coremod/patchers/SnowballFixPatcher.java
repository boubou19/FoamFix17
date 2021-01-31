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

package pl.asie.foamfix.bugfixmod.coremod.patchers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;

/**
 * Created by Vincent on 6/6/2014.
 *
 * This fix makes projectiles knockback players again by removing a catch check for 0 damage events
 */
public class SnowballFixPatcher extends AbstractPatcher implements GlobalModificationPatcher {

    public SnowballFixPatcher(String name, String targetClassName, String targetMethodName, String targetMethodDesc) {
        super(name, targetClassName, targetMethodName, targetMethodDesc);
    }

    @Override
    public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
        return null;
    }

    @Override
    public void modifyInsnsGlobal(InsnList instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i).getOpcode() == Opcodes.IFNE && instructions.get(i).getPrevious().getOpcode() == Opcodes.FCMPL) {
                if (instructions.get(i + 3).getOpcode() == Opcodes.ICONST_0) {
                    instructions.remove(instructions.get(i).getNext().getNext().getNext().getNext());
                    instructions.remove(instructions.get(i).getNext().getNext().getNext());
                    printMessage("Removed instructions");
                    successful = true;
                    //Remove instruction to return without action when damagesource health is 0. In other words, being dealt 0 damage shows everything associated with damage.
                    //Including gui tilt, heart flash, sound, and, most importantly, knockback. This was behavior in older versions but was somehow removed in more recent years.
                }
            }
        }
    }
}
