package net.trashelemental.dracolotl.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.trashelemental.dracolotl.client.gui.DracolotlConfigScreen;

/**
 * Mod Menu 接入实现类。
 * 为模组菜单提供打开 Dracolotl 配置屏幕的入口工厂。
 */
@Environment(EnvType.CLIENT)
public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DracolotlConfigScreen::create;
    }
}
