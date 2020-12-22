package pl.asie.foamfix.bugfixmod.coremod.patchers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.asie.foamfix.bugfixmod.coremod.MappingRegistry;

import java.util.Iterator;

/**
 * Created by Vincent on 6/6/2014.
 *
 * This makes hearts flash the health "delta" after damage by assigning an unused clientside field.
 */
public class HeartFlashFixPatcher extends AbstractPatcher {

    public HeartFlashFixPatcher(String name, String targetClassName, String targetMethodName, String targetMethodDesc) {
        super(name, targetClassName, targetMethodName, targetMethodDesc);
    }

    @Override
    public InsnList buildNewInsns(AbstractInsnNode currentInstruction, Iterator<AbstractInsnNode> instructionSet) {
        if (currentInstruction.getOpcode() == Opcodes.ICONST_0) {
            printMessage("Found entry point");
            String ecpmpClassName = "net/minecraft/client/entity/EntityClientPlayerMP";
            String getHealthMethodName = MappingRegistry.getMethodNameFor("EntityClientPlayerMP.getHealth");
            String prevHealthFieldName = MappingRegistry.getFieldNameFor("EntityClientPlayerMP.prevHealth");

            InsnList toInject = new InsnList();
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                    ecpmpClassName,
                    getHealthMethodName,
                    "()F"));
            toInject.add(new VarInsnNode(Opcodes.FLOAD, 2));
            toInject.add(new InsnNode(Opcodes.FADD));
            toInject.add(new FieldInsnNode(Opcodes.PUTFIELD,
                    ecpmpClassName,
                    prevHealthFieldName,
                    "F"));
            successful = true;
            return toInject;
        }
        return null;
    }
}
