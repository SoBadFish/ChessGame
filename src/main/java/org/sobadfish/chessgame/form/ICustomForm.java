package org.sobadfish.chessgame.form;

import cn.nukkit.Player;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.item.Item;

public interface ICustomForm<T extends FormResponse> {

    void callbackData(FormResponse response);

    int getFormId();

    /**
     * 创建表单
     * */
    void onCreateView();

    void setFormId(int formId);

    FormWindow asWindows();


    Player getPlayerInfo();


    boolean isCanRemove();

}
