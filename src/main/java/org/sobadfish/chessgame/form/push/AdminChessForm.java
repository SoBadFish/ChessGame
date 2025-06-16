package org.sobadfish.chessgame.form.push;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.response.FormResponseSimple;
import org.sobadfish.chessgame.ChessGameMainClass;
import org.sobadfish.chessgame.entitys.ChessPanEntity;
import org.sobadfish.chessgame.form.CustomButtonForm;

public class AdminChessForm extends CustomButtonForm {

    public ChessPanEntity chessPanEntity;

    boolean isCanClose = true;


    public AdminChessForm(Player playerInfo,ChessPanEntity chessPanEntity) {
        super("棋盘设置", "", playerInfo);
        this.chessPanEntity = chessPanEntity;
    }

    @Override
    public void callback(FormResponseSimple response) {
        if(chessPanEntity == null){
            return;
        }
        if(response.getClickedButtonId() == 0){
            chessPanEntity.resetChess();
        }
        if(response.getClickedButtonId() == 1){
            isCanClose = false;
            AdminRobotChessForm adminRobotChessForm = new AdminRobotChessForm("人机设置",playerInfo,chessPanEntity);
            ChessGameMainClass.getInstance().formManager.addForm(playerInfo,adminRobotChessForm);
        }
        if(response.getClickedButtonId() == 2){
            chessPanEntity.close();
        }
    }

    @Override
    public void onCreateView() {
        addButton(new ElementButton("重置棋盘",new ElementButtonImageData("path","textures/entity/qipan")));
        addButton(new ElementButton("人机设置",new ElementButtonImageData("path","textures/ui/FriendsIcon")));
        if(playerInfo.isOp()) {
            addButton(new ElementButton("移除棋盘", new ElementButtonImageData("path", "textures/ui/crossout")));
        }
    }

    @Override
    public boolean isCanRemove() {
        return isCanClose;
    }
}
