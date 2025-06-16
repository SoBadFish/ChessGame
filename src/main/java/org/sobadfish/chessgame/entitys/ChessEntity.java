package org.sobadfish.chessgame.entitys;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

public class ChessEntity extends Entity implements CustomEntity {

    public Player chosePlayer;

    public int type;

    public boolean hasChose = false;

    public ChessPanEntity panEntity;

    public int pan_index;

    public boolean isDie;




    public ChessEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setScale(0.2f);
        type = nbt.getInt("chess_type");

    }



    public void setPanIndex(int pan_index) {
        this.pan_index = pan_index;
    }

    @Override
    public void saveNBT() {
    }

    public void setPanEntity(ChessPanEntity panEntity) {
        this.panEntity = panEntity;
    }

    @Override
    public float getWidth() {
        return 0.2f;
    }

    @Override
    public float getHeight() {
        return 0.2f;
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return ChessEntityManager.DEF_CHESS_ENTITY[type];
    }


    @Override
    public int getNetworkId() {
        return getEntityDefinition().getRuntimeId();
    }

    /**
     * 选中当前棋子
     * */
    public void setChose(Player chosePlayer){
        if(isDie){
            System.out.println("拒绝1");
            return;
        }

        if(panEntity != null && panEntity.choseIndexEntity != null && !panEntity.choseIndexEntity.equals(this)){
            System.out.println("拒绝2");
            return;
        }


        if(panEntity != null && !hasChose){
            if(panEntity.isEnd){
                System.out.println("拒绝3");
                return;
            }
            if(panEntity.isAIMatch()){
                if(!panEntity.isRedRun){
                    chosePlayer.sendTitle(TextFormat.colorize('&',"请等待AI执行完毕"));
                    return;
                }
            }
            if(panEntity.isRedRun && type >= 7 || !panEntity.isRedRun && type < 7){
                this.chosePlayer = chosePlayer;
                this.hasChose = true;
                panEntity.choseChessIndex(pan_index,chosePlayer,type < 7);
                //生成一个选中效果
                panEntity.choseEntity = new ChessChoseEntity(this.getChunk(), Entity.getDefaultNBT(
                        this
                ));
                panEntity.choseEntity.spawnToAll();

                return;
            }else{
                chosePlayer.sendTitle("还不到你走");
            }

        }
        if(hasChose){
            if(panEntity != null){

                if(!panEntity.chessToIndex(pan_index)){
                    ChessPanEntity.ChassPoint point = panEntity.CAN_PLACES.get(panEntity.choseIndex);
                    this.teleport(panEntity.getPosition().add(point.x, 0.1, point.z));
                    this.pan_index = panEntity.choseIndex;
                }else{
                    panEntity.isRedRun = !panEntity.isRedRun;

                    //如果开启AI
                    if(panEntity.isAIMatch()){
                        panEntity.goAI();
                    }
                }
                panEntity.choseIndexEntity = null;
                if(panEntity.choseEntity != null){
                    panEntity.choseEntity.close();
                }

            }
            hasChose = false;

        }

    }


    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        } else {
            if(hasChose){
                if(chosePlayer != null && chosePlayer.isAlive() && chosePlayer.distance(panEntity) < 5){
                    // 增强版视角跟随算法
                    Vector3 eyePos = chosePlayer.getPosition().add(0, chosePlayer.getEyeHeight(), 0);
                    Vector3 lookDir = chosePlayer.getDirectionVector();

                    // 计算视线与棋盘平面的交点
                    double t = (panEntity.y + 0.15 - eyePos.y) / lookDir.y;
                    Vector3 intersectPoint = eyePos.add(lookDir.multiply(t));

                    // 平滑移动参数
                    float moveSpeed = 0.5f; // 移动速度系数
                    float maxDistance = 1.5f; // 最大跟随距离

                    // 计算目标位置
                    Vector3 targetPos = new Vector3(
                        intersectPoint.x,
                        panEntity.y + 0.15,
                        intersectPoint.z
                    );

                    // 限制移动范围
                    double dx = targetPos.x - panEntity.x;
                    double dz = targetPos.z - panEntity.z;
                    double distance = Math.sqrt(dx*dx + dz*dz);
                    if(distance > maxDistance) {
                        double ratio = maxDistance / distance;
                        targetPos.x = panEntity.x + dx * ratio;
                        targetPos.z = panEntity.z + dz * ratio;
                    }

                    // 应用平滑移动
                    Vector3 currentPos = this.getPosition();
                    Vector3 moveVector = targetPos.subtract(currentPos).multiply(moveSpeed);
                    this.teleport(currentPos.add(moveVector));


                    // 实时更新棋子位置索引
                    updatePositionIndex();
                }else{
                    hasChose = false;
                    ChessPanEntity.ChassPoint point = panEntity.CAN_PLACES.get(this.pan_index);
                    // 平滑回到原位
                    Vector3 targetPos = panEntity.getPosition().add(point.x, 0.1, point.z);
                    Vector3 currentPos = this.getPosition();
                    if(currentPos.distanceSquared(targetPos) > 0.01) {
                        Vector3 moveVector = targetPos.subtract(currentPos).multiply(0.3);
                        this.teleport(currentPos.add(moveVector));
                    } else {
                        this.teleport(targetPos);
                    }

                    if(panEntity.choseEntity != null){
                        panEntity.choseEntity.close();
                        panEntity.choseEntity = null;
                    }
                }
            }
        }
        return super.onUpdate(currentTick);
    }

    /**
     * 更新棋子位置索引
     */
    private void updatePositionIndex() {
        int closestIndex = -1;
        double minDist = Double.MAX_VALUE;

        for(int i = 0; i < 90; i++) {
            if(panEntity.CAN_PLACES.containsKey(i)) {
                ChessPanEntity.ChassPoint point = panEntity.CAN_PLACES.get(i);
                Vector3 boardPos = panEntity.getPosition().add(point.x, 0.1, point.z);
                double dist = this.getPosition().distanceSquared(boardPos);
                if(dist < minDist) {
                    minDist = dist;
                    closestIndex = i;
                }
            }
        }

        if(closestIndex != -1) {
            ChessPanEntity.ChassPoint point = panEntity.CAN_PLACES.get(closestIndex);
            this.pan_index = closestIndex;
            // 更新选中效果位置
            if(panEntity.choseEntity != null) {
                panEntity.choseEntity.teleport(new Vector3(panEntity.x + point.x, panEntity.y + 0.1, panEntity.z + point.z));
            }

        }
    }

    public boolean attack(EntityDamageEvent source) {
        EntityDamageEvent.DamageCause cause = source.getCause();
        return cause == EntityDamageEvent.DamageCause.VOID;
    }


}
