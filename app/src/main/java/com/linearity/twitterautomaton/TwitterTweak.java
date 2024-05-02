package com.linearity.twitterautomaton;

import static com.linearity.twitterautomaton.utils.LoggerLog;
import static com.linearity.twitterautomaton.utils.showObjectFields;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TwitterTweak implements IXposedHookLoadPackage {
    public static final boolean showTextFlag = false;
    public static final boolean showTagFlag = false;
    public static final boolean markDontLikeFlag = true;
    public static HashSet<String> newTags = new HashSet<>();
    public static Method[] keyMethods = new Method[2];
    public static Object[] objPool = new Object[1];

    public static final String[] mustContainOne = new String[]{
            "miku","hatsune","初音","ミク"
    };
    /**
     * when tweetText.size >= mustContainLength
     * and tweetText doesn't contain one word in mustContainOne,markDontLike
     * */
    public static final int mustContainLength = 200;
    public static final String[] blackListWords = new String[]{
            "虹夏", "ぼっち","apex","小春と湊","ドン・キホーテ","meiko","gumi","月雪ミヤコ",
            "more more jump","歌い手","足立レイ","vtuber","abydos","UTAU","鏡音","琴葉","結月ゆかり",
            "ウマ娘","ずんだもん"," ia ","喜多","御坂美琴"
    };
    public static final String[] blackListNames = new String[0];
    public static final String[] blackListID = new String[0];

    public static HashSet<String> blackListTags;
    static {
        String[] blackListTagsArray = new String[]{
                "鏡音", "巡音ルカ", "Gumi", "GUMI", "AI","重音テト", "Genshin", "MyGo", "vtuber",
                "足立レイ", "ぼっち・ざ・ろっく", "クラにか", "鏡音リン", "ウマ娘", "ねんどり", "TGCF", "天官賜福",
                "天官赐福", "ねんどろいど刀剣男士", "twstドール部", "アーニャ", "prsk",
                "ねんどろいどどーる", "西住みほ", "MEIKO", "KAITO", "小さなラブライブ", "歌ってみた",
                "DokiWaifuCupContest","DokiGallery","APEX","メズマライザー","純愛コンビ","小山芳姫", "八十八夜",
                "このタグを見た人は黙って魔法使いをあげる","アニメ好きと繋がりたい","VRM_live_viewer","滲音かこい",
                "愛奏さん","GWはフォロワーさんが増えるらしい","澁谷かのん","小春と湊","ドン・キホーテ",
                "4月を写真4枚で振り返る","水銀燈","suigintou","mercurylampe","銀様","doll","ドール","ぷにコレ",
                "メガハウス","MegaHouse","鈍川まなみ","深夜のねんどろ撮影６０分一本勝負","千歌Qi","音街ウナ",
                "南知多まゆの","松新","ブルアカ","BlueArchive","月雪ミヤコ","ビックシムたん",
                "企業公式が毎朝地元の天気を言い合う","愛知","名古屋","イマソラ","秋葉原","akiba","いまそら",
                "艦これ版深夜の真剣MMD60分一本勝負","ふつうのJK", "ゆるキャン","ガシャポン",
                "姫乃アズ","定山渓温泉","定山渓泉美","定山渓ビューホテル","東由利","がこおわ","青ブタ","ホテル湖龍",
                "河口湖多佳美","温泉むすめ","艦これ","外川つくし","銚子電気鉄道","鉄道むすめ","銚子電鉄","銚電",
                "琴葉葵さん","ピカチュウ","十津川飛香","柏たん","ビッカメ娘","駅メモ",
                "しぶハチたん","企業公式相互フォロー","企業公式春のフォロー祭り","鯉の日","湯梨浜町","東郷湖畔公園",
                "こいの湯","温泉たまご","鳥取県","ご当地Vtuber","魔理沙","企業公式相互フォロー",
                "水上バス","敦賀酒造","あおぞら鉄道","セタわんこ","中野四葉","五等分の花嫁","川崎たん","はるなへ",
                "お浄るりりん","能勢町","土肥金山","御泉印","八百康カフェ","玉造彗","おはポス","おはよう","ナイセン",
                "自転車ヘルメットの日","自転車月間","夏色花梨","小樽潮風高校","おはドル","アキバたん","鳴花ヒメ",
                "エヴァンゲリオン","ちいかわ","AIイラスト","AIgirl","AIart","足立レイ投稿祭2024","prsk_FA",
                "境界戦機プラモデルがお買い得","kk_senki","極鋼ノ装鬼","ビッカメ名古屋","おやつ","チーズ",
                "カスタムキャスト","おはようVツイッタラー","GWなので金髪を愛でます","初リプ・初絡み・時差リプ大歓迎",
                "チーズ小説","5月も仲良くしてくれる人リプ","東京都心はパラレルワールド","ハッターズFA","蒼囲空",
                "Furina","飯坂温泉","飯坂","ゆげお","寿楽園茶舗","湯らっとさばこちゃん","新茶",
                "Beyondtheway","Giga","歌ってみた","歌い手さんMIX師さん絵師さん動画師さんPさんと繋がりたい",
                "推し探し","推し不在","温むすトレ缶","湯郷美彩","湯郷温泉","ガソスタむすめ","吉田よしこ","おはツイ",
                "らきすた","よりもい","GUNMARINちゃん","野上武志","佐田杏里生誕祭2024","澁谷かのん生誕祭2024",
                "yenillust","紲星あかり","荒潮","ゴールデンウィークSNS展覧会2024","誰でもないガール","おはかのん",
                "にじさんじ","ディーゼルガールズ",
                "ああああ・ああああ・ああ・・あ・ああ・あ・あ・ああｆふｗｆｇふ" +
                        "いｒしゅいｊふぃｆほえｆおうぃおふおｗふわふわふぇｆいおふぇいｆっふへふふいうぇふいでゅいふぃうえｈ" +
                        "ふぃえｈｆひえｆふいふぃえ998",//wtf
                "skeb","かわいい","AI美少女","AIイラスト︎","AIイラスト好きさんと繋がりたい","浦風",
                "艦これ11周年おめでとう","ショウゾウ画","サムネカンショウ会","新宿西口たん","ALINA娘",
                "CHERRY_CLOTHES","ばけっ子","DDS鏡音リン","dollfiedream","ドルフィードリーム","五十嵐響子",
                "TOAClaris","フラワーパーク浜名湖","浜名湖花博2024","今日の褐色子","花姫図鑑","葬送のフリーレン",
                "カエデっこ","獣化ふぇ","みとねるっ子","MDD","すずりっこ","すずりっこのおへや","あいまいみどる",
                "3人の魔女","orionedoll","MDDアルル","窓際寸劇","ぐるっ子","しんく屋さん","ひまたねっ娘",
                "初めてお泊まりした温泉むすめ宿","りあんっ子","karinariaっ子","なにぬねこもの","おはドル",
                "うちのこかわいい","はなむら工房お散歩日和","エモドール","MELODY_C","ぷらむっ娘","ぷらむアイ",
                "ももマン","skyloops","ふにゃほわっ子","しろうさ服","azone","LilFairy","シュガーカップス",
                "すのわっ娘","ねこちょっこ","ぱぴぷぺ会","MMD艦これ","迅鯨","神威",
                "メグメグファイアーエンドレスナイト","めいぷる工房","のんたんはんどめいど","エルドール下着",
                "もふもふマン","るあべあい","ONE_ARIA_ON_THE_PLANETES","毎月1日はOИEちゃんの日",
                "OИE","毎月1日はONEちゃんの日","例大祭21","例大祭21告知","oc",
                "軽い気持ちでツイートしたら思った以上に反響があったツイート","このシーンが神すぎて溺愛してる",
                "星界美術部","恋の予感の日","VOICEROID","桜乃そら","thinkスタンプラリー","能勢","浄瑠璃シアター",
                "HonkaiStarRail","Sparkle","花火","P丸様","ほっこりトゥイッター","インコ","ワカケホンセイインコ",
                "スズランの日","おいしい市販","架空アニソン祭2024","弦巻マキ","ツルマキマキ","VOICEVOX",
                "ナースロボ_タイプT","TeyvatFashion","GenshinImpact","いらすとや","963art","たんぽぽ",
                "nikke","nikkeAnniversary","蛙田みぃな","４月を写真４枚で振り返る","アイドール名古屋",
                "世界まぐろデー","AIさくらさん","みくらこ","伊自良湖","尾櫃制服計画","三代静","rainmelセーラー",
                "神鉄","ラク子の紹介","神戸市","兵庫県","摂津国","通勤ラク子","幻想夢乙女","cospaly",
                "らいコス写真","コスプレ","平湯温泉","高山匠美","奥飛騨温泉郷","飛騨高山",
                "温泉閣","カナエちゃん","カナ絵アート","鹿屋園芸部","nijijourney","東方project","霧雨魔理沙",
                "鬼滅の刃","竈門禰󠄀豆子",
        };
        blackListTags = new HashSet<>(Arrays.asList(blackListTagsArray));
    }
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.processName.contains("com.twitter.android")
            || lpparam.packageName.contains("com.twitter.android")
        ){
            LoggerLog("Hooked Twitter");
            try {
                Class<?> FindingClass;

                Class<?> CurationViewDelegateBinder_Class = XposedHelpers.findClassIfExists("com.twitter.tweetview.core.ui.curation.CurationViewDelegateBinder",lpparam.classLoader);
                FindingClass = XposedHelpers.findClassIfExists("pae$a",lpparam.classLoader);//ad blocker
                if (FindingClass != null){
                    XposedBridge.hookAllMethods(FindingClass, "a", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object obj = param.args[2];
                            Field f = XposedHelpers.findFieldIfExists(obj.getClass(),"k");
                            if (f == null){
                                return;
                            }
                            obj = f.get(obj);if (obj == null){return;}
                            f = XposedHelpers.findFieldIfExists(obj.getClass(),"d");if (f == null){return;}
                            Object obj1 = f.get(obj);
                            if (obj1!=null && obj1.getClass().getTypeName().equals("cql")) {
                                param.setResult(null);
                                LoggerLog("ad cancelled");
                                return;
                            }
                            {
                                String userName = "";
                                String userID = "";
                                {
                                    Object vjsobj = param.args[2];
                                    String tweetText = "";
                                    ArrayList<String> tagList = new ArrayList<>();
                                    f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");if (f==null){return;}
                                    obj = f.get(obj);if (obj == null){return;}

                                    Object userNameObj = obj;
                                    Field f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"r3");
                                    if (f_user != null){
                                        userNameObj = f_user.get(userNameObj);
                                        if (userNameObj != null){
                                            f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"q");
                                            if (f_user != null){
                                                userNameObj = f_user.get(userNameObj);
                                                if (userNameObj != null && userNameObj instanceof String){
                                                    userName = (String) userNameObj;
                                                }
                                            }
                                            f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"d");
                                            if (f_user != null){
                                                userNameObj = f_user.get(userNameObj);
                                                if (userNameObj != null && userNameObj instanceof String){
                                                    userID = (String) userNameObj;
                                                }
                                            }
                                        }
                                    }

                                    Field f1 = XposedHelpers.findFieldIfExists(obj.getClass(),"X2");
                                    if (f1 != null){
                                        obj1 = f1.get(obj);
                                        if (obj1 != null){
//                                            showObjectFields(obj1,"    ");
                                            f1 = XposedHelpers.findFieldIfExists(obj1.getClass().getSuperclass(),"c");
                                            if (f1 != null){tweetText = (String) f1.get(obj1);}
                                        }
                                    }
                                    f = XposedHelpers.findFieldIfExists(obj.getClass(),"W2");if (f==null){return;}
                                    obj = f.get(obj);if (obj == null){return;}
                                    f = XposedHelpers.findFieldIfExists(obj.getClass(),"X");if (f==null){return;}
                                    obj = f.get(obj);if (obj == null){return;}
                                    f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");if (f==null){return;}
                                    obj = f.get(obj);if (obj == null){return;}
                                    f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");if (f==null){return;}
                                    obj = f.get(obj);if (!(obj instanceof List)){return;}
                                    List<?> tagEntities = (List<?>) obj;
                                    for (Object o:tagEntities){
                                        Field field = XposedHelpers.findFieldIfExists(o.getClass(),"y");
                                        if (field != null && field.getType().isInstance("")){
                                            String tag = (String) field.get(o);
                                            if (tag != null && !tag.isEmpty()){
                                                tagList.add(tag);
                                            }
                                        }
                                    }
                                    if (objPool[0] != null){
                                        if (checkDontLike(blackListTags, tagList, blackListWords, blackListNames, blackListID, userName, userID, tweetText,mustContainOne,mustContainLength)) {
                                            Object yva$c_obj = XposedHelpers.callStaticMethod(CurationViewDelegateBinder_Class,"d",param.args[2]);
                                            markDontLikeMethods.bpt_j(yva$c_obj, vjsobj, lpparam.classLoader);
                                            return;
                                        }
                                    }
                                    if (!tweetText.isEmpty()){
                                        if (!newTags.contains(tweetText)) {
                                            newTags.add(tweetText);
                                            if (showTextFlag){
                                                LoggerLog(tweetText);
                                            }
                                        }
                                    }
                                    if (!tagList.isEmpty()){
                                        StringBuilder sb = new StringBuilder();
                                        for (String s:tagList){
                                            if (!newTags.contains(s)){
                                                newTags.add(s);
                                                sb.append("\"");
                                                sb.append(s);
                                                sb.append("\",");
                                            }

                                        }
                                        if (sb.length()!=0 && showTagFlag){
                                            LoggerLog("[tag]" + sb);
                                        }
                                    }
                                }
                            }

                        }
                    });
                }
                 FindingClass = XposedHelpers.findClassIfExists("fot",lpparam.classLoader);
                 if (FindingClass != null){
                     XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
                         @Override
                         protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                             super.afterHookedMethod(param);
                             if (param.args.length == 20){

                                 String userName = "";
                                 String userID = "";
//                                 LoggerLog(new Exception("not an exception"));
                                 Field f;
                                 Object obj1;
                                 Object txtObj = param.args[4];
                                 Object obj = param.args[0];
                                 Object vx6obj = param.args[0];
                                 Object vjsobj = param.args[6];
                                 {
                                String tweetText = "";
                                ArrayList<String> tagList = new ArrayList<>();
                                f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");
                                obj = f.get(obj);if (obj == null){return;}
                                 Object userNameObj = obj;
                                 Field f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"r3");
                                 if (f_user != null){
                                     userNameObj = f_user.get(userNameObj);
                                     if (userNameObj != null){
                                         f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"q");
                                         if (f_user != null){
                                             userNameObj = f_user.get(userNameObj);
                                             if (userNameObj != null && userNameObj instanceof String){
                                                 userName = (String) userNameObj;
                                             }
                                         }
                                         f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"d");
                                         if (f_user != null){
                                             userNameObj = f_user.get(userNameObj);
                                             if (userNameObj != null && userNameObj instanceof String){
                                                 userID = (String) userNameObj;
                                             }
                                         }
                                     }
                                 }

                                Field f1 = XposedHelpers.findFieldIfExists(obj.getClass(),"X2");
                                obj1 = f1.get(obj);
                                if (obj1 != null){
                                    f1 = XposedHelpers.findFieldIfExists(obj1.getClass().getSuperclass(),"c");
                                    tweetText = (String) f1.get(obj1);
                                }
                                f = XposedHelpers.findFieldIfExists(obj.getClass(),"W2");
                                obj = f.get(obj);if (obj == null){return;}
                                f = XposedHelpers.findFieldIfExists(obj.getClass(),"X");
                                obj = f.get(obj);if (obj == null){return;}
                                f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");
                                obj = f.get(obj);if (obj == null){return;}
                                f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");
                                obj = f.get(obj);if (!(obj instanceof List)){return;}
                                List<?> tagEntities = (List<?>) obj;
                                for (Object o:tagEntities){
                                    Field field = XposedHelpers.findFieldIfExists(o.getClass(),"y");
                                    if (field != null && field.getType().isInstance("")){
                                        String tag = (String) field.get(o);
                                        if (tag != null && !tag.isEmpty()){
                                            tagList.add(tag);
                                        }
                                    }
                                }
                                if (checkDontLike(blackListTags,tagList,blackListWords,blackListNames,blackListID,userName,userID,tweetText,mustContainOne,mustContainLength)){
                                    if (objPool[0] == null){
                                        Object lObj = XposedHelpers.findField(txtObj.getClass(),"l").get(txtObj);
                                        objPool[0] = lObj;
                                    }
                                    if (keyMethods[0] == null){
                                        for (Method m:txtObj.getClass().getDeclaredMethods()){
                                            if (m.getName().equals("j")){
                                                keyMethods[0] = m;
                                                Object yva$c_obj = XposedHelpers.callStaticMethod(CurationViewDelegateBinder_Class,"d",vjsobj);
                                                m.invoke(txtObj,vx6obj,yva$c_obj,vjsobj);
                                            }
                                        }
                                    }else {
                                        Object yva$c_obj = XposedHelpers.callStaticMethod(CurationViewDelegateBinder_Class,"d",vjsobj);
                                        keyMethods[0].invoke(txtObj,vx6obj,yva$c_obj,vjsobj);
                                    }
                                }
                            }
                             }
                         }
                     });
                 }


            }catch (Exception e){
                LoggerLog(e);
            }
        }
    }

    public static boolean checkDontLike(
            HashSet<String> blackListTags,List<String> tags,String[] blackListWords,
            String[] blackListNames,String[] blackListID,String username,
            String userID,String tweetText, String[] mustContainOne,int mustContainLength){
        if (!markDontLikeFlag){return false;}
//        {
//            //warning:strict mode!
//            //you may get banned for this!!!!!!(not proofed)
//            for (String toCheck:mustContainOne){
//            if (tweetText.toLowerCase().contains(toCheck)){
//                return false;
//            }
//        }
//            return true;}
        String textLower = tweetText.toLowerCase();
        for (String toCheck:blackListWords){
            if (textLower.contains(toCheck)){
                LoggerLog("matched blacklist word:" + toCheck);
                return true;
            }
        }
        for (String toCheck:blackListNames){
            if (username.contains(toCheck)){
                LoggerLog("matched:" + toCheck);
                return true;
            }
        }
        for (String toCheck:blackListID){
            if (userID.contains(toCheck)){
                LoggerLog("matched:" + toCheck);
                return true;
            }
        }
        for (String tag:tags){
            if (blackListTags.contains(tag)) {
                LoggerLog("matched tag:" + tag);
                return true;
            }
        }
        if (tweetText.length() >= mustContainLength
                || !tweetText.contains("/")
            //AcdeAsdff cannot read japanese:
            // all "pure text"(without a link) without miku will be ignored
        ){
            for (String toCheck:mustContainOne){
                if (tweetText.toLowerCase().contains(toCheck)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}

