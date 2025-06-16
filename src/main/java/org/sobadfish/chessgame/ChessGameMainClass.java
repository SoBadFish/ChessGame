package org.sobadfish.chessgame;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import org.sobadfish.chessgame.entitys.ChessEntity;
import org.sobadfish.chessgame.entitys.ChessEntityManager;
import org.sobadfish.chessgame.entitys.ChessPanEntity;
import org.sobadfish.chessgame.form.ICustomForm;
import org.sobadfish.chessgame.form.push.AdminChessForm;
import org.sobadfish.chessgame.manager.FormManager;

public class ChessGameMainClass extends PluginBase implements Listener {

    public static ChessGameMainClass instance;

    public FormManager formManager;
    @Override
    public void onLoad() {
        super.onLoad();
        for(EntityDefinition definition : ChessEntityManager.DEF_CHESS_ENTITY){
            EntityManager.get().registerDefinition(definition);
        }
        EntityManager.get().registerDefinition(ChessEntityManager.DEF_QI_PAN);
        EntityManager.get().registerDefinition(ChessEntityManager.DEF_CHOSE);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("象棋游戏启动中 @author Sobadfish");
        this.getServer().getPluginManager().registerEvents(this, this);
        if(formManager == null){
            formManager = new FormManager();
        }else{
            formManager.clearForms();
        }
        this.getServer().getCommandMap().register("chessgame", new Command("cg") {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                if(sender instanceof Player player && player.isOp()) {
                    //生成棋盘与棋子
                    CompoundTag tag =  Entity.getDefaultNBT(player.getPosition());
                    tag.putInt("place_face",player.getDirection().getIndex());
                    ChessPanEntity chessEntity = new ChessPanEntity(player.chunk,tag);
                    chessEntity.spawnToAll();
                    chessEntity.initChess();
                    sender.sendMessage("成功");

                }

               return true;
            }
        });
        this.getLogger().info("象棋游戏启动完成!");

    }

    public static ChessGameMainClass getInstance() {
        return instance;
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        if(event.getEntity() instanceof ChessEntity entity){
            entity.setChose(event.getPlayer());
        }
        if(event.getEntity() instanceof ChessPanEntity entity){
            if(event.getPlayer().isSneaking()){
                //打开面板
                AdminChessForm adminChessForm = new AdminChessForm(event.getPlayer(),entity);
                formManager.addForm(event.getPlayer(),adminChessForm);

            }
        }
    }

    @EventHandler
    public void onFormListener(PlayerFormRespondedEvent event){
        if (event.wasClosed()) {
            return;
        }
        Player player = event.getPlayer();
        ICustomForm<? extends FormResponse> customForm = formManager.getFrom(player.getName());
        if(!player.isOnline()){
            return;
        }
        if(customForm != null && event.getFormID() == customForm.getFormId()){
            FormResponse response = event.getResponse();
            if(response != null) {
                try {
                    customForm.callbackData(response);
                } catch (ClassCastException e) {
                    System.err.println("表单响应类型不匹配: " + e.getMessage());
                }
                if(customForm.isCanRemove()){
                    formManager.removeForm(player.getName());
                }
//
            }
        }

    }
}
