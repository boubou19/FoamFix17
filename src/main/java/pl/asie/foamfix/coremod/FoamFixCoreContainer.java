package pl.asie.foamfix.coremod;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class FoamFixCoreContainer extends DummyModContainer {
    public FoamFixCoreContainer() {
        super(new ModMetadata());
        ModMetadata myMeta = super.getMetadata();
        myMeta.authorList = Lists.newArrayList("williewillus", "asie");
        myMeta.modId = "FoamFixCore";
        myMeta.version = "@VERSION@";
        myMeta.name = "FoamFixCore";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}
