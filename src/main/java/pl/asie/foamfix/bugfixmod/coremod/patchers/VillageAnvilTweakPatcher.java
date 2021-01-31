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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import pl.asie.foamfix.bugfixmod.coremod.MappingRegistry;

import java.util.Iterator;

/**
 * Created by Vincent on 6/6/2014.
 *
 * This tweak replaces the double stone slab in a blacksmith house with an anvil.
 */
public class VillageAnvilTweakPatcher extends AbstractPatcher implements ModificationPatcher {

    public VillageAnvilTweakPatcher(String name, String targetClassName, String targetMethodName, String targetMethodDesc) {
        super(name, targetClassName, targetMethodName, targetMethodDesc);
    }

    @Override
    public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
        return null;
    }

    @Override
    public void modifyInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet, InsnList instructions) {
        if (currentInstruction instanceof FieldInsnNode) {
            FieldInsnNode f = (FieldInsnNode) currentInstruction;
            String doubleSlabFieldName = MappingRegistry.getFieldNameFor("Blocks.double_stone_slab");

            if (f.name.equals(doubleSlabFieldName)) {
                printMessage("Found entry point: " + f.owner + " " + f.name + " " + f.desc);
                String blocksClassName = ("net/minecraft/init/Blocks");
                String anvilFieldName = MappingRegistry.getFieldNameFor("Blocks.anvil");
                String blockClassName = ("net/minecraft/block/Block");

                instructions.insert(currentInstruction, new FieldInsnNode(Opcodes.GETSTATIC,
                        blocksClassName,
                        anvilFieldName,
                        "L" + blockClassName + ";"
                ));
                printMessage("Replaced double stone slab with anvil.");
                instructions.remove(currentInstruction);
                successful = true;
            }
        }
    }
}
