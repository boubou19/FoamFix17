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
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.bugfixmod.coremod.MappingRegistry;

import java.util.Iterator;

/**
 * Created by Vincent on 7/15/2014.
 *
 * This makes the heart delta flash and regen blink fixes work together by assigning some fields.
 */
public class HeartFlashFixCompatPatcher extends AbstractPatcher {

    public HeartFlashFixCompatPatcher(String name, String targetClassName, String targetMethodName, String targetMethodDesc) {
        super(name, targetClassName, targetMethodName, targetMethodDesc);
    }

    @Override
    public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
        if (currentInstruction.getOpcode() == Opcodes.GETFIELD && currentInstruction.getPrevious().getOpcode() == Opcodes.ALOAD) {
            printMessage("Found entry point");
            String ecpmpClassName = "net/minecraft/client/entity/EntityClientPlayerMP";
            String getHealthMethodName = MappingRegistry.getMethodNameFor("EntityClientPlayerMP.getHealth");
            String prevHealthFieldName = MappingRegistry.getFieldNameFor("EntityClientPlayerMP.prevHealth");

            InsnList toInject = new InsnList();
            toInject.add(new VarInsnNode(Opcodes.FLOAD, 1));
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ecpmpClassName, getHealthMethodName, "()F"));
            InsnNode insertPoint = new InsnNode(Opcodes.FCMPL);
            toInject.add(insertPoint);
            // 1
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ecpmpClassName, getHealthMethodName, "()F"));
            toInject.add(new FieldInsnNode(Opcodes.PUTFIELD, ecpmpClassName, prevHealthFieldName, "F"));
            LabelNode label = new LabelNode();
            toInject.add(label);

            /* 1 */ toInject.insert(insertPoint, new JumpInsnNode(Opcodes.IFLE, label));

            successful = true;
            return toInject;
        }

        return null;
    }
}
