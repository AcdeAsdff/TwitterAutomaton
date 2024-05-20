package com.linearity.twitterautomaton;

import static com.linearity.twitterautomaton.TwitterTweak.objPool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.robv.android.xposed.XposedHelpers;

public class markDontLikeMethods {
    public static final void bpt_j(Object cVar, Object vjsVar, Constructor<?> h19Constructor) throws InvocationTargetException, IllegalAccessException, InstantiationException {
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
        Object aVar = h19Constructor.newInstance();
        XposedHelpers.setIntField(aVar,"c",4);
        XposedHelpers.setIntField(aVar,"c",4);
        long cVar_c = XposedHelpers.getLongField(cVar,"c");
        XposedHelpers.setLongField(aVar,"d",cVar_c);
        Object o = XposedHelpers.findMethodExact(aVar.getClass().getSuperclass(),"o").invoke(aVar);
        if (z) {
            i = 2;
        }
        for (Object disabler:objPool){
            bpt_r(vjsVar, o, i, disabler);
        }
    }

    public static boolean e58_t(int i) {
        boolean z;
        boolean z2;
        boolean z3;
        z = (i & 6) != 0;
        if (z) {
            z2 = (i & 16) != 0;
            if (z2) {
                z3 = (i & 32) != 0;
                return z3;
            }
        }
        return false;
    }

    public static final void bpt_r(Object vjsVar, Object h19Var, int i, Object whsVar) throws InvocationTargetException, IllegalAccessException {
        if (whsVar != null && vjsVar != null) {
            XposedHelpers.findMethodBestMatch(whsVar.getClass(),"a",vjsVar.getClass().getSuperclass(),h19Var.getClass(),int.class).invoke(whsVar,vjsVar, h19Var, i);
        }
    }
}
