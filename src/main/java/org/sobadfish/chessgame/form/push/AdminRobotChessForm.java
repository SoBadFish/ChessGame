package org.sobadfish.chessgame.form.push;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.item.Item;
import org.sobadfish.chessgame.entitys.ChessPanEntity;
import org.sobadfish.chessgame.form.CustomInputForm;

import java.util.ArrayList;
import java.util.List;

public class AdminRobotChessForm extends CustomInputForm {


    public ChessPanEntity chessPanEntity;



    public AdminRobotChessForm(String title, Player playerInfo, ChessPanEntity chessPanEntity) {
        super(title, playerInfo);
        this.chessPanEntity = chessPanEntity;
    }

    @Override
    public void callback(FormResponseCustom response) {
        boolean e1 = response.getToggleResponse(0);
        int e2 = response.getStepSliderResponse(1).getElementID();
        if(e1 && !chessPanEntity.isAIMatch()){
            chessPanEntity.startAIMatch(ChessPanEntity.AIDifficulty.values()[e2]);
        }else{
            if(e1){
                chessPanEntity.setAIDifficulty(ChessPanEntity.AIDifficulty.values()[e2]);
            }else{
                chessPanEntity.stopAIMatch();
            }
        }

    }

    @Override
    public void onCreateView() {
        addElement(new ElementToggle("是否开启AI", chessPanEntity.isAIMatch()));
        List<String> nd = new ArrayList<>();
        nd.add("简单");
        nd.add("中等");
        nd.add("困难");
        addElement(new ElementStepSlider("AI难度",nd));

    }

    @Override
    public boolean isCanRemove() {
        return true;
    }
}
