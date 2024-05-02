package com.linearity.twitterautomaton;

import java.lang.reflect.InvocationTargetException;

import de.robv.android.xposed.XposedHelpers;

public class markDontLikeMethods {
    public static final void bpt_j(Object cVar, Object vjsVar, ClassLoader classLoader) throws InvocationTargetException, IllegalAccessException, InstantiationException {
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
        Class<?> h19Cls = XposedHelpers.findClass("h19$a",classLoader);
        Object aVar = XposedHelpers.findConstructorExact(h19Cls).newInstance();
        XposedHelpers.setIntField(aVar,"c",4);
        XposedHelpers.setIntField(aVar,"c",4);
        XposedHelpers.setLongField(aVar,"d",XposedHelpers.getLongField(cVar,"c"));
        Object o = XposedHelpers.findMethodExact(aVar.getClass().getSuperclass(),"o").invoke(aVar);
        if (z) {
            i = 2;
        }
        bpt_r(vjsVar, o, i, TwitterTweak.objPool[0]);
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
