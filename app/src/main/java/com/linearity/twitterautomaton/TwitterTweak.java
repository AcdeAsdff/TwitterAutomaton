package com.linearity.twitterautomaton;

import static com.linearity.twitterautomaton.utils.LoggerLog;
import static com.linearity.twitterautomaton.utils.showArgs;
import static com.linearity.twitterautomaton.utils.showObjectFields;
import static com.linearity.twitterautomaton.utils.showObjectFields_noExpand;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    public static final boolean showTextFlag = true;
    public static final boolean showTagFlag = true;
    public static final boolean markDontLikeFlag = true;
    public static HashSet<String> newTags = new HashSet<>();
    public static Method[] keyMethods = new Method[2];
    public static Object[] objPool = new Object[1];

    public static final String[] mustContainOne = new String[]{
            "miku","hatsune","初音","ミク"
    };
    //when tweetText.size >= mustContainLength
    // and tweetText doesn't contain one word in mustContainOne,markDontLike
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
                "温泉閣","カナエちゃん","カナ絵アート","鹿屋園芸部","nijijourney",
        };
        blackListTags = new HashSet<>(Arrays.asList(blackListTagsArray));
    }
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.processName.contains("com.twitter.android")
            || lpparam.packageName.contains("com.twitter.android")
        ){
            LoggerLog("Hooked Twitter");
            try {
                Class<?> FindingClass;
//                FindingClass = XposedHelpers.findClassIfExists("nat",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllMethods(FindingClass,"h", new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            Object thisD = XposedHelpers.findField(param.thisObject.getClass(),"d").get(param.thisObject);
//                            Object thisQ = XposedHelpers.findField(param.thisObject.getClass(),"q").get(param.thisObject);
//                            Object dResult = XposedHelpers.findMethodExact(thisD.getClass(),"h",int.class).invoke(thisD,param.args[0]);
//                            Object qResult = XposedHelpers.findMethodBestMatch(thisQ.getClass(),"a",dResult.getClass()).invoke(thisQ,dResult);
//                            Object obj = qResult;
////                            showObjectFields(obj,"    ");
//                            Field f = XposedHelpers.findFieldIfExists(obj.getClass(),"k");
//                            if (f == null){
//                                return;
//                            }
//                            obj = f.get(obj);
//                            if (obj == null){return;}
//                            f = XposedHelpers.findFieldIfExists(obj.getClass(),"d");
//                            obj = f.get(obj);
//                            if (obj!=null && obj.getClass().getTypeName().equals("cql")) {
//                                showObjectFields(qResult,"    ");
////                                LoggerLog(new Exception("not an exception"));
////                                param.setResult(null);
////                                LoggerLog("ad cancelled");
////                                return;
//                            }
//                            param.setResult(qResult);
//                        }
//                    });
//                }

                Class Dontlike_yva$c_class = XposedHelpers.findClassIfExists("yva$c",lpparam.classLoader);
                Class Dontlike_yva$c$a_class = XposedHelpers.findClassIfExists("yva$c$a",lpparam.classLoader);
                Class Dontlike_e_class = XposedHelpers.findClassIfExists("cqu",lpparam.classLoader);
                Object Dontlike_e = null;
                if (Dontlike_e_class != null){
                    Object[] icons = Dontlike_e_class.getEnumConstants();
                    if (icons != null){
                        for (Object o:icons){
                            if (o.toString().equals("FROWN")){
                                Dontlike_e = o;
                                break;
                            }
                        }
                    }
                    if (Dontlike_e==null && icons != null && icons.length > 0){
                        Dontlike_e = icons[0];
                    }
                }

                if (Dontlike_yva$c$a_class != null){
                    Class Dontlike_yva$c$b_class = XposedHelpers.findClassIfExists("yva$c$b",lpparam.classLoader);
//                    Class o4j_class = XposedHelpers.findClassIfExists("o4j",lpparam.classLoader);
//
//                    XposedBridge.hookAllMethods(o4j_class, "a", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            Class e93_class = XposedHelpers.findClass("e93",lpparam.classLoader);
//                            Method e93_A = XposedHelpers.findMethodExact("e93",lpparam.classLoader,"A");
//                            if (e93_class.isInstance(param.args[0])) {
////                                LoggerLog(e93_A.invoke(param.args[0]));
//                                LoggerLog(param.getResult());
//                            }
//                        }
//                    });
                    

                }
                Class CurationViewDelegateBinder_Class = XposedHelpers.findClassIfExists("com.twitter.tweetview.core.ui.curation.CurationViewDelegateBinder",lpparam.classLoader);

                FindingClass = XposedHelpers.findClassIfExists("pae$a",lpparam.classLoader);//ad blocker
                if (FindingClass != null){
                    Object finalDontlike_e = Dontlike_e;
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
                            Object vx6obj = obj;
                            f = XposedHelpers.findFieldIfExists(obj.getClass(),"d");if (f == null){return;}
                            Object obj1 = f.get(obj);
                            if (obj1!=null && obj1.getClass().getTypeName().equals("cql")) {
                                param.setResult(null);
                                LoggerLog("ad cancelled");
                                return;
                            }
                            {
                                {
                                    Object vjsobj = param.args[2];
                                    String tweetText = "";
                                    ArrayList<String> tagList = new ArrayList<>();
                                    f = XposedHelpers.findFieldIfExists(obj.getClass(),"c");if (f==null){return;}
                                    obj = f.get(obj);if (obj == null){return;}
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
                                    List tagEntities = (List) obj;
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
                                        if (checkDontLike(blackListTags, tagList, blackListWords, blackListNames, blackListID, "", "", tweetText,mustContainOne,mustContainLength)) {
                                            Object yva$c_obj = XposedHelpers.callStaticMethod(CurationViewDelegateBinder_Class,"d",param.args[2]);
                                            bpt_j(vx6obj, yva$c_obj, vjsobj,objPool[0],lpparam.classLoader);
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
//
//                FindingClass = XposedHelpers.findClassIfExists("vm",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            for (Object o:(List)param.args[0]){
//                                showObjectFields(o,"        ");
//                            }
//                            showObjectFields(param.args[1],"        ");
//                        }
//                    });
//                }

//                FindingClass = XposedHelpers.findClassIfExists("bn",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            LoggerLog(new Exception("not an exception"));
//                            showArgs(param.args);
//                        }
//                    });
//                }

//                FindingClass = XposedHelpers.findClassIfExists("xm",lpparam.classLoader);
//                if (FindingClass != null){
////                    XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
////                        @Override
////                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                            super.beforeHookedMethod(param);
//////                            showObjectFields(param.args[1],"    ");
////                            LoggerLog(param.args[1]);
////                            LoggerLog(param.args[1].getClass().getTypeName());
////                            XposedHelpers.findMethodExact(param.args[1].getClass(),"h2").invoke(param.args[1]);
////                        }
////                    });
//                    XposedBridge.hookAllMethods(FindingClass,"onClick", new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
////                            LoggerLog(new Exception("not an exception"));
////                            Field f = XposedHelpers.findField(param.thisObject.getClass(),"k3");
////                            showObjectFields(f.get(param.thisObject),"    ");
//                            Field f_k3 = XposedHelpers.findField(param.thisObject.getClass(),"k3");
//                            Class cls_sm = XposedHelpers.findClass("sm",lpparam.classLoader);
//                            Object o = f_k3.get(param.thisObject);
//                            cls_sm.cast(o);
////                            XposedHelpers.findMethodExact(o.getClass().getSuperclass().getSuperclass().getSuperclass(),"q2",int.class).invoke(o,0);
////                            XposedHelpers.findMethodExact(o.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass(),"h2").invoke(o);
////                            param.setResult(null);
//
//                            Object o1 = XposedHelpers.findMethodExact(o.getClass().getSuperclass().getSuperclass().getSuperclass(),"p2").invoke(o);
//                            LoggerLog(((Bundle)XposedHelpers.findField(o1.getClass(),"a").get(o1)));
//                        }
//                    });
//                }

//                FindingClass = XposedHelpers.findClassIfExists("bi7",lpparam.classLoader);
//                if (FindingClass != null){
//                    XC_MethodHook after = new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        Object obj_tweetViewViewModel = XposedHelpers.findField(param.thisObject.getClass(),"c").get(param.thisObject);
//                        Object obj_tweetViewViewModel_x = XposedHelpers.findField(obj_tweetViewViewModel.getClass(),"x").get(obj_tweetViewViewModel);
//                        Object obj_tweetViewViewModel_c = XposedHelpers.findField(obj_tweetViewViewModel_x.getClass(),"c").get(obj_tweetViewViewModel_x);
//                        Object obj_tweetViewViewModel_a = XposedHelpers.findMethodExact(obj_tweetViewViewModel_c.getClass(),"get").invoke(obj_tweetViewViewModel_c);
//                        if (obj_tweetViewViewModel_a == null){LoggerLog("obj_tweetViewViewModel_a:null");return;}
//                        Object obj_m1uVar = XposedHelpers.findField(obj_tweetViewViewModel_a.getClass(),"f").get(obj_tweetViewViewModel_a);
//                        Object obj_vx6Var = XposedHelpers.findField(obj_tweetViewViewModel_a.getClass(),"a").get(obj_tweetViewViewModel_a);
//                        showObjectFields(obj_m1uVar,"    ");
//                    }
//                };
//                    XposedBridge.hookAllMethods(FindingClass,"invoke", after );
//                    XposedBridge.hookAllConstructors(FindingClass, after);
////                    XposedBridge.hookAllMethods(FindingClass, "n", new XC_MethodHook() {
////                        @Override
////                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
////                            super.afterHookedMethod(param);
////                            LoggerLog(new Exception("not an exception"));
//////                            LoggerLog(param.getResult().getClass().getTypeName());
////                        }
////                    });
//                }


//                FindingClass = XposedHelpers.findClassIfExists("yva$c",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            showObjectFields_noExpand(param.thisObject,"    ");
////                            LoggerLog(new Exception("not an exception"));
////                            LoggerLog(param.getResult().getClass().getTypeName());
//                        }
//                    });
//                }
//                FindingClass = XposedHelpers.findClassIfExists("yva$c$b",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllMethods(FindingClass, "i", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            showObjectFields(param.args[0],"    ");
//                        }
//                    });
//                }

//                        FindingClass = XposedHelpers.findClassIfExists("e93",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllMethods(FindingClass, "A", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            if (param.getResult().equals(35501L)){
//                                LoggerLog(new Exception("not an exception"));
//                            }
//                        }
//                    });
//                }

//                Class Dontlike_g_Class = XposedHelpers.findClassIfExists("yva$c$b",lpparam.classLoader);//f:null

                 FindingClass = XposedHelpers.findClassIfExists("fot",lpparam.classLoader);
                 if (FindingClass != null){
                     Object finalDontlike_e = Dontlike_e;
                     XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
                         @Override
                         protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                             super.afterHookedMethod(param);
                             if (param.args.length == 20){
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
                                List tagEntities = (List) obj;
                                for (Object o:tagEntities){
                                    Field field = XposedHelpers.findFieldIfExists(o.getClass(),"y");
                                    if (field != null && field.getType().isInstance("")){
                                        String tag = (String) field.get(o);
                                        if (tag != null && !tag.isEmpty()){
                                            tagList.add(tag);
                                        }
                                    }
                                }
                                if (checkDontLike(blackListTags,tagList,blackListWords,blackListNames,blackListID,"","",tweetText,mustContainOne,mustContainLength)){
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
//                FindingClass = XposedHelpers.findClassIfExists("x5w$a",lpparam.classLoader);
//                if (FindingClass != null){
//                    XposedBridge.hookAllConstructors(FindingClass, new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            Object q = XposedHelpers.findField(param.thisObject.getClass(),"q").get(param.thisObject);
//                            Class yzu = XposedHelpers.findClass("yzu", lpparam.classLoader);
//                            Method m = XposedHelpers.findMethodBestMatch(q.getClass(),"onNext",yzu);
//                        }
//                    });
//                }


            }catch (Exception e){
                LoggerLog(e);
            }
        }
    }


    public static Object markAsDontLike_requestObject(long id, Object DontLike_e, Class Dontlike_yva$c_class, Class Dontlike_yva$c$a_class) throws Exception{
        Object yva$c$a_obj = XposedHelpers.findConstructorExact(Dontlike_yva$c$a_class).newInstance();
        XposedHelpers.findField(Dontlike_yva$c$a_class,"c").set(yva$c$a_obj,"DontLike");
        XposedHelpers.findField(Dontlike_yva$c$a_class,"d").set(yva$c$a_obj,"对此帖子不感兴趣");
        XposedHelpers.findField(Dontlike_yva$c$a_class,"q").setLong(yva$c$a_obj,id);
        XposedHelpers.findField(Dontlike_yva$c$a_class,"y").set(yva$c$a_obj,DontLike_e);
        XposedHelpers.findField(Dontlike_yva$c$a_class,"x").setInt(yva$c$a_obj,0);
        Object yva$c_obj = XposedHelpers.findConstructorExact(Dontlike_yva$c_class,Dontlike_yva$c$a_class).newInstance(yva$c$a_obj);
        return yva$c_obj;
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

    public final boolean bpt_j(Object vx6Var, Object cVar, Object vjsVar, Object bpt_l, ClassLoader classLoader) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        boolean z = false;
        int i = 1;
        Object hInside = XposedHelpers.findMethodExact(vjsVar.getClass().getSuperclass(),"c").invoke(vjsVar);
        int h = XposedHelpers.findField(hInside.getClass(),"h").getInt(hInside);
        if (vjsVar != null) {
            if (e58_t(h)){
                z = true;
            }
        }

//        String str = cVar.a;
////        Pattern pattern = g9r.a;
//        if (t9r.equalsWithCase("Moderate", str, true) && (vjsVar instanceof m1u)) {
//            s(this.h.get(), vx6Var);
//            return true;
//        }
        Class h19Cls = XposedHelpers.findClass("h19$a",classLoader);
        Object aVar = XposedHelpers.findConstructorExact(h19Cls).newInstance();
        XposedHelpers.setIntField(aVar,"c",4);
        XposedHelpers.setIntField(aVar,"c",4);
        XposedHelpers.setLongField(aVar,"d",XposedHelpers.getLongField(cVar,"c"));
        Object o = XposedHelpers.findMethodExact(aVar.getClass().getSuperclass(),"o").invoke(aVar);
        if (z) {
            i = 2;
        }
        return bpt_r(vjsVar, o, i, objPool[0]);
    }

    public static boolean e58_t(int i) {
        boolean z;
        boolean z2;
        boolean z3;
        if ((i & 6) != 0) {
            z = true;
        } else {
            z = false;
        }
        if (z) {
            if ((i & 16) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (z2) {
                if ((i & 32) != 0) {
                    z3 = true;
                } else {
                    z3 = false;
                }
                if (z3) {
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean bpt_r(Object vjsVar, Object h19Var, int i, Object whsVar) throws InvocationTargetException, IllegalAccessException {
        if (whsVar != null && vjsVar != null) {
            XposedHelpers.findMethodBestMatch(whsVar.getClass(),"a",vjsVar.getClass().getSuperclass(),h19Var.getClass(),int.class).invoke(whsVar,vjsVar, h19Var, i);
            return true;
        }
        return false;
    }
}

