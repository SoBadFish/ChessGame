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
import org.sobadfish.chessgame.manager.ChessBoardManager;
import org.sobadfish.chessgame.manager.FormManager;
import org.sobadfish.chessgame.manager.ThreadManager;

public class ChessGameMainClass extends PluginBase implements Listener {

    public static ChessGameMainClass instance;

    public FormManager formManager;
    private ChessBoardManager chessBoardManager;
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
        ThreadManager.reset();
        if (chessBoardManager == null) {
            chessBoardManager = new ChessBoardManager();
        }
        scanAndRepairWorldEntities();
        if(formManager == null){
            formManager = new FormManager();
        }else{
            formManager.clearForms();
        }

        this.getServer().getCommandMap().register("chessgame", new Command("cg") {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                if(sender instanceof Player player && player.isOp()) {
                    ChessPanEntity nearbyBoard = chessBoardManager.findNearbyBoard(player.getPosition(), ChessBoardManager.DEFAULT_BOARD_RADIUS);
                    if (nearbyBoard != null) {
                        sender.sendMessage("附近已经有一个棋盘了，请先移除旧棋盘再生成新的。");
                        return true;
                    }
                    int boardCount = chessBoardManager.getBoardCount(player.getLevel());
                    if (boardCount >= ChessBoardManager.DEFAULT_MAX_BOARDS_PER_LEVEL) {
                        sender.sendMessage("当前世界棋盘数量已达到上限，请先清理部分棋盘。");
                        return true;
                    }
                    //生成棋盘与棋子
                    CompoundTag tag =  Entity.getDefaultNBT(player.getPosition());
                    tag.putInt("place_face",player.getDirection().getIndex());
                    ChessPanEntity chessEntity = new ChessPanEntity(player.chunk,tag);
                    chessBoardManager.registerBoard(chessEntity);
                    chessEntity.spawnToAll();
                    chessEntity.initChess();
                    sender.sendMessage("成功生成棋盘");

                }

               return true;
            }
        });
        this.getLogger().info("象棋游戏启动完成!");

    }

    @Override
    public void onDisable() {
        if (formManager != null) {
            formManager.clearForms();
        }
        if (chessBoardManager != null) {
            chessBoardManager.closeAllBoards();
        }
        ThreadManager.shutdown();
    }

    public static ChessGameMainClass getInstance() {
        return instance;
    }

    public ChessBoardManager getChessBoardManager() {
        return chessBoardManager;
    }

    private void scanAndRepairWorldEntities() {
        for (var level : this.getServer().getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof ChessPanEntity board) {
                    chessBoardManager.registerBoard(board);
                    continue;
                }
                if (entity instanceof org.sobadfish.chessgame.entitys.ChessChoseEntity) {
                    entity.close();
                    continue;
                }
                if (entity instanceof ChessEntity chessEntity && chessEntity.panEntity == null) {
                    entity.close();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        if(event.getEntity() instanceof ChessEntity entity){
            if(entity.panEntity == null){
                entity.close();
                return;
            }
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
