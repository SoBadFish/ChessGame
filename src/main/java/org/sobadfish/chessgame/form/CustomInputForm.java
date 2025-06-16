package org.sobadfish.chessgame.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomInputForm implements ICustomForm<FormResponseCustom>{

    public int formId;

    public String title;

    public Player playerInfo;


    public List<Element> elements = new ArrayList<>();

    public CustomInputForm(String title, Player playerInfo) {
        this.title = title;
        this.playerInfo = playerInfo;


    }

    public void addElement(Element element) {
        elements.add(element);
    }

    @Override
    public FormWindow asWindows() {
        FormWindowCustom custom = new FormWindowCustom(title);
        for (Element element : elements) {
            custom.addElement(element);
        }
        return custom;
    }

    public abstract void callback(FormResponseCustom response);

    public void callbackData(FormResponse response) {
        if(response instanceof FormResponseCustom) {
            callback((FormResponseCustom) response);
        }
    }


    @Override
    public void setFormId(int formId) {
        this.formId = formId;
    }

    @Override
    public int getFormId() {
        return formId;
    }

    @Override
    public Player getPlayerInfo() {
        return playerInfo;
    }
}
