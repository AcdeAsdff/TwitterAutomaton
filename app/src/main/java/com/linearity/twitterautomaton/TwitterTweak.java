package com.linearity.twitterautomaton;

import static com.linearity.utils.LoggerUtils.LoggerLog;
import static com.linearity.utils.LoggerUtils.searchStringInObjectFields;
import static com.linearity.utils.LoggerUtils.showObjectFields;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TwitterTweak implements IXposedHookLoadPackage{
    public static final boolean showTextFlag = true;
    public static final boolean showTagFlag = true;
    public static final boolean showUserNameAndID = true;
    public static final boolean markDontLikeFlag = true;
    public static HashSet<String> newTags = new HashSet<>();
    public static LinkedList<Object> objPool = new LinkedList<>();

    public static final String[] mustContainOne = new String[]{
            "miku","hatsune","初音","ミク","39",
    };
    /**
     * when tweetText.size >= mustContainLength
     * and tweetText doesn't contain one word in mustContainOne,execute markDontLike
     * */
    public static final int mustContainLength = 200;
    public static final String[] blackListStrings = new String[]{
            "虹夏", "ぼっち","apex","小春と湊","ドン・キホーテ","meiko","gumi","月雪ミヤコ",
            "more more jump","歌い手","足立レイ","vtuber","abydos","utau","鏡音","琴葉","結月ゆかり",
            "ウマ娘","ずんだもん"," ia ","喜多","御坂美琴","東雲絵名","原神","編み物","crochet","食品サンプル",
            "foodsample","塩音ルト","重音テト"
    };//remember to use lowercase
    public static final String[] notStrictStrings = new String[]{
            "誕生祭20","cat","百合"
    };//when mustContainOne found none,use this
    public static String[] blackListNames = new String[]{
            "tuber",
    };
    public static String[] blackListID = new String[]{
            "tuber"
    };

    public static HashSet<String> blackListTags;//once found,disable
    public static HashSet<String> notStrictTags;//when mustContainOne found none,use this
    static {
        String[] blackListTagsArray = new String[]{
                "鏡音", "巡音ルカ", "Gumi", "GUMI", "AI","重音テト", "Genshin", "MyGo", "vtuber",
                 "ぼっち・ざ・ろっく", "クラにか", "鏡音リン", "ウマ娘", "ねんどり", "TGCF", "天官賜福",
                "天官赐福", "ねんどろいど刀剣男士", "twstドール部", "アーニャ", "prsk",
                "ねんどろいどどーる", "西住みほ", "MEIKO", "KAITO", "小さなラブライブ", "歌ってみた",
                "DokiWaifuCupContest","DokiGallery","APEX","メズマライザー","純愛コンビ","小山芳姫", "八十八夜",
                "このタグを見た人は黙って魔法使いをあげる","アニメ好きと繋がりたい","VRM_live_viewer","滲音かこい",
                "愛奏さん","GWはフォロワーさんが増えるらしい","澁谷かのん","小春と湊","ドン・キホーテ",
                "4月を写真4枚で振り返る","水銀燈","suigintou","mercurylampe","銀様","doll","ドール","ぷにコレ",
                "メガハウス","MegaHouse","鈍川まなみ","千歌Qi","音街ウナ",
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
                "エヴァンゲリオン","ちいかわ","AIイラスト","AIgirl","AIart","prsk_FA",
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
                "鬼滅の刃","竈門禰󠄀豆子","アクアシューターズ","うさりこちゃんをすこれ",
                "東雲絵名","足立レイ","東方Project","ずんちゃんモチモチ木曜日","東北ずん子","VRMLiveViewer",
                "水橋パルスィ","ikamusume","イカ娘","teibo","放課後ていぼう日誌","Luchina_sisters",
                "ahsoft","フリモメン","人マニア","いいからパンツ見せろ","MyXAnniversary","とみーのどぼん",
                "アゾカツ","アイコレプチ","のんびり生きるネコ","ジョギング","緑茶の日","asterian_synthv",
                "鶴居村","鶴居いづる","くろりょこう","かちまちチャレンジ","晴ちゃん誕生日おめでとう",
                "NIKKEcosplay","ニコニコ超会議2024","ニコニコ超会議コスプレ","ニコニコ超会議","埼玉県",
                "久喜市","新聞配達","柴犬","道志渓谷","野原の吊り橋","新緑",
                "繋がらなくていいから俺のフランドール・スカーレットを見てくれ","あん娘","MDDはいいぞ",
                "東雲絵名生誕祭2024","VALORANT","御茶ノ愛","めっとこ","とちてれアニメフェスタ",
                "ゼルダの伝説","劇場版すとぷりはじまりの物語で1番好きなシーン","とべ動物園","ジャガー","下諏訪町",
                "farcille","dungeonmeshi","アークナイツ","明日方舟","Arknights","twstプラス","twst夢",
                "helltaker","IA","塩音ルト","ユアマジェ","Bocchi","デザフェス59", "ワンコインランウェイ",

        };
        blackListTags = new HashSet<>(Arrays.asList(blackListTagsArray));

        String[] notStrictTagsArr = new String[]{
                "深夜のねんどろ撮影６０分一本勝負","絵描きさんと繋がりたい","絵柄が好きっていう人にフォローされたい",
                "DTM無料","深夜のねんどろ撮影60分一本勝負","ねんどろいど","みくさんぽ","figmaster","なまらってぃ",
                "悪ノ強化月間","悪ノ創作","妖怪大行進","ニコニコ動画","ぬいぐるみの時間","ふわぷち",
                "このタグを見た人は黙って武器を持ったキャラを貼る","goodsmile","ねんどろいど","性癖パネルトラップ",
                "ホココス2024","敦賀市立博物館","XRAnimator","愛三電機","vocaloid","ISKAgallery",
                "おひBUNNY","EmoismGathering","에모게더","電音部","ESP_Japan","DTM",
                "DTMerさんと繋がりたい","GarageBand",

        };
        notStrictTags = new HashSet<>(Arrays.asList(notStrictTagsArr));
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
                FindingClass = XposedHelpers.findClassIfExists("pae$a",lpparam.classLoader);//blocker
                if (FindingClass != null){

//                    Constructor<?> UserIdentifierConstructor = XposedHelpers.findConstructorExact("UserIdentifier",lpparam.classLoader,long.class);
//                    Object userIdentifier = UserIdentifierConstructor.newInstance(114514L);

                    Class<?> h19Cls = XposedHelpers.findClass("h19$a",lpparam.classLoader);
                    Constructor<?> h19Constructor = XposedHelpers.findConstructorExact(h19Cls);
//                    Class<?> UserIdentifierClass = XposedHelpers.findClass("com.twitter.util.user.UserIdentifier",lpparam.classLoader);
                    Class<?> vjsClass = XposedHelpers.findClass("vjs",lpparam.classLoader);
//                    Constructor<?> hksConstructor = XposedHelpers.findConstructorExact("hks",lpparam.classLoader,Context.class,UserIdentifierClass,vjsClass,boolean.class,h19Cls,int.class,boolean.class);
                    XposedBridge.hookAllMethods(FindingClass, "a", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            LoggerLog(new Exception("not an exception"));
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
//                            showArgs(param.args);

//                            for (Object arg:param.args){
//                                searchStringInObjectFields(arg,"    ","whs");
//                            }
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
                                        Object userIDObj = userNameObj;
                                        if (userNameObj != null){
                                            f_user = XposedHelpers.findFieldIfExists(userNameObj.getClass(),"q");
                                            if (f_user != null){
                                                userNameObj = f_user.get(userNameObj);
                                                if (userNameObj instanceof String){
                                                    userName = (String) userNameObj;
                                                }
                                            }
                                            f_user = XposedHelpers.findFieldIfExists(userIDObj.getClass(),"d");
                                            if (f_user != null){
                                                userIDObj = f_user.get(userIDObj);
                                                if (userIDObj instanceof String){
                                                    userID = (String) userIDObj;
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
//                                    Context currentAppContext = AndroidAppHelper.currentApplication();
                                    if (checkDontLike(blackListTags, tagList, blackListStrings,
                                            blackListNames, blackListID,
                                            userName, userID, tweetText,
                                            mustContainOne,mustContainLength,
                                            notStrictStrings,notStrictTags)) {
//                                        Object yva$c_obj = XposedHelpers.callStaticMethod(CurationViewDelegateBinder_Class,"d",vjsobj);
                                        List<?> yva$c_list = (List<?>) XposedHelpers.getObjectField(XposedHelpers.callMethod(vjsobj,"c"),"s");
//                                        showObjectFields(yva$c_list,"    ");
                                        if (yva$c_list.isEmpty()){return;}
                                        markDontLikeMethods.bpt_j(yva$c_list.get(0), vjsobj, h19Constructor);
                                        return;
                                    }
                                    if (showUserNameAndID && showTextFlag){
                                        tweetText = "username:" + userName + "    userID:" + userID + "\n" + tweetText;
                                        if (!newTags.contains(tweetText)) {
                                            newTags.add(tweetText);
                                            if (showTextFlag){
                                                LoggerLog(tweetText);
                                            }
                                        }
                                    }else if (showUserNameAndID){
                                        if (!tweetText.isEmpty()){
                                            if (!newTags.contains(userName)) {
                                                newTags.add(userName);
                                                LoggerLog("username:" + userName + "    userID:" + userID);
                                            }
                                        }
                                    }else {
                                        if (!tweetText.isEmpty()){
                                            if (!newTags.contains(tweetText)) {
                                                newTags.add(tweetText);
                                                if (showTextFlag){
                                                    LoggerLog(tweetText);
                                                }
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

                FindingClass = XposedHelpers.findClassIfExists("whs",lpparam.classLoader);
                if (FindingClass != null){
                    XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
//                            showObjectFields(param.args[3],"    ");
                            objPool.add(param.thisObject);
//                            LoggerLog(param.args[0].getClass());
                        }
                    });
                }



            }catch (Exception e){
                LoggerLog(e);
            }
        }
    }

    //true:Don't like
    public static boolean checkDontLike(
            HashSet<String> blackListTags,List<String> tags,String[] blackListWords,
            String[] blackListNames,String[] blackListID,String username,
            String userID,String tweetText, String[] mustContainOne,int mustContainLength,String[] notStrictStrings,HashSet<String> notStrictTags){
        if (!markDontLikeFlag){return false;}
        String checking = tweetText + userID + username;
        boolean mustContainFlag = false;
        for (String toCheck:mustContainOne){
            if (checking.toLowerCase().contains(toCheck)){
                mustContainFlag = true;
                break;
            }
        }
        if (!mustContainFlag){
            if (tweetText.length() >= mustContainLength
                    || !tweetText.contains("/")
                //AcdeAsdff cannot read japanese:
                // all "pure text"(without a link) without miku will be ignored
            ){
                return true;
            }
            for (String tag:tags){
                if (notStrictTags.contains(tag)){
                    return true;
                }
            }
            for (String s:notStrictStrings){
                if (checking.contains(s)){return true;}
            }
        }

//        {
//            //warning:strict mode!
//            //you may get banned for this!!!!!!(not proofed)
//            tweetText += username += userID;
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
        username = username.toLowerCase();
        for (String toCheck:blackListNames){
            if (username.contains(toCheck)){
                LoggerLog("matched Name:" + toCheck);
                return true;
            }
        }
        userID = userID.toLowerCase();
        for (String toCheck:blackListID){
            if (userID.contains(toCheck)){
                LoggerLog("matched ID:" + toCheck);
                return true;
            }
        }
        for (String tag:tags){
            if (blackListTags.contains(tag)) {
                LoggerLog("matched tag:" + tag);
                return true;
            }
        }
        return false;
    }

}

