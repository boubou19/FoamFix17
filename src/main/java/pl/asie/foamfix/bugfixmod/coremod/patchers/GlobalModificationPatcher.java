package pl.asie.foamfix.bugfixmod.coremod.patchers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;

public interface GlobalModificationPatcher {
    // For patchers that do not just add bytecode
    public abstract void modifyInsnsGlobal(InsnList instructions);
    // Passes instruction list itself so the patcher can modify it at free will.
}
