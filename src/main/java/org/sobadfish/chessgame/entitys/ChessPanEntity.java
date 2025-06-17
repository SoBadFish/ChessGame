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
import cn.nukkit.utils.Utils;
import org.sobadfish.chessgame.manager.ThreadManager;

import java.util.*;

public class ChessPanEntity extends Entity implements CustomEntity {

    // AI难度级别
    public enum AIDifficulty {
        EASY,
        MEDIUM, 
        HARD
    }

    private AIDifficulty aiDifficulty = AIDifficulty.EASY;
    private boolean vsAI = false;
    private boolean aiIsRed = false;
    public BlockFace placeFace;

    /**
     * 象棋棋盘是 9*10 的大小，每个点位都有 x z 两个值
     */

    /** 每个点位的象棋棋子 */
    public LinkedHashMap<Integer, ChessEntity> chessEntities = new LinkedHashMap<>();

    /** 点位预设遵循左+右减 */
    public LinkedHashMap<Integer, ChassPoint> CAN_PLACES = new LinkedHashMap<>();

    /** 红方阵亡棋子 */
    public LinkedList<ChessEntity> RED_DIE_LIST = new LinkedList<>();

    /** 黑方阵亡棋子 */ 
    public LinkedList<ChessEntity> BLACK_DIE_LIST = new LinkedList<>();

    public ChassPoint redDiechassPoint;

    public ChassPoint blackDiechassPoint;

    public ChessChoseEntity choseEntity;

    public Player choseRedPlayer;

    public Player choseBlackPlayer;

    // 安全评估方法
    private boolean isMoveSafe(int[] move, ChessEntity captured, boolean kingInDanger) {
        if (captured == null) return true;

        ChessEntity movingPiece = chessEntities.get(move[0]);
        if (movingPiece != null && isPieceUnderThreat(move[1])) {
            int movingValue = getPieceValue(movingPiece.type);
            int capturedValue = getPieceValue(captured.type);
            return capturedValue > movingValue * 1.2 ||
                  (kingInDanger && capturedValue > movingValue);
        }
        return true;
    }

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
        if(namedTag.contains("vs_ai")){
            vsAI = namedTag.getBoolean("vs_ai");
        }

        if(namedTag.contains("ai_difficulty")){
            aiDifficulty = AIDifficulty.valueOf(namedTag.getString("ai_difficulty"));
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

        // 检查是否吃自己的棋子 (type<6是黑方，>=7是 红方)
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

        choseIndexEntity = null;

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
            case 0: case 7: { // 黑将 / 红帅
                // 判断将对将
                if (sourceCol == targetCol) {
                    targetEntity = chessEntities.get(targetRow * 9 + targetCol);
                    if (targetEntity != null &&
                            ((chessType == 0 && targetEntity.type == 7) || (chessType == 7 && targetEntity.type == 0))) {

                        int minRow = Math.min(sourceRow, targetRow);
                        int maxRow = Math.max(sourceRow, targetRow);
                        for (int r = minRow + 1; r < maxRow; r++) {
                            if (chessEntities.get(r * 9 + sourceCol) != null) {
                                return false; // 中间有子，不能将对将
                            }
                        }
                        return true; // 允许将吃将
                    }
                }
                // 普通移动
                if (!((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1))) return false;
                if (targetCol < 3 || targetCol > 5) return false;
                if (chessType == 0 && targetRow > 2) return false;  // 黑将只能在0~2行
                if (chessType == 7 && targetRow < 7) return false;  // 红帅只能在7~9行
                return true;
            }

            case 1: case 8: // 兵/卒
                // 必须走一格
                if(rowDiff + colDiff != 1) {
                    return false;
                }

                if (chessType == 1) { // 黑兵
                    if (sourceRow >= 5) {
                        return (targetRow == sourceRow && colDiff == 1) || (targetRow == sourceRow + 1 && colDiff == 0);
                    } else {
                        return (targetRow == sourceRow + 1 && colDiff == 0);
                    }
                } else { // 红兵
                    if (sourceRow <= 4) {
                        return (targetRow == sourceRow && colDiff == 1) || (targetRow == sourceRow - 1 && colDiff == 0);
                    } else {
                        return (targetRow == sourceRow - 1 && colDiff == 0);
                    }
                }


            case 2: case 9: // 炮
                if (rowDiff != 0 && colDiff != 0) return false;

                int count = countPiecesBetween(sourceRow, sourceCol, targetRow, targetCol);
                if (targetEntity == null) {
                    return count == 0;
                } else {
                    // 如果吃的目标是将帅，仍要求中间必须有一个棋子
                    return count == 1;
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
    // 兵/卒移动验证已整合到isValidMove方法中

    /**
     * 炮移动验证
     */
    private boolean isValidCannonMove(int sourceRow, int sourceCol,
                                      int targetRow, int targetCol, ChessEntity targetEntity) {
        int rowDiff = Math.abs(targetRow - sourceRow);
        int colDiff = Math.abs(targetCol - sourceCol);

        // 必须直线移动
        if (rowDiff != 0 && colDiff != 0) return false;

        int count = countPiecesBetween(sourceRow, sourceCol, targetRow, targetCol);
        if (targetEntity == null) {
            return count == 0; // 移动时不能有子
        } else {
            return count == 1; // 吃子时必须有一个炮架
        }
    }

    /**
     * 车移动验证
     */
    private boolean isValidChariotMove(int sourceRow, int sourceCol,
                                       int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - sourceRow);
        int colDiff = Math.abs(targetCol - sourceCol);

        // 必须直线移动
        if (rowDiff != 0 && colDiff != 0) {
            return false;
        }
        return countPiecesBetween(sourceRow, sourceCol, targetRow, targetCol) == 0;
    }

    /**
     * 马移动验证
     */
    private boolean isValidHorseMove(int sourceRow, int sourceCol,
                                     int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - sourceRow);
        int colDiff = Math.abs(targetCol - sourceCol);

        // 必须走日字
        if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
            return false;
        }

        // 检查马脚
        int horseLegRow = sourceRow + (targetRow - sourceRow) / 2;
        int horseLegCol = sourceCol + (targetCol - sourceCol) / 2;
        return chessEntities.get(horseLegRow * 9 + horseLegCol) == null;
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
        namedTag.putBoolean("vs_ai", vsAI);
        namedTag.putString("ai_difficulty", aiDifficulty.name());
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
     * 判断目标位置是否为关键防守位置
     */
    private boolean isKeyDefensePosition(int position, int pieceType) {
        int row = position / 9;
        int col = position % 9;

        // 将/帅的九宫格区域
        if(pieceType == 0 || pieceType == 7) {
            return (row >= 0 && row <= 2 && col >= 3 && col <= 5) ||
                    (row >= 7 && row <= 9 && col >= 3 && col <= 5);
        }

        // 士/仕的斜线位置
        if(pieceType == 1 || pieceType == 8) {
            return (col == 3 || col == 5) && (row == 0 || row == 2 || row == 7 || row == 9);
        }

        // 象/相的河界位置
        if(pieceType == 2 || pieceType == 9) {
            return row == 4 || row == 5;
        }


        // 车/炮的中线位置
        if(pieceType == 3 || pieceType == 4 || pieceType == 10 || pieceType == 11) {
            return col == 4;
        }

        return false;
    }


    // ============ 简单难度 ============
    private void doEasyMove() {
        boolean kingInDanger = !getKingThreats().isEmpty();
        List<int[]> moves = getAllValidMoves();
        sortMoves(moves);
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        // 优先检查是否能直接将军
        for (int[] move : moves) {
            ChessEntity target = chessEntities.get(move[1]);
            if (target != null && target.type == 7) {
                choseChessIndex(move[0], null, false);
                chessToIndex(move[1]);
                return;
            }
        }

        for (int[] move : moves) {
            if (move == null || move.length < 2 || !isValidMoveByIndex(move[0], move[1])) continue;

            ChessEntity source = chessEntities.get(move[0]);
            ChessEntity captured = chessEntities.get(move[1]);
            if (source == null || source.type >= 7) continue;

            simulateMove(move[0], move[1]);
            boolean isSafe = isMoveSafe(move, captured, kingInDanger);

            int score = 0;
            if (captured != null) {
                score += getPieceValue(captured.type) / 2;
                if (!isSafe) {
                    score -= getPieceValue(source.type) / 3;
                }
            }

            score += evaluateKingProtection(move[0], move[1]);
            if (isOpponentInCheck()) score += 500; // 提高将军分数
            if (!isSelfInCheck() && isPieceDefended(move[1])) score += 50;

            undoMove(move[0], move[1], captured);

            if (score > bestScore && isSafe) {
                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove != null && !isMoveDangerous(bestMove)) {
            choseChessIndex(bestMove[0], null, false);
            chessToIndex(bestMove[1]);
        } else {
            doRandomMove();
        }
    }

    private void doRandomMove() {
        List<int[]> moves = getAllValidMoves();
        for (int[] move : moves) {
            if (isValidMoveByIndex(move[0], move[1])) {
                choseChessIndex(move[0], null, false);
                chessToIndex(move[1]);
                return;
            }
        }
    }
    // ============ 中等难度 ============

    private void doMediumMove() {
        List<int[]> moves = getAllValidMoves();
        sortMoves(moves);
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        // 1. 检查将军威胁
        int kingIndex = findBlackKingIndex();
        List<Integer> threats = getKingThreats();
        boolean kingInDanger = !threats.isEmpty();

        // 优先检查是否能直接将军
        for (int[] move : moves) {
            ChessEntity target = chessEntities.get(move[1]);
            if (target != null && target.type == 7) {
                choseChessIndex(move[0], null, false);
                chessToIndex(move[1]);
                return;
            }
        }

        for (int[] move : moves) {
            if (!isValidMoveByIndex(move[0], move[1])) continue;
            ChessEntity captured = chessEntities.get(move[1]);
            simulateMove(move[0], move[1]);

            int score = evaluateBoard(false);
            if (isSelfInCheck()) {
                undoMove(move[0], move[1], captured);
                continue;
            }

            // 2. 针对性防御评估
            if (captured != null) {
                score += getPieceValue(captured.type);
                // 如果能吃掉威胁将军的棋子，大幅加分
                if(kingInDanger && threats.contains(move[1])) {
                    score += getPieceValue(captured.type) * 3;
                }
                if(isPieceUnderThreat(move[1])) {
                    score -= getPieceValue(chessEntities.get(move[0]).type) / 2;
                }
            }

            // 3. 增强保护评估
            int protectionScore = evaluateKingProtection(move[0], move[1]);
            if(kingInDanger) {
                // 如果能阻挡威胁路线，额外加分
                if(canBlockThreat(move[0], move[1], kingIndex)) {
                    protectionScore += 200;
                }
                protectionScore *= 2; // 危险时加倍保护分数
            }
            score += protectionScore * 2; // 中等难度更重视保护

            // 4. 进攻性评估
            score += evaluateTargetedAttack(move[0], move[1]) * 2;
            if (isOpponentInCheck()) score += 800; // 提高将军分数
            if (!isSelfInCheck() && isPieceDefended(move[1])) score += 100;

            undoMove(move[0], move[1], captured);

            if (score > bestScore && !isMoveDangerous(move)) {
                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove != null) {
            choseChessIndex(bestMove[0], null, false);
            chessToIndex(bestMove[1]);
        } else {
            doEasyMove();
        }
    }




    // ============ 困难难度 ============
    private void doHardMove() {
        List<int[]> moves = getAllValidMoves();
        sortMoves(moves);
        int depth = getDynamicSearchDepth() + 2; // 更深层搜索

        int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        // 1. 将军保护优先级提升
        int kingIndex = findBlackKingIndex();
        List<Integer> threats = getKingThreats();
        boolean kingInDanger = !threats.isEmpty();
        boolean criticalDanger = threats.size() > 1;


        // 2. 紧急保护模式
        if(kingInDanger) {
            int[] bestProtectionMove = null;
            int maxProtectionScore = Integer.MIN_VALUE;

            for(int[] move : moves) {
                if(!isValidMoveByIndex(move[0], move[1])) continue;

                ChessEntity captured = chessEntities.get(move[1]);
                simulateMove(move[0], move[1]);

                // 增强保护评分
                int protectionScore = evaluateKingProtection(move[0], move[1]);

                // 多重保护策略
                if(criticalDanger) {
                    protectionScore *= 3; // 紧急情况加倍重视
                } else {
                    protectionScore *= 2;
                }

                // 确保移动后不会自将
                if(!isSelfInCheck()) {
                    protectionScore += 150;
                }

                // 如果能直接吃掉威胁棋子
                if(captured != null && threats.contains(move[1])) {
                    protectionScore += getPieceValue(captured.type) * 3;
                }

                // 如果能阻挡攻击路线
                if(canBlockThreat(move[0], move[1], kingIndex)) {
                    protectionScore += 200;
                }

                // 如果移动后将军有更多保护
                if(isBetterKingProtection(move[0], move[1], kingIndex)) {
                    protectionScore += 100;
                }

                undoMove(move[0], move[1], captured);

                if(protectionScore > maxProtectionScore) {
                    maxProtectionScore = protectionScore;
                    bestProtectionMove = move;
                }
            }

            if(bestProtectionMove != null && maxProtectionScore > 0) {
                choseChessIndex(bestProtectionMove[0], null, false);
                chessToIndex(bestProtectionMove[1]);
                return;
            }
        }

        // 3. 主动威胁消除
        int[] kingThreatMove = findKingThreatMove(moves);
        if(kingThreatMove != null) {
            // 验证威胁消除的有效性
            ChessEntity target = chessEntities.get(kingThreatMove[1]);
            if(target != null && target.type == 7) { // 红方将
                simulateMove(kingThreatMove[0], kingThreatMove[1]);
                boolean valid = !isSelfInCheck();
                undoMove(kingThreatMove[0], kingThreatMove[1], target);

                if(valid) {
                    choseChessIndex(kingThreatMove[0], null, false);
                    chessToIndex(kingThreatMove[1]);
                    return;
                }
            }
        }


        for (int[] move : moves) {
            if (!isValidMoveByIndex(move[0], move[1])) continue;
            ChessEntity captured = chessEntities.get(move[1]);
            simulateMove(move[0], move[1]);

            // 安全击杀检查
            boolean isSafeCapture;
            if(captured == null) {
                isSafeCapture = true; // 非吃子动作总是安全
            } else {
                // 检查吃子后是否会被立即吃掉
                ChessEntity movingPiece = chessEntities.get(move[1]);
                if(movingPiece != null && isPieceUnderThreat(move[1])) {
                    int movingValue = getPieceValue(movingPiece.type);
                    int capturedValue = getPieceValue(captured.type);
                    // 风险收益评估
                    isSafeCapture = capturedValue > movingValue * 1.2;

                    // 将军危险时放宽安全标准
                    if(kingInDanger && capturedValue > movingValue) {
                        isSafeCapture = true;
                    }
                } else {
                    isSafeCapture = true; // 无直接威胁
                }
            }

            int score = -negamax(depth - 1, -beta, -alpha);

            // 动态局势评估增强
            score += evaluateBoard(false) / 20; // 局面控制趋势引导

            // 将军保护评估
            int protectionScore = evaluateKingProtection(move[0], move[1]);

            // 根据将军危险状态调整保护权重
            if(kingInDanger) {
                // 阻挡威胁路线
                if(canBlockThreat(move[0], move[1], kingIndex)) {
                    protectionScore += 300 * (criticalDanger ? 2 : 1);
                }
                // 吃掉威胁棋子
                if(captured != null && threats.contains(move[1])) {
                    protectionScore += getPieceValue(captured.type) * (criticalDanger ? 5 : 3);
                }
                // 危险程度系数
                protectionScore *= (criticalDanger ? 3 : 2);
            }
            score += protectionScore;

            // 针对性进攻评估
            score += evaluateTargetedAttack(move[0], move[1]) * 3;

            // 若对方将无退路，加分
            if (countOpponentKingEscapeRoutes() <= 1) score += 150;

            // 若自己王安全，加分（稳定优势）
            if (!isSelfInCheck()) score += 80;

            // 若目标格为中心线或九宫附近（施压核心区域）
            if (isCenterControl(move[1])) score += 100;
            if (isSelfInCheck()) score = Integer.MIN_VALUE;

            if (captured != null) score += getPieceValue(captured.type);
            // 如果能直接将军，大幅增加分数
            ChessEntity target = chessEntities.get(move[1]);
            if (target != null && target.type == 7) {
                score += 5000; // 直接吃掉对方将的走法优先级最高
            } else if (isOpponentInCheck()) {
                score += 1000; // 将军走法优先级很高
            }
            if (!isSelfInCheck() && isPieceDefended(move[1])) score += 150;

            if (isFork(move[1])) score += 200;
            if (createsPin(move[0], move[1])) score += 200;

            undoMove(move[0], move[1], captured);

            // 只有当移动是安全的或者收益足够大时才考虑
            if ((isSafeCapture || score > bestScore * 1.5) && score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break;
        }

        if (bestMove != null) {
            choseChessIndex(bestMove[0], null, false);
            chessToIndex(bestMove[1]);
        } else {
            doMediumMove();
        }
    }

    private boolean isCenterControl(int index) {
        int row = index / 9;
        int col = index % 9;
        return (col >= 3 && col <= 5) && (row >= 0 && row <= 2 || row >= 7 && row <= 9);
    }

    private int countOpponentKingEscapeRoutes() {
        int kingType = 7; // 红方将
        int kingIndex = -1;
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            if (entry.getValue().type == kingType) {
                kingIndex = entry.getKey();
                break;
            }
        }
        if (kingIndex == -1) return 0;

        int row = kingIndex / 9, col = kingIndex % 9;
        int count = 0;
        for (int r = -1; r <= 1; r++) {
            for (int c = -1; c <= 1; c++) {
                if (Math.abs(r) + Math.abs(c) != 1) continue;
                int nr = row + r, nc = col + c;
                if (nr < 0 || nr >= 10 || nc < 0 || nc >= 9) continue;
                int toIndex = nr * 9 + nc;
                if (!isValidMove(kingType, row, col, nr, nc)) continue;
                ChessEntity occupant = chessEntities.get(toIndex);
                if (occupant == null || occupant.type < 7) count++;
            }
        }
        return count;
    }

    private boolean isFork(int targetIndex) {
        int threatCount = 0;
        ChessEntity piece = chessEntities.get(targetIndex);
        if (piece == null) return false;
        int row = targetIndex / 9;
        int col = targetIndex % 9;

        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity enemy = entry.getValue();
            if ((enemy.type >= 7) == (piece.type >= 7)) continue;
            int er = entry.getKey() / 9;
            int ec = entry.getKey() % 9;
            if (isValidMove(piece.type, row, col, er, ec)) {
                threatCount++;
            }
        }
        return threatCount >= 2;
    }



    /**
     * 检测是否形成战术攻击(马后炮、双车错等)
     */
    /**
     * 检测是否形成战术攻击
     * @param fromIndex 攻击起始位置
     * @param toIndex 攻击目标位置
     * @return 如果形成战术攻击则返回true
     */
    private boolean isTacticalAttack(int fromIndex, int toIndex) {
        ChessEntity source = chessEntities.get(fromIndex);
        ChessEntity target = chessEntities.get(toIndex);

        // 空值检查
        if(source == null || target == null) {
            return false;
        }

        boolean isRed = source.type >= 7;

        // 1. 马后炮检测(炮攻击且有马保护)
        if((source.type == 2 || source.type == 9) && // 炮
           hasHorseProtection(toIndex, isRed)) {
            return true;
        }

        // 2. 双车错检测(车攻击且形成双车错位)
        if((source.type == 3 || source.type == 10) && // 车
           hasDoubleChariotThreat(toIndex, isRed)) {
            return true;
        }

        // 3. 炮架攻击检测(炮攻击且目标为重要棋子)
        if((source.type == 2 || source.type == 9) && // 炮
           (target.type == 0 || target.type == 7 || // 将/帅
            target.type == 3 || target.type == 10)) { // 车
            return true;
        }

        return false;
    }



    /**
     * 评估将军保护分数
     * @return 保护分数 (0-100)
     */
    private int evaluateGeneralProtection() {
        int generalIndex = findGeneralIndex();
        if(generalIndex < 0) return 0;

        int protectionScore = 0;

        // 检查马保护
        if(hasHorseProtection(generalIndex, false)) protectionScore += 30;
        // 检查车保护
        if(hasDoubleChariotThreat(generalIndex, false)) protectionScore += 50;

        return protectionScore;
    }

    /**
     * 检查是否有其他棋子保护将军
     * @param kingIndex 将军位置索引
     * @param excludeIndex 要排除的棋子索引(通常是正在移动的棋子)
     * @return 如果有其他保护者返回true
     */
    private boolean hasOtherProtector(int kingIndex, int excludeIndex) {
        int kingRow = kingIndex / 9;
        int kingCol = kingIndex % 9;

        for(Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            int index = entry.getKey();
            ChessEntity protector = entry.getValue();

            // 跳过要排除的棋子和敌方棋子
            if(index == excludeIndex || protector == null || protector.type >= 7) continue;

            int row = index / 9;
            int col = index % 9;

            // 检查是否能保护将军
            if(isValidMove(protector.type, row, col, kingRow, kingCol)) {
                // 特殊处理炮的保护
                if(protector.type == 2 || protector.type == 9) {
                    int count = countPiecesBetween(row, col, kingRow, kingCol);
                    if(count != 1) continue; // 炮需要隔一个子才能保护
                }
                return true;
            }
        }
        return false;
    }

    private boolean hasHorseProtection(int targetIndex, boolean isRed) {
        int targetRow = targetIndex / 9;
        int targetCol = targetIndex % 9;

        // 检查周围8个位置是否有马保护
        for(int r = -2; r <= 2; r++) {
            for(int c = -2; c <= 2; c++) {
                if(Math.abs(r) + Math.abs(c) != 3) continue; // 马走日

                int horseRow = targetRow + r;
                int horseCol = targetCol + c;
                if(horseRow >= 0 && horseRow < 10 && horseCol >= 0 && horseCol < 9) {
                    ChessEntity horse = chessEntities.get(horseRow * 9 + horseCol);
                    if(horse != null && ((horse.type == 4 && !isRed) || (horse.type == 11 && isRed))) {
                        // 检查马腿是否被蹩
                        int legRow = targetRow + r/2;
                        int legCol = targetCol + c/2;
                        if(chessEntities.get(legRow * 9 + legCol) == null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isOpponentInCheck() {
        return isInCheck(true);
    }

    private boolean isInCheck(boolean forRed) {
        int kingType = forRed ? 7 : 0;
        int kingIndex = -1;
        for (var entry : chessEntities.entrySet()) {
            if (entry.getValue().type == kingType) {
                kingIndex = entry.getKey();
                break;
            }
        }
        if (kingIndex == -1) return true;

        int kr = kingIndex / 9, kc = kingIndex % 9;
        for (var entry : chessEntities.entrySet()) {
            ChessEntity e = entry.getValue();
            if ((e.type >= 7) == forRed) continue;
            int sr = entry.getKey() / 9, sc = entry.getKey() % 9;
            if (isValidMove(e.type, sr, sc, kr, kc)) return true;
        }
        return false;
    }

    /**
     * 检测双车错战术
     * @param targetIndex 目标位置索引
     * @param isRed 是否为红方
     * @return 是否存在双车错威胁
     */
    private boolean hasDoubleChariotThreat(int targetIndex, boolean isRed) {
        int targetRow = targetIndex / 9;
        int targetCol = targetIndex % 9;
        int chariotCount = 0;

        // 检查同一行和列的车
        for(int i = 0; i < 9; i++) {
            ChessEntity colEntity = chessEntities.get(targetRow * 9 + i);
            if(colEntity != null && ((colEntity.type == 3 && !isRed) || (colEntity.type == 10 && isRed))) {
                chariotCount++;
                if(chariotCount >= 2) return true;
            }
        }

        for(int i = 0; i < 10; i++) {
            ChessEntity rowEntity = chessEntities.get(i * 9 + targetCol);
            if(rowEntity != null && ((rowEntity.type == 3 && !isRed) || (rowEntity.type == 10 && isRed))) {
                chariotCount++;
                if(chariotCount >= 2) return true;
            }
        }

        return false;
    }



    private String moveToKey(int[] move) {
        return move[0] + "-" + move[1];
    }




    /**
     * 查找将军位置索引
     *
     * @return 将军位置索引，未找到返回-1
     */
    private int findGeneralIndex() {
        int generalType = 0; // 红方将/黑方帅
        for(int i = 0; i < chessEntities.size(); i++) {
            ChessEntity entity = chessEntities.get(i);
            if(entity != null && entity.type == generalType) {
                return i;
            }
        }
        return -1;
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
        RED_DIE_LIST.clear();
        BLACK_DIE_LIST.clear();
        chessEntities.clear();
    }

    public void resetChess() {
        clear();
        initChess();
    }


    /**
     * 打印当前棋盘状态
     */
    private void printChessBoard() {
        String[] pieceSymbols = {"将", "兵", "炮", "车", "马", "象", "士",
                               "帅", "兵", "炮", "车", "马", "象", "士"};

        for(int row = 0; row < 10; row++) {
            StringBuilder line = new StringBuilder();
            for(int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                ChessEntity entity = chessEntities.get(index);
                if(entity != null) {
                    String symbol = pieceSymbols[entity.type];
                    if(entity.type < 7) {
                        line.append("[").append(symbol).append("]"); // 黑方
                    } else {
                        line.append(" ").append(symbol).append(" ");  // 红方
                    }
                } else {
                    line.append(" · ");
                }
            }
            System.out.println(line.toString());
        }
    }



    private void logAIDecision(ChessEntity source, int targetIndex, ChessEntity target, int score) {
        String[] pieceNames = {"黑将", "黑兵", "黑炮", "黑车", "黑马", "黑象", "黑士",
                              "红帅", "红兵", "红炮", "红车", "红马", "红象", "红士"};
        String moveLog = String.format("AI决策: %s从(%d,%d)移动到(%d,%d)",
                pieceNames[source.type],
                source.pan_index % 9, source.pan_index / 9,
                targetIndex % 9, targetIndex / 9);
        if(target != null) {
            moveLog += String.format(" 吃%s", pieceNames[target.type]);
        }
        moveLog += String.format(" 得分:%d", score);
        System.out.println(moveLog);
    }

    public static class ChassPoint{
        float x;
        float z;

        public ChassPoint(float x, float z){
            this.x = x;
            this.z = z;
        }
    }


    //=-=================== AI 算法 ======================= //


    public void goAI() {
        if(vsAI && !isRedRun) {
            ThreadManager.executor.execute(() -> {
                doAIMove();
                isRedRun = !isRedRun;
            });
        }
    }

    private void doAIMove() {
        if (isEnd || !vsAI) return;
        switch (aiDifficulty) {
            case EASY -> doEasyMove();
            case MEDIUM -> doMediumMove();
            case HARD -> doHardMove();
        }
    }



    private boolean createsPin(int fromIndex, int toIndex) {
        ChessEntity attacker = chessEntities.get(fromIndex);
        ChessEntity defender = chessEntities.get(toIndex);
        if (attacker == null || defender == null) return false;

        // 检查是否移动后敌方无法动某个关键子
        simulateMove(fromIndex, toIndex);
        boolean inCheck = isOpponentInCheck();
        undoMove(fromIndex, toIndex, defender);
        return inCheck;
    }

// 原有困难模式（doHardMove）增强完毕

    private boolean isPieceDefended(int index) {
        ChessEntity target = chessEntities.get(index);
        if (target == null) return false;
        boolean isRed = target.type >= 7;
        int row = index / 9;
        int col = index % 9;

        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity e = entry.getValue();
            if ((e.type >= 7) != isRed) continue;
            int sr = entry.getKey() / 9;
            int sc = entry.getKey() % 9;
            if (isValidMove(e.type, sr, sc, row, col)) {
                return true;
            }
        }
        return false;
    }

    private int negamax(int depth, int alpha, int beta) {
        if (depth == 0 || isEnd) return evaluateBoard(false);

        List<int[]> moves = getAllValidMoves();
        sortMoves(moves);
        int max = Integer.MIN_VALUE;

        for (int[] move : moves) {
            ChessEntity captured = chessEntities.get(move[1]);
            simulateMove(move[0], move[1]);
            if (isSelfInCheck()) {
                undoMove(move[0], move[1], captured);
                continue;
            }
            int score = -negamax(depth - 1, -beta, -alpha);
            undoMove(move[0], move[1], captured);
            max = Math.max(max, score);
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break;
        }
        return max;
    }

    private void sortMoves(List<int[]> moves) {
        moves.sort((a, b) -> {
            ChessEntity ta = chessEntities.get(a[1]);
            ChessEntity tb = chessEntities.get(b[1]);
            int valueA = (ta != null ? getPieceValue(ta.type) : 0);
            int valueB = (tb != null ? getPieceValue(tb.type) : 0);
            return Integer.compare(valueB, valueA);
        });
    }

    private int getPieceValue(int type) {
        return switch (type) {
            case 0, 7 -> 10000;
            case 1, 8 -> 120;
            case 2, 9 -> 450;
            case 3, 10 -> 600;
            case 4, 11 -> 350;
            case 5, 12 -> 250;
            case 6, 13 -> 200;
            default -> 0;
        };
    }

    private ArrayList<int[]> getAllValidMoves() {
        ArrayList<int[]> result = new ArrayList<>();
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            int from = entry.getKey();
            ChessEntity entity = entry.getValue();
            if (entity == null || entity.type >= 7) continue;
            int row = from / 9, col = from % 9;
            for (int to = 0; to < 90; to++) {
                int tr = to / 9, tc = to % 9;
                if (isValidMove(entity.type, row, col, tr, tc)) {
                    ChessEntity target = chessEntities.get(to);
                    if (target == null || target.type >= 7) result.add(new int[]{from, to});
                }
            }
        }
        return result;
    }

    private void simulateMove(int from, int to) {
        ChessEntity e = chessEntities.remove(from);
        chessEntities.put(to, e);
        e.setPanIndex(to);
    }

    private void undoMove(int from, int to, ChessEntity captured) {
        ChessEntity e = chessEntities.remove(to);
        chessEntities.put(from, e);
        e.setPanIndex(from);
        if (captured != null) chessEntities.put(to, captured);
    }

    /**
     * 检查黑方将是否被将军
     */
    private boolean isSelfInCheck() {
        int kingIndex = findBlackKingIndex();
        if (kingIndex == -1) return true;

        int kr = kingIndex / 9, kc = kingIndex % 9;
        for (var entry : chessEntities.entrySet()) {
            ChessEntity e = entry.getValue();
            if (e == null || e.type < 7) continue; // 跳过黑方棋子和空位

            int sr = entry.getKey() / 9, sc = entry.getKey() % 9;
            // 增强将军检测，考虑特殊棋子的攻击规则
            if (e.type == 2 || e.type == 9) { // 炮
                int count = countPiecesBetween(sr, sc, kr, kc);
                if (count == 1 && isValidMove(e.type, sr, sc, kr, kc)) {
                    return true;
                }
            } else if (isValidMove(e.type, sr, sc, kr, kc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取对将军的所有直接威胁
     */
    private List<Integer> getKingThreats() {
        List<Integer> threats = new ArrayList<>();
        int kingIndex = findBlackKingIndex();
        if (kingIndex == -1) return threats;

        int kr = kingIndex / 9, kc = kingIndex % 9;
        for (var entry : chessEntities.entrySet()) {
            ChessEntity e = entry.getValue();
            if (e == null || e.type < 7) continue;

            int sr = entry.getKey() / 9, sc = entry.getKey() % 9;
            if (isValidMove(e.type, sr, sc, kr, kc)) {
                threats.add(entry.getKey());
            }
        }
        return threats;
    }

    // ================= 高级棋型识别与残局逼和 =================

    private boolean isTwoSoldiersDeadlock() {
        int redSoldier = 0;
        int blackSoldier = 0;
        for (ChessEntity e : chessEntities.values()) {
            if (e.type == 1) blackSoldier++;
            if (e.type == 8) redSoldier++;
        }
        return redSoldier == 1 && blackSoldier == 1;
    }

    private boolean isOnlyKingLeft(boolean forRed) {
        for (ChessEntity e : chessEntities.values()) {
            if ((e.type >= 7) == forRed && e.type != 7 && e.type != 0) return false;
        }
        return true;
    }

    private boolean detectFortressFormation() {
        int rook = 0, guard = 0;
        for (ChessEntity e : chessEntities.values()) {
            if (e.type == 3 || e.type == 10) rook++;
            if (e.type == 6 || e.type == 13) guard++;
        }
        return rook >= 2 && guard >= 2;
    }

    private int evaluateBoard(boolean forRed) {
        int baseScore = 0;
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity entity = entry.getValue();
            int type = entity.type;
            int value = getPieceValue(type);
            baseScore += ((type >= 7) == forRed ? value : -value);

            // 黑方棋子位置加成
            if(type < 7) {
                int row = entry.getKey() / 9;
                int col = entry.getKey() % 9;
                // 鼓励黑方棋子控制中心区域
                if(col >= 3 && col <= 5 && row >= 3 && row <= 6) {
                    baseScore += 20;
                }
            }
        }

        if (isOnlyKingLeft(true) && isOnlyKingLeft(false)) baseScore = 0;
        if (isTwoSoldiersDeadlock()) baseScore = 0;
        if (detectFortressFormation()) baseScore -= 200;

        return baseScore;
    }

    private int getDynamicSearchDepth() {
        int count = chessEntities.size();
        if (count <= 10) return 5;
        if (count <= 20) return 4;
        return 3;
    }

    // ============ 新增方法 ============

    /**
     * 查找黑方将的位置索引
     */
    private int findBlackKingIndex() {
        for (var entry : chessEntities.entrySet()) {
            if (entry.getValue().type == 0) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * 检查棋子是否在保护将军
     * @param pieceIndex 棋子位置索引
     * @param kingIndex 将军位置索引
     * @return 如果该棋子在保护将军则返回true
     */
    private boolean isProtectingKing(int pieceIndex, int kingIndex) {
        ChessEntity piece = chessEntities.get(pieceIndex);
        ChessEntity king = chessEntities.get(kingIndex);
        if(piece == null || king == null) return false;

        int pieceRow = pieceIndex / 9;
        int pieceCol = pieceIndex % 9;
        int kingRow = kingIndex / 9;
        int kingCol = kingIndex % 9;

        // 检查士的保护
        if((piece.type == 6 || piece.type == 13) && // 士/仕
           Math.abs(pieceCol - kingCol) == 1 &&
           Math.abs(pieceRow - kingRow) == 1) {
            return true;
        }

        // 检查象的保护
        if((piece.type == 5 || piece.type == 12) && // 象/相
           Math.abs(pieceCol - kingCol) == 2 &&
           Math.abs(pieceRow - kingRow) == 2) {
            // 检查象眼是否被堵
            int eyeRow = (pieceRow + kingRow) / 2;
            int eyeCol = (pieceCol + kingCol) / 2;
            return chessEntities.get(eyeRow * 9 + eyeCol) == null;
        }

        // 检查车的保护
        if((piece.type == 3 || piece.type == 10) && // 车
           (pieceCol == kingCol || pieceRow == kingRow)) {
            return true;
        }

        // 检查马的保护
        if((piece.type == 4 || piece.type == 11) && // 马
           ((Math.abs(pieceCol - kingCol) == 2 && Math.abs(pieceRow - kingRow) == 1) ||
            (Math.abs(pieceCol - kingCol) == 1 && Math.abs(pieceRow - kingRow) == 2))) {
            // 检查马腿是否被蹩
            int legRow = pieceRow + (kingRow - pieceRow) / 2;
            int legCol = pieceCol + (kingCol - pieceCol) / 2;
            return chessEntities.get(legRow * 9 + legCol) == null;
        }

        // 检查炮的保护
        if((piece.type == 2 || piece.type == 9) && // 炮
           (pieceCol == kingCol || pieceRow == kingRow)) {
            int count = countPiecesBetween(pieceRow, pieceCol, kingRow, kingCol);
            return count == 1; // 炮需要隔一个子才能保护
        }

        return false;
    }

    /**
     * 评估将军保护分数
     */
    private int evaluateKingProtection(int fromIndex, int toIndex) {
        int kingIndex = findBlackKingIndex();
        if(kingIndex == -1) return 0;

        ChessEntity movedPiece = chessEntities.get(fromIndex);
        if(movedPiece == null) return 0;

        // 1. 检查将军是否处于被攻击状态
        boolean kingInDanger = isPieceUnderThreat(kingIndex);

        // 2. 如果移动的棋子是保护将军的关键棋子，根据危险程度扣分
        if(isProtectingKing(fromIndex, kingIndex)) {
            return kingInDanger ? -200 : -80; // 将军危险时扣更多分
        }

        // 3. 如果新位置能更好地保护将军，根据危险程度加分
        if(isBetterKingProtection(fromIndex, toIndex, kingIndex)) {
            return kingInDanger ? 150 : 50; // 将军危险时加更多分
        }

        // 4. 如果将军处于危险中，优先考虑能解除危险的走法
        if(kingInDanger && canBlockThreat(fromIndex, toIndex, kingIndex)) {
            return 100;
        }

        return 0;
    }

    /**
     * 检查移动是否能阻挡对将军的威胁
     */
    private boolean canBlockThreat(int fromIndex, int toIndex, int kingIndex) {
        // 1. 找出所有威胁将军的敌方棋子
        List<Integer> threats = new ArrayList<>();
        int kingRow = kingIndex / 9;
        int kingCol = kingIndex % 9;

        // 2. 检查移动棋子是否本身就是保护将军的关键棋子
        boolean isKeyProtector = isProtectingKing(fromIndex, kingIndex);

        for(Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity attacker = entry.getValue();
            if(attacker == null || attacker.type >= 7) continue;

            int attackerRow = entry.getKey() / 9;
            int attackerCol = entry.getKey() % 9;
            if(isValidMove(attacker.type, attackerRow, attackerCol, kingRow, kingCol)) {
                threats.add(entry.getKey());
            }
        }

        // 3. 检查移动是否能阻挡或吃掉威胁棋子
        for(int threatIndex : threats) {
            // 如果能直接吃掉威胁棋子
            if(toIndex == threatIndex) {
                return true;
            }

            // 如果能阻挡攻击路线
            int threatRow = threatIndex / 9;
            int threatCol = threatIndex % 9;
            if(isOnAttackPath(toIndex, threatRow, threatCol, kingRow, kingCol)) {
                // 如果移动棋子是关键保护者，需要额外验证
                if(!isKeyProtector || hasOtherProtector(kingIndex, fromIndex)) {
                    return true;
                }
            }
        }

        return false;
    }




    /**
     * 检查位置是否在攻击路径上
     */
    private boolean isOnAttackPath(int posIndex, int threatRow, int threatCol,
                                  int kingRow, int kingCol) {
        int posRow = posIndex / 9;
        int posCol = posIndex % 9;

        // 直线攻击(车、炮、将)
        if(threatRow == kingRow || threatCol == kingCol) {
            if(threatRow == kingRow && posRow == threatRow) { // 同一行
                int min = Math.min(threatCol, kingCol);
                int max = Math.max(threatCol, kingCol);
                return posCol > min && posCol < max;
            } else if(threatCol == kingCol && posCol == threatCol) { // 同一列
                int min = Math.min(threatRow, kingRow);
                int max = Math.max(threatRow, kingRow);
                return posRow > min && posRow < max;
            }
        }
        return false;
    }




    /**
     * 评估针对性进攻
     * @param fromIndex 攻击起始位置
     * @param toIndex 攻击目标位置
     * @return 攻击评分(越高表示攻击效果越好)
     */
    private int evaluateTargetedAttack(int fromIndex, int toIndex) {
        ChessEntity attacker = chessEntities.get(fromIndex);
        ChessEntity target = chessEntities.get(toIndex);
        if(target == null) return 0;

        int score = 0;

        // 1. 优先攻击高价值棋子
        if(target.type == 10 || target.type == 11 || target.type == 9) {
            score += 50;
        }

        // 2. 鼓励控制红方将的活动空间
        if(isRestrictingRedKing(toIndex)) {
            score += 40;
        }

        // 3. 考虑攻击棋子的价值与自身价值的比例(避免用高价值棋子攻击低价值目标)
        if(attacker != null) {
            int attackerValue = getPieceValue(attacker.type);
            int targetValue = getPieceValue(target.type);
            if(attackerValue > targetValue * 1.5) {
                score -= 20; // 惩罚用高价值棋子攻击低价值目标
            }
        }

        // 4. 检查是否形成战术攻击(如马后炮、双车错等)
        if(isTacticalAttack(fromIndex, toIndex)) {
            score += 60;
        }

        return score;
    }

    /**
     * 查找直接将军的走法
     */
    private int[] findKingThreatMove(List<int[]> moves) {
        for(int[] move : moves) {
            ChessEntity target = chessEntities.get(move[1]);
            if(target != null && target.type == 7) { // 红方将
                return move;
            }
        }
        return null;
    }



    /**
     * 检查是否限制红方帅的活动(将帅对面规则)
     * @param positionIndex 要检查的位置索引
     * @return 如果该位置会形成将帅对面则返回true
     */
    private boolean isRestrictingRedKing(int positionIndex) {
        // 查找红方帅的位置
        int redKingIndex = -1;
        int blackKingIndex = -1;

        for(Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            if(entry.getValue().type == 7) { // 红方帅
                redKingIndex = entry.getKey();
            } else if(entry.getValue().type == 0) { // 黑方将
                blackKingIndex = entry.getKey();
            }
        }

        if(redKingIndex == -1 || blackKingIndex == -1) {
            return false;
        }

        int redCol = redKingIndex % 9;
        int blackCol = blackKingIndex % 9;

        // 检查是否在同一列
        if(redCol != blackCol) {
            return false;
        }

        // 检查中间是否有其他棋子
        int redRow = redKingIndex / 9;
        int blackRow = blackKingIndex / 9;
        int minRow = Math.min(redRow, blackRow);
        int maxRow = Math.max(redRow, blackRow);

        for(int row = minRow + 1; row < maxRow; row++) {
            if(chessEntities.get(row * 9 + redCol) != null) {
                return false; // 中间有棋子阻挡
            }
        }

        // 检查目标位置是否在同一列且会形成将帅对面
        int targetCol = positionIndex % 9;
        return targetCol == redCol;
    }

    private boolean isValidMoveByIndex(int from, int to) {
        if (from < 0 || from >= 90 || to < 0 || to >= 90) return false;
        if (!CAN_PLACES.containsKey(from) || !CAN_PLACES.containsKey(to)) return false;
        ChessEntity fromE = chessEntities.get(from);
        if (fromE == null) return false;
        int fr = from / 9, fc = from % 9;
        int tr = to / 9, tc = to % 9;
        return isValidMove(fromE.type, fr, fc, tr, tc);
    }

    /**
     * 检查指定位置的棋子是否受到威胁
     * @param index 要检查的棋子位置索引
     * @return 如果该位置的棋子受到敌方棋子威胁则返回true
     */
    public boolean isPieceUnderThreat(int index) {
        ChessEntity target = chessEntities.get(index);
        if (target == null) return false;

        boolean isRed = target.type >= 7; // 判断目标棋子阵营
        int targetRow = index / 9;
        int targetCol = index % 9;

        // 1. 检查将/帅的特殊威胁(将帅对面)
        if(target.type == 0 || target.type == 7) {
            int opponentKingType = target.type == 0 ? 7 : 0;
            for(Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
                ChessEntity entity = entry.getValue();
                if(entity != null && entity.type == opponentKingType) {
                    int kingRow = entry.getKey() / 9;
                    int kingCol = entry.getKey() % 9;
                    if(kingCol == targetCol) {
                        // 检查中间是否有阻挡
                        int minRow = Math.min(kingRow, targetRow);
                        int maxRow = Math.max(kingRow, targetRow);
                        boolean blocked = false;
                        for(int r = minRow + 1; r < maxRow; r++) {
                            if(chessEntities.get(r * 9 + kingCol) != null) {
                                blocked = true;
                                break;
                            }
                        }
                        if(!blocked) {
                            return true; // 将帅对面
                        }
                    }
                }
            }
        }

        // 2. 遍历所有敌方棋子，检查是否能攻击目标位置
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity attacker = entry.getValue();
            // 跳过己方棋子和空位
            if (attacker == null || (attacker.type >= 7) == isRed) continue;

            int attackerIndex = entry.getKey();
            int attackerRow = attackerIndex / 9;
            int attackerCol = attackerIndex % 9;

            // 3. 使用增强的isValidMove方法检查攻击有效性
            if (isValidMove(attacker.type, attackerRow, attackerCol, targetRow, targetCol)) {
                // 特殊处理炮的攻击规则
                if (attacker.type == 2 || attacker.type == 9) { // 炮
                    int count = countPiecesBetween(attackerRow, attackerCol, targetRow, targetCol);
                    if (count != 1) continue; // 炮必须隔一个子才能攻击
                }
                // 特殊处理马腿
                if (attacker.type == 4 || attacker.type == 11) { // 马
                    int legRow = attackerRow + (targetRow - attackerRow) / 2;
                    int legCol = attackerCol + (targetCol - attackerCol) / 2;
                    if(chessEntities.get(legRow * 9 + legCol) != null) {
                        continue; // 马腿被蹩
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 检查移动是否危险
     * @param move 移动数组 [fromIndex, toIndex]
     * @return 如果移动会导致危险情况则返回true
     */
    public boolean isMoveDangerous(int[] move) {
        if (move == null || move.length < 2) return false;

        ChessEntity movingPiece = chessEntities.get(move[0]);
        if (movingPiece == null) return false;

        boolean isRed = movingPiece.type >= 7;
        ChessEntity captured = chessEntities.get(move[1]);

        // 模拟移动
        simulateMove(move[0], move[1]);

        // 1. 检查移动后是否导致己方将/帅被将军
        int kingType = isRed ? 7 : 0;
        int kingIndex = -1;
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            if (entry.getValue().type == kingType) {
                kingIndex = entry.getKey();
                break;
            }
        }
        if (kingIndex != -1 && isPieceUnderThreat(kingIndex)) {
            undoMove(move[0], move[1], captured);
            return true;
        }

        // 2. 检查移动后是否导致高价值棋子被攻击
        int[] highValuePieces = {3, 4, 10, 11}; // 车和马
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity piece = entry.getValue();
            if (piece != null && (piece.type >= 7) == isRed) {
                for (int type : highValuePieces) {
                    if (piece.type == type && isPieceUnderThreat(entry.getKey())) {
                        undoMove(move[0], move[1], captured);
                        return true;
                    }
                }
            }
        }

        // 3. 检查移动后是否导致关键防御位置失守
        if (isProtectingKing(move[0], kingIndex)) {
            undoMove(move[0], move[1], captured);
            return true;
        }

        undoMove(move[0], move[1], captured);
        return false;
    }

    /**
     * 检查移动后是否能提供更好的将军保护
     * @param fromIndex 移动起始位置
     * @param toIndex 移动目标位置
     * @param kingIndex 将/帅的位置
     * @return 如果新位置能提供更好的保护则返回true
     */
    private boolean isBetterKingProtection(int fromIndex, int toIndex, int kingIndex) {
        if (kingIndex == -1) return false;

        ChessEntity movingPiece = chessEntities.get(fromIndex);
        if (movingPiece == null) return false;

        boolean isRed = movingPiece.type >= 7;
        ChessEntity captured = chessEntities.get(toIndex);

        // 模拟移动前计算当前保护分数
        int beforeScore = calculateProtectionScore(kingIndex, isRed);

        // 模拟移动
        simulateMove(fromIndex, toIndex);

        // 计算移动后的保护分数
        int afterScore = calculateProtectionScore(kingIndex, isRed);

        // 恢复移动
        undoMove(fromIndex, toIndex, captured);

        // 如果移动后保护分数提高，则认为保护更好
        return afterScore > beforeScore;
    }

    /**
     * 计算当前将/帅的保护分数
     * @param kingIndex 将/帅的位置
     * @param isRed 是否为红方
     * @return 保护分数(越高表示保护越好)
     */
    private int calculateProtectionScore(int kingIndex, boolean isRed) {
        int score = 0;
        int kingRow = kingIndex / 9;
        int kingCol = kingIndex % 9;

        // 1. 计算直接保护棋子的数量
        for (Map.Entry<Integer, ChessEntity> entry : chessEntities.entrySet()) {
            ChessEntity piece = entry.getValue();
            if (piece == null || (piece.type >= 7) != isRed) continue;

            int pieceRow = entry.getKey() / 9;
            int pieceCol = entry.getKey() % 9;

            // 检查是否能保护将/帅
            if (isValidMove(piece.type, pieceRow, pieceCol, kingRow, kingCol)) {
                // 不同类型棋子提供不同保护分数
                switch(piece.type) {
                    case 3: case 10: score += 30; break; // 车
                    case 4: case 11: score += 20; break; // 马
                    case 6: case 13: score += 15; break; // 士/仕
                    case 5: case 12: score += 10; break; // 象/相
                    case 2: case 9:  // 炮需要特殊处理
                        int count = countPiecesBetween(pieceRow, pieceCol, kingRow, kingCol);
                        if (count == 1) score += 15; // 炮架
                        break;
                }
            }
        }

        // 2. 计算将/帅的活动空间(越多越好)
        int mobility = 0;
        for (int r = -1; r <= 1; r++) {
            for (int c = -1; c <= 1; c++) {
                if (Math.abs(r) + Math.abs(c) != 1) continue; // 只考虑上下左右
                int newRow = kingRow + r;
                int newCol = kingCol + c;
                if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 9) {
                    int newIndex = newRow * 9 + newCol;
                    ChessEntity target = chessEntities.get(newIndex);
                    if (target == null || (target.type >= 7) != isRed) {
                        mobility++;
                    }
                }
            }
        }
        score += mobility * 5;

        // 3. 检查关键防御位置是否被控制
        if (isKeyDefensePosition(kingIndex, isRed ? 7 : 0)) {
            score += 20;
        }

        return score;
    }



}