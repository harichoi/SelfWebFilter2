package kr.selfcontrol.selfwebfilter.model;

/**
 * Created by hari on 17. 3. 2..
 */
public enum BlockType {
    WHITEURL, URL, HTML, CAUTION, TRUST;

    public static BlockType make(String type) {
        if ("whiteurl".equalsIgnoreCase(type)) {
            return BlockType.WHITEURL;
        } else if ("url".equalsIgnoreCase(type)) {
            return BlockType.URL;
        } else if ("html".equalsIgnoreCase(type)) {
            return BlockType.HTML;
        } else if ("caution".equalsIgnoreCase(type)) {
            return BlockType.CAUTION;
        } else if ("trust".equalsIgnoreCase(type)) {
            return BlockType.TRUST;
        }

        return null;
    }

    public static boolean isWhiteCase(BlockType blockType) {
        return blockType == WHITEURL || blockType == TRUST;
    }

    public boolean isWhiteCase() {
        return this == WHITEURL || this == TRUST;
    }
}
