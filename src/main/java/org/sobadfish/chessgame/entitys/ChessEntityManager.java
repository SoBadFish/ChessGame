package org.sobadfish.chessgame.entitys;

import cn.nukkit.entity.custom.EntityDefinition;

/**
 * 列举14个实体
 * */
public class ChessEntityManager {
    public static final EntityDefinition DEF_BLACK_JIANG =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_jiang")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_BLACK_BING =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_bing")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_BLACK_PAO =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_pao")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_BLACK_CHE =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_che")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_BLACK_MA =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_ma")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_BLACK_XIANG =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_xiang")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_BLACK_SHI =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:black_shi")
                    .implementation(ChessEntity.class)
                    .build();

    public static final EntityDefinition DEF_RED_JIANG =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_jiang")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_RED_BING =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_bing")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_RED_PAO =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_pao")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_RED_CHE =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_che")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_RED_MA =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_ma")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_RED_XIANG =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_xiang")
                    .implementation(ChessEntity.class)
                    .build();
    public static final EntityDefinition DEF_RED_SHI =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:red_shi")
                    .implementation(ChessEntity.class)
                    .build();

    public static final EntityDefinition DEF_QI_PAN =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:qipan")
                    .implementation(ChessPanEntity.class)
                    .build();

    public static final EntityDefinition DEF_CHOSE =
            EntityDefinition
                    .builder()
                    .identifier("chessgame:chose")
                    .implementation(ChessChoseEntity.class)
                    .build();

    public static EntityDefinition[] DEF_CHESS_ENTITY;
    static {
        DEF_CHESS_ENTITY = new EntityDefinition[] {
                DEF_BLACK_JIANG,
                DEF_BLACK_BING,
                DEF_BLACK_PAO,
                DEF_BLACK_CHE,
                DEF_BLACK_MA,
                DEF_BLACK_XIANG,
                DEF_BLACK_SHI,
                DEF_RED_JIANG,
                DEF_RED_BING,
                DEF_RED_PAO,
                DEF_RED_CHE,
                DEF_RED_MA,
                DEF_RED_XIANG,
                DEF_RED_SHI
        };
    }



}
