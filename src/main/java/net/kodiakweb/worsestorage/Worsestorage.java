package net.kodiakweb.worsestorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
//import net.kodiakweb.worsestorage.blocks.*;
import net.kodiakweb.worsestorage.blocks.TypeBundle;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worsestorage implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("worsestorage");
    public static final Item TYPE_BUNDLE = new TypeBundle(new FabricItemSettings());
    public static final java.lang.String MOD_ID = "worsestorage";
    @Override
    public void onInitialize() {

        Registry.register(Registries.ITEM, new Identifier(MOD_ID,"typebundle"), TYPE_BUNDLE);
    }

}

