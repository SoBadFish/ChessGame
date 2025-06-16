package org.sobadfish.chessgame.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomButtonForm implements ICustomForm<FormResponseSimple> {

    public int formId;

    public String title;


    public String content;

    public List<ElementButton> elements = new ArrayList<>();

    public Player playerInfo;

    public CustomButtonForm(String title, String content, Player playerInfo) {
        this.title = title;
        this.content = content;
        this.playerInfo = playerInfo;


    }



    public void addButton(ElementButton element) {
        elements.add(element);
    }

    public abstract void callback(FormResponseSimple response);

    public void callbackData(FormResponse response) {
        if (response instanceof FormResponseSimple) {
            callback((FormResponseSimple) response);
        }
    }


    @Override
    public Player getPlayerInfo() {
        return playerInfo;
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
    public FormWindow asWindows() {
        FormWindowSimple simple = new FormWindowSimple(title, content);
        for (ElementButton element : elements) {
            simple.addButton(element);
        }
        return simple;
    }

    public void setContent(String content) {
        this.content = content;
    }
}