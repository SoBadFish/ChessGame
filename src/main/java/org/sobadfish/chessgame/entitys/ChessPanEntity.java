package org.sobadfish.chessgame.entitys;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ChessPanEntity extends Entity implements CustomEntity {

    // AI难度级别
    public enum AIDifficulty {
        EASY,    // 简单：随机走合法步
        MEDIUM,  // 中等：考虑基本吃子
        HARD     // 困难：考虑多步策略
    }

    private AIDifficulty aiDifficulty = AIDifficulty.EASY;
    private boolean vsAI = false;
    private boolean aiIsRed = false;


    public BlockFace placeFace;

    //在这里就可以计算出棋盘中每个点位 然后进行预设 象棋棋盘是 9 * 10的大小 每个点位都有 x z 两个值 所以预设位

    /**
     * 每个点位的象棋棋子
     * */
    public LinkedHashMap<Integer,ChessEntity> chessEntities = new LinkedHashMap<>();

    /**
     * 点位预设遵循左+右减
     * */
    public LinkedHashMap<Integer,ChassPoint> CAN_PLACES = new LinkedHashMap<>();

    /**
     * 红方阵亡棋子
     * */
    public LinkedList<ChessEntity> RED_DIE_LIST = new LinkedList<>();

    /**
     * 黑方阵亡棋子
     * */
    public LinkedList<ChessEntity> BLACK_DIE_LIST = new LinkedList<>();

    public ChassPoint redDiechassPoint;

    public ChassPoint blackDiechassPoint;

    public ChessChoseEntity choseEntity;

    public Player choseRedPlayer;

    public Player choseBlackPlayer;

    //走棋循序
    public boolean isRedRun = true;

    /**
     * 棋盘结束
     * */
    public boolean isEnd;



    public ChessPanEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setScale(1.4f);
        if(namedTag.contains("place_face")){
            placeFace = BlockFace.fromIndex(namedTag.getInt("place_face"));
        }else{
            placeFace = BlockFace.NORTH;
        }
        if(namedTag.contains("is_end")){
            isEnd = namedTag.getBoolean("is_end");
        }
        if(namedTag.contains("is_red")){
            isRedRun = namedTag.getBoolean("is_red");
        }

        blackDiechassPoint = new ChassPoint(1.3f,1);
        redDiechassPoint = new ChassPoint(-1.3f,-1);
        // 初始化棋盘点位 (9列x10行)
        float startX = -1f; // 最左侧x坐标
        float startZ = -1f; // 最上方z坐标

        int index = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                float x = startX + col * 0.24f;
                float z = startZ + row * 0.23f;
                CAN_PLACES.put(index, new ChassPoint(x, z));
                index++;
            }
        }
        //TODO 在这里增加还原
        if(namedTag.contains("chess_data")){
            CompoundTag tag = namedTag.getCompound("chess_data");
            for(int i = 0;i<90;i++){
                if(tag.contains(i+"")){
                    int type = tag.getInt(i+"");
                    if(tag.contains("isDie") && tag.getBoolean("isDie")){
                        ChessEntity targetEntity;
                        if(type < 7){
                            targetEntity = getChessEntityByType(type,this.add(blackDiechassPoint.x,BLACK_DIE_LIST.size() * 0.03f,blackDiechassPoint.z),-1);
                            //黑方
                            BLACK_DIE_LIST.add(targetEntity);
                        }else{
                            targetEntity = getChessEntityByType(type,this.add(redDiechassPoint.x,RED_DIE_LIST.size() * 0.03f,redDiechassPoint.z),-1);
                            RED_DIE_LIST.add(targetEntity);

                        }
                    }else{

                        placeChess(type,i);
                    }

                }
            }
        }

    }



    public void initChess(){
        //清除tag
        namedTag.remove("chess_data");
        // 初始化棋子位置 (黑方:0-6, 红方:7-13)
        // 黑方棋子 (将,兵,炮,车,马,象,士)
        placeChess(0, 4, 0);  // 将
        placeChess(1, 0, 3);  // 兵1
        placeChess(1, 2, 3);  // 兵2
        placeChess(1, 4, 3);  // 兵3
        placeChess(1, 6, 3);  // 兵4
        placeChess(1, 8, 3);  // 兵5
        placeChess(2, 1, 2);  // 炮1
        placeChess(2, 7, 2);  // 炮2
        placeChess(3, 0, 0);  // 车1
        placeChess(3, 8, 0);  // 车2
        placeChess(4, 1, 0);  // 马1
        placeChess(4, 7, 0);  // 马2
        placeChess(5, 2, 0);  // 象1
        placeChess(5, 6, 0);  // 象2
        placeChess(6, 3, 0);  // 士1
        placeChess(6, 5, 0);  // 士2

        // 红方棋子 (将,兵,炮,车,马,象,士)
        placeChess(7, 4, 9);  // 将
        placeChess(8, 0, 6);  // 兵1
        placeChess(8, 2, 6);  // 兵2
        placeChess(8, 4, 6);  // 兵3
        placeChess(8, 6, 6);  // 兵4
        placeChess(8, 8, 6);  // 兵5
        placeChess(9, 1, 7);  // 炮1
        placeChess(9, 7, 7);  // 炮2
        placeChess(10, 0, 9); // 车1
        placeChess(10, 8, 9); // 车2
        placeChess(11, 1, 9); // 马1
        placeChess(11, 7, 9); // 马2
        placeChess(12, 2, 9); // 象1
        placeChess(12, 6, 9); // 象2
        placeChess(13, 3, 9); // 士1
        placeChess(13, 5, 9); // 士2
    }

    private void placeChess(int type, int col, int row) {
        int index = row * 9 + col;
        if (CAN_PLACES.containsKey(index)) {
            ChassPoint point = CAN_PLACES.get(index);
            CompoundTag tag2 =  Entity.getDefaultNBT(getPosition().add(point.x,0.1,point.z));
            tag2.putInt("chess_type", type);
            ChessEntity entity = new ChessEntity(chunk,tag2);
            if (type <= 6){
                entity.setYaw(180);
            }
            entity.setPanEntity(this);
            entity.setPanIndex(index);
            entity.spawnToAll();
            chessEntities.put(index, entity);
        }
    }

    private void placeChess(int type, int index) {
        if (CAN_PLACES.containsKey(index)) {
            ChassPoint point = CAN_PLACES.get(index);
            ChessEntity entity = getChessEntityByType(type,getPosition().add(point.x,0.1,point.z),index);
            chessEntities.put(index, entity);
        }
    }

    public ChessEntity getChessEntityByType(int type, Position pos,int index){
        CompoundTag tag2 =  Entity.getDefaultNBT(pos);
        tag2.putInt("chess_type", type);
        ChessEntity entity = new ChessEntity(chunk,tag2);
        if (type <= 6){
            entity.setYaw(180);
        }
        entity.setPanEntity(this);
        entity.setPanIndex(index);
        if(index == -1){
            entity.isDie = true;
        }
        entity.spawnToAll();
        return entity;
    }



    /**
     * 当前选中的棋子
     * */
    public int choseIndex;

    public ChessEntity choseIndexEntity = null;

    public void choseChessIndex(int index,Player player,boolean isBlack){
        this.choseIndex = index;
        choseIndexEntity = chessEntities.get(index);
        if(isBlack){
            this.choseBlackPlayer = player;
        }else{
            this.choseRedPlayer = player;
        }

    }

    /**
     * AI开始走棋
     * */
    public void goAI(){
        // 如果是AI对战且轮到AI走棋，触发AI走棋
        if(vsAI && ((aiIsRed && isRedRun) || (!aiIsRed && !isRedRun))) {
            doAIMove();
            isRedRun = !isRedRun;
        }


    }

    /**
     * 走棋逻辑
     * @param targetIndex 目标位置索引
     * @return 是否走棋成功
     */
    public boolean chessToIndex(int targetIndex) {
        // 参数检查
        if(targetIndex < 0 || targetIndex >= 90) {
            if(choseRedPlayer != null) {
                choseRedPlayer.sendMessage(TextFormat.colorize('&',"&c目标位置无效"));
            }
            if(choseBlackPlayer != null) {
                choseBlackPlayer.sendMessage(TextFormat.colorize('&',"&c目标位置无效"));
            }
            return false;
        }


        if (choseIndexEntity == null || !CAN_PLACES.containsKey(targetIndex)) {
            return false;
        }

        int sourceIndex = choseIndex;
        ChessEntity sourceEntity = chessEntities.get(sourceIndex);
        ChessEntity targetEntity = chessEntities.get(targetIndex);

        // 获取行列坐标 (10行x9列)
        if (sourceIndex < 0 || sourceIndex >= 90) {
            return false; // 越界检查
        }
        int sourceRow = sourceIndex / 9;
        int sourceCol = sourceIndex % 9;
        int targetRow = targetIndex / 9;
        int targetCol = targetIndex % 9;

        // 检查走棋规则
        if (!isValidMove(sourceEntity.type, sourceRow, sourceCol, targetRow, targetCol)) {
            return false;
        }

        // 检查是否吃自己的棋子 (type<6是黑方，>=7是红方)
        if (targetEntity != null) {
            boolean sourceIsBlack = sourceEntity.type < 7;
            boolean targetIsBlack = targetEntity.type < 7;
            if (sourceIsBlack == targetIsBlack) {
                return false; // 不能吃自己的棋子
            }
        }

        // 执行走棋
        if (targetEntity != null) {
            // 吃子
            targetEntity.isDie = true;
            if(targetEntity.type < 7){
                //黑方
                targetEntity.teleport(this.add(blackDiechassPoint.x,BLACK_DIE_LIST.size() * 0.05f,blackDiechassPoint.z));
                BLACK_DIE_LIST.add(targetEntity);
            }else{

                targetEntity.teleport(this.add(redDiechassPoint.x,RED_DIE_LIST.size() * 0.05f,redDiechassPoint.z));
                RED_DIE_LIST.add(targetEntity);
            }
            chessEntities.remove(targetIndex);
            //判断是否结束
            if(targetEntity.type == 0 || targetEntity.type == 7){
                isEnd = true;
                //判断玩家
                if(choseRedPlayer != null){
                    if(targetEntity.type == 7){
                        choseRedPlayer.sendTitle(TextFormat.colorize('&',"&0黑方获胜"));

                    }else{
                        choseRedPlayer.sendTitle(TextFormat.colorize('&',"&c红方获胜"));
                    }
                }
                if(choseBlackPlayer != null){
                    if(targetEntity.type == 7){
                        choseBlackPlayer.sendTitle(TextFormat.colorize('&',"&0黑方获胜"));
                    }else{
                        choseBlackPlayer.sendTitle(TextFormat.colorize('&',"&c红方获胜"));
                    }
                }

            }

        }

        // 移动棋子
        chessEntities.remove(sourceIndex);
        chessEntities.put(targetIndex, sourceEntity);
        sourceEntity.setPanIndex(targetIndex);

        // 更新位置
        ChassPoint point = CAN_PLACES.get(targetIndex);
        sourceEntity.teleport(getPosition().add(point.x, 0.1, point.z));


        return true;
    }


    /**
     * 验证走棋是否符合规则
     */
    public boolean isValidMove(int chessType, int sourceRow, int sourceCol, int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - sourceRow);
        int colDiff = Math.abs(targetCol - sourceCol);
        ChessEntity targetEntity = chessEntities.get(targetRow * 9 + targetCol);

        switch (chessType) {
            case 0: case 7: // 将/帅
                // 将对将直接吃子
                if(targetEntity != null &&
                   ((chessType == 0 && targetEntity.type == 7) ||
                    (chessType == 7 && targetEntity.type == 0))) {
                    // 检查中间是否有其他棋子
                    if(sourceCol == targetCol) {
                        int minRow = Math.min(sourceRow, targetRow);
                        int maxRow = Math.max(sourceRow, targetRow);
                        for(int r = minRow + 1; r < maxRow; r++) {
                            if(chessEntities.get(r * 9 + sourceCol) != null) {
                                return false;
                            }
                        }
                        return true;
                    }
                }

                // 普通走法
                return (colDiff == 1 && rowDiff == 0) || (rowDiff == 1 && colDiff == 0)
                    && targetCol >= 3 && targetCol <= 5
                    && ((chessType == 0 && targetRow <= 2) || (chessType == 7 && targetRow >= 7));

            case 1: case 8: // 兵/卒
                // 必须走一格
                if(rowDiff + colDiff != 1) {
                    return false;
                }

                if(chessType == 1) { // 黑兵
                    if(sourceRow >= 5) { // 过河后
                        // 可以前、左、右走，不能后退
                        return targetRow >= sourceRow || rowDiff == 0;
                    } else {
                        // 未过河只能前进
                        return targetRow > sourceRow && colDiff == 0;
                    }
                } else { // 红兵
                    if(sourceRow <= 4) { // 过河后
                        // 可以前、左、右走，不能后退
                        return targetRow <= sourceRow || rowDiff == 0;
                    } else {
                        // 未过河只能前进
                        return targetRow < sourceRow && colDiff == 0;
                    }
                }

            case 2: case 9: // 炮
                if (rowDiff != 0 && colDiff != 0 && rowDiff != colDiff) {
                    return false; // 必须直线移动
                }
                int pieceCount = countPiecesBetween(sourceRow, sourceCol, targetRow, targetCol);
                targetEntity = chessEntities.get(targetRow * 9 + targetCol);
                if (targetEntity == null) {
                    return pieceCount == 0; // 移动时中间不能有子
                } else {
                    return pieceCount == 1; // 吃子时中间必须有一个子
                }

            case 3: case 10: // 车
                if (rowDiff != 0 && colDiff != 0) {
                    return false; // 必须直线移动
                }
                return countPiecesBetween(sourceRow, sourceCol, targetRow, targetCol) == 0;

            case 4: case 11: // 马
                if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
                    return false; // 必须走日字
                }
                // 检查马脚
                int horseLegRow = sourceRow + (targetRow - sourceRow) / 2;
                int horseLegCol = sourceCol + (targetCol - sourceCol) / 2;
                return chessEntities.get(horseLegRow * 9 + horseLegCol) == null;

            case 5: case 12: // 象/相
                if (!(rowDiff == 2 && colDiff == 2)) {
                    return false; // 必须走田字
                }
                // 检查象眼
                int elephantEyeRow = (sourceRow + targetRow) / 2;
                int elephantEyeCol = (sourceCol + targetCol) / 2;
                if (chessEntities.get(elephantEyeRow * 9 + elephantEyeCol) != null) {
                    return false;
                }
                // 象不能过河
                return (chessType == 5 && targetRow <= 4) || (chessType == 12 && targetRow >= 5);

            case 6: case 13: // 士/仕
                if (!(rowDiff == 1 && colDiff == 1)) {
                    return false; // 必须斜走一格
                }
                // 必须在九宫格内
                return targetCol >= 3 && targetCol <= 5
                    && ((chessType == 6 && targetRow <= 2) || (chessType == 13 && targetRow >= 7));

            default:
                return false;
        }
    }

    /**
     * 计算两点之间的棋子数量
     */
    private int countPiecesBetween(int row1, int col1, int row2, int col2) {
        int count = 0;
        if (row1 == row2) { // 横向
            int min = Math.min(col1, col2);
            int max = Math.max(col1, col2);
            for (int col = min + 1; col < max; col++) {
                if (chessEntities.get(row1 * 9 + col) != null) {
                    count++;
                }
            }
        } else if (col1 == col2) { // 纵向
            int min = Math.min(row1, row2);
            int max = Math.max(row1, row2);
            for (int row = min + 1; row < max; row++) {
                if (chessEntities.get(row * 9 + col1) != null) {
                    count++;
                }
            }
        }
        return count;
    }


    public boolean attack(EntityDamageEvent source) {
        EntityDamageEvent.DamageCause cause = source.getCause();
        return cause == EntityDamageEvent.DamageCause.VOID;
    }


    @Override
    public float getWidth() {
        return 1f;
    }

    @Override
    public float getHeight() {
        return 0.01f;
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return ChessEntityManager.DEF_QI_PAN;
    }

    @Override
    public int getNetworkId() {
        return getEntityDefinition().getRuntimeId();
    }


    @Override
    public void saveNBT() {
        super.saveNBT();
        namedTag.putInt("place_face", placeFace.getIndex());

        // 保存棋子状态
        CompoundTag chessData = new CompoundTag();
        for(Integer index : chessEntities.keySet()) {
            ChessEntity entity = chessEntities.get(index);
            if(entity != null) {
                chessData.putInt(index.toString(), entity.type);
                chessData.putBoolean("isDie", entity.isDie);
            }
        }
        namedTag.putCompound("chess_data", chessData);
        namedTag.putBoolean("is_end", isEnd);
        namedTag.putBoolean("is_red", isRedRun);
    }


    /**
     * 设置AI难度
     */
    public void setAIDifficulty(AIDifficulty difficulty) {
        this.aiDifficulty = difficulty;
    }

    /*
     * 开始人机对战
     * @param aiIsRed AI是否为红方
     */
    /**
     * 开启人机对战模式
     * @param difficulty 设置AI难度级别
     */
    public void startAIMatch(AIDifficulty difficulty) {
        this.vsAI = true;
        this.aiIsRed = false; // AI固定为黑方
        this.aiDifficulty = difficulty;
        if(!isRedRun) { // 黑方回合时立即走棋
            doAIMove();
        }

    }



    /**
     * 检查是否处于人机对战模式
     * @return true表示正在人机对战
     */
    public boolean isAIMatch() {
        return vsAI;
    }

    /**
     * 获取当前AI难度级别
     * @return 当前AI难度
     */
    public AIDifficulty getAIDifficulty() {
        return aiDifficulty;
    }

    /**
     * 停止人机对战
     */
    public void stopAIMatch() {
        this.vsAI = false;
    }

    /**
     * AI执行走棋
     * 根据设置的难度级别选择对应的策略
     */
    private void doAIMove() {
        if(!vsAI || isEnd) return;

        // 根据难度选择策略
        switch(aiDifficulty) {
            case EASY:
                doRandomMove();
                break;
            case MEDIUM:
                doMediumMove();
                break;
            case HARD:
                doHardMove();
                break;
        }
    }

    /**
     * 评估当前棋盘状态对AI方的价值
     * @param forRed 评估红方的优势
     * @return 评估分数，正数表示优势
     */
    private int evaluateBoard(boolean forRed) {
        int score = 0;

        // 棋子基础价值
        int[] pieceValues = {1000, 10, 50, 100, 50, 20, 20,  // 黑方棋子价值
                            1000, 10, 50, 100, 50, 20, 20}; // 红方棋子价值

        // 统计棋子价值
        for(ChessEntity entity : chessEntities.values()) {
            if(entity.type < 7) { // 黑方棋子
                score += forRed ? -pieceValues[entity.type] : pieceValues[entity.type];
            } else { // 红方棋子
                score += forRed ? pieceValues[entity.type] : -pieceValues[entity.type];
            }
        }

        // 考虑棋子位置价值
        // 这里可以添加更复杂的位置评估

        return score;
    }

    /**
     * 获取当前AI方的所有合法走法
     * @return 包含所有合法走法的列表，每个元素是[源位置, 目标位置]的数组
     */
    private LinkedList<int[]> getAllValidMoves() {
        LinkedList<int[]> validMoves = new LinkedList<>();

        // 获取所有AI方的棋子
        for(Integer sourceIndex : chessEntities.keySet()) {
            ChessEntity entity = chessEntities.get(sourceIndex);
            if(entity != null && ((aiIsRed && entity.type >= 7) || (!aiIsRed && entity.type < 7))) {
                int sourceRow = sourceIndex / 9;
                int sourceCol = sourceIndex % 9;

                // 检查所有可能的走法
                for(int targetIndex = 0; targetIndex < 90; targetIndex++) {
                    int targetRow = targetIndex / 9;
                    int targetCol = targetIndex % 9;

                    if(isValidMove(entity.type, sourceRow, sourceCol, targetRow, targetCol)) {
                        ChessEntity targetEntity = chessEntities.get(targetIndex);
                        // 检查是否吃自己的棋子
                        if(targetEntity == null ||
                           (aiIsRed && targetEntity.type < 7) ||
                           (!aiIsRed && targetEntity.type >= 7)) {
                            validMoves.add(new int[]{sourceIndex, targetIndex});
                        }
                    }
                }
            }
        }

        return validMoves;
    }

    /**
     * 简单难度
     */
    private void doRandomMove() {
        LinkedList<int[]> validMoves = getAllValidMoves();
        if(validMoves.isEmpty()) return;

        // 评估每个走法
        LinkedList<int[]> goodMoves = new LinkedList<>();
        LinkedList<int[]> neutralMoves = new LinkedList<>();

        for(int[] move : validMoves) {

            ChessEntity targetEntity = chessEntities.get(move[1]);
            // 吃子走法优先
            if(targetEntity != null) {
                // 吃高价值棋子是最好走法
                if((targetEntity.type == 0 || targetEntity.type == 7) ||  // 将/帅
                   (targetEntity.type == 3 || targetEntity.type == 10)) { // 车
                    goodMoves.add(move);
                    continue;
                }
                goodMoves.add(move);
            }
            // 普通走法
            else {
                neutralMoves.add(move);
            }
        }

        // 优先选择好走法，没有则选中性走法
        LinkedList<int[]> candidates = !goodMoves.isEmpty() ? goodMoves : neutralMoves;
        // 随机选择一个候选走法
        int randomMove = new java.util.Random().nextInt(candidates.size());
        int[] selectedMove = candidates.get(randomMove);
        // 执行走棋
        choseChessIndex(selectedMove[0], null, !aiIsRed);
        chessToIndex(selectedMove[1]);

    }

    /**
     * 中等难度：优先吃子并考虑简单评估
     * 1. 优先吃价值高的棋子
     * 2. 避免被吃
     * 3. 随机选择最优的几个走法之一
     */
    private void doMediumMove() {
        LinkedList<int[]> validMoves = getAllValidMoves();
        if(validMoves.isEmpty()) return;

        // 严格验证并评估每个走法
        LinkedList<int[]> bestMoves = new LinkedList<>();
        int bestScore = Integer.MIN_VALUE;

        for(int[] move : validMoves) {
            int sourceIndex = move[0];
            int targetIndex = move[1];
            ChessEntity sourceEntity = chessEntities.get(sourceIndex);
            ChessEntity targetEntity = chessEntities.get(targetIndex);
            int sourceRow = sourceIndex / 9;
            int sourceCol = sourceIndex % 9;
            int targetRow = targetIndex / 9;
            int targetCol = targetIndex % 9;

            // 严格验证走法
            if(!isValidMove(sourceEntity.type, sourceRow, sourceCol, targetRow, targetCol)) {
                continue;
            }

            // 检查是否吃自己的棋子
            if(targetEntity != null &&
              ((sourceEntity.type < 7 && targetEntity.type < 7) ||
               (sourceEntity.type >= 7 && targetEntity.type >= 7))) {
                continue;
            }

            // 计算走法得分
            int score = 0;
            if(targetEntity != null) {
                // 吃子得分
                score += targetEntity.type < 7 ?
                    (targetEntity.type == 0 ? 1000 : targetEntity.type == 3 ? 100 : 50) :
                    (targetEntity.type == 7 ? 1000 : targetEntity.type == 10 ? 100 : 50);
            }

            // 更新最佳走法
            if(score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if(score == bestScore) {
                bestMoves.add(move);
            }
        }

        // 随机选择一个最佳走法
        int randomMove = new java.util.Random().nextInt(bestMoves.size());
        int[] selectedMove = bestMoves.get(randomMove);

        // 执行走棋
        choseChessIndex(selectedMove[0], null, !aiIsRed);
        chessToIndex(selectedMove[1]);
    }

    /**
     * 困难难度：使用简单极小化极大算法考虑2步策略
     * 1. 评估所有可能的走法
     * 2. 考虑对手的最佳回应
     * 3. 选择最有利的走法
     */
    private void doHardMove() {
        LinkedList<int[]> validMoves = getAllValidMoves();
        if(validMoves.isEmpty()) return;

        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        // 严格验证并评估每个走法
        for(int[] move : validMoves) {
            ChessEntity sourceEntity = chessEntities.get(move[0]);
            ChessEntity targetEntity = chessEntities.get(move[1]);
            int sourceRow = move[0] / 9;
            int sourceCol = move[0] % 9;
            int targetRow = move[1] / 9;
            int targetCol = move[1] % 9;

            // 严格验证走法
            if(!isValidMove(sourceEntity.type, sourceRow, sourceCol, targetRow, targetCol)) {
                continue;
            }

            // 检查是否吃自己的棋子
            if(targetEntity != null &&
              ((sourceEntity.type < 7 && targetEntity.type < 7) ||
               (sourceEntity.type >= 7 && targetEntity.type >= 7))) {
                continue;
            }

            // 模拟走棋
            targetEntity = chessEntities.get(move[1]);
            chessEntities.remove(move[0]);
            chessEntities.put(move[1], sourceEntity);
            sourceEntity.setPanIndex(move[1]);
            isRedRun = !isRedRun;

            // 评估对手的最佳回应
            int minOpponentScore = Integer.MAX_VALUE;
            LinkedList<int[]> opponentMoves = getAllValidMoves();
            for(int[] oppMove : opponentMoves) {
                ChessEntity oppSource = chessEntities.get(oppMove[0]);
                ChessEntity oppTarget = chessEntities.get(oppMove[1]);
                chessEntities.remove(oppMove[0]);
                chessEntities.put(oppMove[1], oppSource);
                oppSource.setPanIndex(oppMove[1]);
                isRedRun = !isRedRun;

                // 评估局面
                int score = evaluateBoard(aiIsRed);
                if(score < minOpponentScore) {
                    minOpponentScore = score;
                }

                // 撤销对手走棋
                chessEntities.remove(oppMove[1]);
                chessEntities.put(oppMove[0], oppSource);
                oppSource.setPanIndex(oppMove[0]);
                if(oppTarget != null) {
                    chessEntities.put(oppMove[1], oppTarget);
                }
                isRedRun = !isRedRun;
            }

            // 如果没有对手走法，直接评估当前局面
            if(opponentMoves.isEmpty()) {
                minOpponentScore = evaluateBoard(aiIsRed);
            }

            // 选择对AI最有利的走法
            if(minOpponentScore > bestScore) {
                bestScore = minOpponentScore;
                bestMove = move;
            }

            // 撤销模拟走棋
            chessEntities.remove(move[1]);
            chessEntities.put(move[0], sourceEntity);
            sourceEntity.setPanIndex(move[0]);
            if(targetEntity != null) {
                chessEntities.put(move[1], targetEntity);
            }
            isRedRun = !isRedRun;
        }

        // 执行最佳走法
        if(bestMove != null) {
            choseChessIndex(bestMove[0], null, !aiIsRed);
            chessToIndex(bestMove[1]);
        } else {
            // 没有找到最佳走法，使用中等难度策略
            doMediumMove();
        }
    }

    @Override
    public void close() {
        clear();
        super.close();
    }

    public void clear(){
        //先清除
        if((choseIndexEntity != null)){
            choseIndexEntity.close();
        }
        choseIndexEntity = null;
        choseIndex = -1;
        for(Integer index : chessEntities.keySet()) {
            ChessEntity entity = chessEntities.get(index);
            if(entity != null) {
                entity.close();
            }
        }
        if(choseEntity != null){
            choseEntity.close();
        }
        isEnd = false;
        isRedRun = true;
        //清空死亡的
        for(ChessEntity entity: RED_DIE_LIST){
            entity.close();
        }
        for(ChessEntity entity: BLACK_DIE_LIST){
            entity.close();
        }
        chessEntities.clear();
    }

    public void resetChess() {
        clear();
        initChess();

    }

    public static class ChassPoint{
        float x;
        float z;

        public ChassPoint(float x, float z){
            this.x = x;
            this.z = z;
        }
    }
}
