package kr.selfcontrol.selfwebfilter.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import kr.selfcontrol.selfwebfilter.dao.WebFilterDao;
import kr.selfcontrol.selfwebfilter.model.BlockType;
import kr.selfcontrol.selfwebfilter.model.BlockVo;
import kr.selfcontrol.selfwebfilter.model.GroupVo;
import kr.selfcontrol.selfwebfilter.util.SelfControlUtil;

/**
 * Created by hari on 17. 3. 3..
 */
public class InternetService {
    private static InternetService instance;
    WebFilterDao webFilterDao;
    Context context;
    List<BlockVo> whiteUrlList = new ArrayList<>();
    List<BlockVo> blockUrlList = new ArrayList<>();
    List<BlockVo> blockCntsList = new ArrayList<>();
    List<BlockVo> cautionList = new ArrayList<>();
    List<BlockVo> trustList = new ArrayList<>();
    List<GroupVo> groupVoList;
    FavoriteService favoriteService;

    private InternetService(Context context) {
        this.context = context;
        webFilterDao = new WebFilterDao(context);
        favoriteService = FavoriteService.getInstance(context);
    }

    public static InternetService getInstance(Context context) {
        if (instance == null) {
            instance = new InternetService(context);
        }
        return instance;
    }

    public void updateBlockData() {
        if (webFilterDao != null) {
            webFilterDao.close();
        }
        webFilterDao = new WebFilterDao(context);

        groupVoList = webFilterDao.readGroupVoList();
        if (groupVoList != null) {
            blockCntsList.clear();
            blockUrlList.clear();
            whiteUrlList.clear();
            cautionList.clear();
            trustList.clear();
            for (GroupVo group : groupVoList) {
                System.out.println("LLL" + group.type.name());
                List<BlockVo> blockList = webFilterDao.readBlockVoList(group.id);
                if (blockList != null) {
                    for (BlockVo block : blockList) {
                        if (block.isBlocked()) {
                            if (group.type.equals(BlockType.URL)) {
                                block.value = SelfControlUtil.decode(block.value);
                                blockUrlList.add(block);
                                //     Log.d("showUrl",block.value);
                            } else if (group.type.equals(BlockType.WHITEURL)) {
                                block.value = SelfControlUtil.decode(block.value);
                                whiteUrlList.add(block);
                                //       Log.d("showHtml", block.value);
                            } else if (group.type.equals(BlockType.HTML)) {
                                block.value = SelfControlUtil.decode(block.value);
                                blockCntsList.add(block);
                                //       Log.d("showHtml", block.value);
                            } else if (group.type.equals(BlockType.TRUST)) {
                                block.value = SelfControlUtil.decode(block.value);
                                trustList.add(block);
                                //         Log.d("showTrust", block.value);
                            } else if (group.type.equals(BlockType.CAUTION)) {
                                block.value = SelfControlUtil.decode(block.value);
                                cautionList.add(block);
                                //           Log.d("showCaution", block.value);
                            }
                        }
                    }
                }
            }
        }
    }

    public String hasBadUrl(String url) {
        if (url == null) return null;
        if (!url.startsWith("http")) return null;

        String test = "";
        try {
            if (SelfControlUtil.isHangulHanjaJapaness(java.net.URLDecoder.decode(url.toLowerCase(), "utf-8"))) {
                return "hanjaJapanese";
            }
            test = SelfControlUtil.remainOnlyKoreanAndEnglish(java.net.URLDecoder.decode(url.toLowerCase(), "utf-8"));
        } catch (Exception exc) {
        }

        for (BlockVo blockUrl : blockUrlList) {
            if (test.contains(blockUrl.value)) {
                return blockUrl.value;
            }
        }
        return null;
    }

    public String hasBadHtml(String html) {
        if (html == null) return null;

        String test = "";
        test = html.toLowerCase();

        for (BlockVo blockCnts : blockCntsList) {
            if (test.contains(blockCnts.value)) {
                return blockCnts.value;
            }
        }
        return null;
    }

    public boolean isTrustUrl(String url) {
        if (url == null) return false;
        try {
            String test = "";
            test = url.toLowerCase();
            test = test.split("/")[2];

            for (BlockVo trust : trustList) {
                if (test.contains(trust.value)) {
                    return true;
                }
            }
        } catch (Exception exc) {
            return false;
        }
        return false;
    }

    public boolean isWhiteUrl(String url) {
        if (url == null) return false;
        try {
            String test = "";
            test = url.toLowerCase();
            test = test.split("/")[2];

            for (BlockVo trust : whiteUrlList) {
                if (test.contains(trust.value)) {
                    return true;
                }
            }
        } catch (Exception exc) {
            return false;
        }
        System.out.println("WhiteUrl Not Passed : " + url);
        return false;
    }
}
