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
