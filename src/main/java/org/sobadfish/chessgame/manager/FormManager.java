package org.sobadfish.chessgame.manager;

import cn.nukkit.Player;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.utils.Utils;
import org.sobadfish.chessgame.form.ICustomForm;

import java.util.LinkedHashMap;

public class FormManager {

    private final LinkedHashMap<String, ICustomForm<? extends FormResponse>> fromObj = new LinkedHashMap<>();

    
    /**
     * 获取指定玩家的表单
     * @param playerName 玩家名称
     * @return 对应的表单对象，如果不存在则返回null
     */
    public ICustomForm<? extends FormResponse> getFrom(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return null;
        }
        return fromObj.get(playerName);
    }

    /**
     * 添加表单到管理器
     * @param player 玩家对象，不能为null
     * @param form 要添加的表单对象，不能为null
     */
    public void addForm(Player player, ICustomForm<?> form) {
        if (player == null || form == null || !player.isOnline()) {
            return;
        }
        form.onCreateView();
        String playerName = player.getName();
        fromObj.put(playerName, form);
        int size = player.formWindows.size()+ Utils.rand(1,100);
        form.setFormId(size);
        player.showFormWindow(form.asWindows(),size);

    }

    /**
     * 移除指定玩家的表单
     *
     * @param playerName 玩家名称
     */
    public void removeForm(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }
        fromObj.remove(playerName);
    }

    /**
     * 清空所有表单
     */
    public void clearForms() {
        fromObj.clear();
    }
}
