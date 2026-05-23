package com.elmakers.mine.bukkit.plugins.liminal.rp;

import java.util.List;

public interface ResourcePackResponse {
    void finished(boolean success, boolean hasModifiedTime, List<String> responses, ResourcePack pack);
}
