package org.trivait.minigamesmod.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import org.trivait.minigamesmod.api.MinigameDefinition;
import org.trivait.minigamesmod.api.MinigameRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Config(name = "minigames/layout")
public class MinigameLayoutConfig implements ConfigData {
    public List<String> order = new ArrayList<>();
    public List<String> hidden = new ArrayList<>();

    public static MinigameLayoutConfig get() {
        return AutoConfig.getConfigHolder(MinigameLayoutConfig.class).getConfig();
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(MinigameLayoutConfig.class).save();
    }

    public boolean isVisible(String id) {
        return !hidden.contains(id);
    }

    public void hide(String id) {
        if (!hidden.contains(id)) {
            hidden.add(id);
            saveConfig();
        }
    }

    public void show(String id) {
        if (hidden.remove(id)) {
            saveConfig();
        }
    }

    public void sync() {
        Set<String> known = new HashSet<>(order);
        boolean changed = false;
        for (MinigameDefinition def : MinigameRegistry.getAll()) {
            if (!known.contains(def.getId())) {
                order.add(def.getId());
                known.add(def.getId());
                changed = true;
            }
        }
        if (changed) {
            saveConfig();
        }
    }

    public List<MinigameDefinition> getVisibleList() {
        sync();
        List<MinigameDefinition> result = new ArrayList<>();
        for (String id : order) {
            if (!hidden.contains(id)) {
                MinigameDefinition def = MinigameRegistry.get(id);
                if (def != null) {
                    result.add(def);
                }
            }
        }
        return result;
    }

    public List<MinigameDefinition> getHiddenList() {
        sync();
        List<MinigameDefinition> result = new ArrayList<>();
        for (String id : order) {
            if (hidden.contains(id)) {
                MinigameDefinition def = MinigameRegistry.get(id);
                if (def != null) {
                    result.add(def);
                }
            }
        }
        return result;
    }

    public void moveVisible(int fromIndex, int toIndex) {
        List<MinigameDefinition> visible = getVisibleList();
        if (fromIndex < 0 || fromIndex >= visible.size() || toIndex < 0 || toIndex >= visible.size() || fromIndex == toIndex) {
            return;
        }
        String fromId = visible.get(fromIndex).getId();
        String toId = visible.get(toIndex).getId();
        order.remove(fromId);
        int targetPos = order.indexOf(toId);
        if (toIndex > fromIndex) {
            targetPos++;
        }
        if (targetPos < 0 || targetPos > order.size()) {
            order.add(fromId);
        } else {
            order.add(targetPos, fromId);
        }
        saveConfig();
    }

    public void moveHidden(int fromIndex, int toIndex) {
        List<MinigameDefinition> hiddenList = getHiddenList();
        if (fromIndex < 0 || fromIndex >= hiddenList.size() || toIndex < 0 || toIndex >= hiddenList.size() || fromIndex == toIndex) {
            return;
        }
        String fromId = hiddenList.get(fromIndex).getId();
        String toId = hiddenList.get(toIndex).getId();
        order.remove(fromId);
        int targetPos = order.indexOf(toId);
        if (toIndex > fromIndex) {
            targetPos++;
        }
        if (targetPos < 0 || targetPos > order.size()) {
            order.add(fromId);
        } else {
            order.add(targetPos, fromId);
        }
        saveConfig();
    }

    public void moveToHidden(String id, int targetIndex) {
        if (!hidden.contains(id)) {
            hidden.add(id);
        }
        List<MinigameDefinition> hiddenList = getHiddenList();
        order.remove(id);
        if (targetIndex >= 0 && targetIndex < hiddenList.size()) {
            String toId = hiddenList.get(targetIndex).getId();
            int pos = order.indexOf(toId);
            if (pos >= 0) {
                order.add(pos, id);
            } else {
                order.add(id);
            }
        } else {
            order.add(id);
        }
        saveConfig();
    }

    public void moveToVisible(String id, int targetIndex) {
        hidden.remove(id);
        List<MinigameDefinition> visibleList = getVisibleList();
        order.remove(id);
        if (targetIndex >= 0 && targetIndex < visibleList.size()) {
            String toId = visibleList.get(targetIndex).getId();
            int pos = order.indexOf(toId);
            if (pos >= 0) {
                order.add(pos, id);
            } else {
                order.add(id);
            }
        } else {
            order.add(id);
        }
        saveConfig();
    }
}
