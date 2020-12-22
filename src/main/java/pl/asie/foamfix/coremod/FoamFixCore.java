package pl.asie.foamfix.coremod;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import pl.asie.foamfix.bugfixmod.coremod.BugfixModClassTransformer;

import java.io.File;
import java.util.Map;

@IFMLLoadingPlugin.Name("Do not report to Forge! (If you haven't disabled the FoamFix coremod, try disabling it in the config! Note that this bit of text will still appear.)")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"pl.asie.foamfix"})
public class FoamFixCore implements IFMLLoadingPlugin, IFMLCallHook {
    public String[] getASMTransformerClass() {
        return new String[]{BugfixModClassTransformer.class.getName()};
    }

    public String getModContainerClass() {
        return FoamFixCoreContainer.class.getName();
    }

    public String getSetupClass() {
        return FoamFixCore.class.getName();
    }

    public void injectData(Map<String, Object> data) {
        BugfixModClassTransformer.instance.settingsFile = new File(((File) data.get("mcLocation")).getPath() + "/config/foamfix.cfg");
        BugfixModClassTransformer.instance.initialize((Boolean) data.get("runtimeDeobfuscationEnabled"));
    }

    public String getAccessTransformerClass() {
        return null;
    }

    public Void call() {
        return null;
    }
}
