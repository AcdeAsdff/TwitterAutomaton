package com.linearity.twitterautomaton;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;

public class utils {

    public static final boolean useLogger = true;

    public static void LoggerLog(Object log){
        if (log != null){
            LoggerLog("[linearity]", log.toString());
        }else {LoggerLog("[linearity]", "null");}
    }

    public static void LoggerLog(String log){
        LoggerLog("[linearity]", log);
    }

    public static void LoggerLog(Throwable e){
        LoggerLog("[linearity]", e);
        for (StackTraceElement s:e.getStackTrace()){
            LoggerLog("        at "+s);
        }
        LoggerLog("--------------------------------");
    }

    public static void LoggerLog(String prefix, String log){
        if (useLogger){
            XposedBridge.log(prefix + log);//not best?
        }
    }

    public static void LoggerLog(String prefix, Throwable e){
        if (useLogger){
            XposedBridge.log(prefix + e);//not best?
        }
    }

    public static void showChildViewGroups(ViewGroup viewGroup, String pre){
        for (int i=0;i<viewGroup.getChildCount();i++){
            View child = viewGroup.getChildAt(i);
            LoggerLog(pre + i + ":"  + child.toString());
            if(child instanceof ViewGroup){
                showChildViewGroups((ViewGroup) child,"ChildOf" + pre);
            }else if (child instanceof TextView){
                LoggerLog("text:" + ((TextView)child).getText());
            }
        }
    }

    public static void showArgs(Object[] args){
        HashSet<Object> filter = new HashSet<>();
        LoggerLog("---item start---");
        for (Object o: args){
            LoggerLog("object:");
            LoggerLog(o);
            if (o==null){continue;}
            LoggerLog("fields:");
            String objTypeName = o.getClass().getTypeName();
            String objTypeNameLower = objTypeName.toLowerCase();
            if (objTypeNameLower.contains("java")
                    || objTypeNameLower.contains("integer")
                    || objTypeNameLower.contains("long")
                    || objTypeNameLower.contains("byte")
                    || objTypeNameLower.contains("boolean")
                    || objTypeNameLower.equals("int")){continue;}
            for (Field f:o.getClass().getDeclaredFields()){
                String typeName = f.getType().getTypeName();
                String typeNameLower = typeName.toLowerCase();
                try {
                    LoggerLog("     fieldName:" + f.getName() + "     " + f.get(o) + "      " + typeName);
                    if (!typeNameLower.contains("java")
                            && !typeNameLower.contains("integer")
                            && !typeNameLower.contains("long")
                            && !typeNameLower.contains("byte")
                            && !typeNameLower.contains("boolean")
                            && !typeName.equals("int")
                            && !typeNameLower.startsWith("android")
                    ){
                        showObjectFields(f.get(o),"          ",filter);
                    }
                }catch (Exception e){
                    LoggerLog("     cannotAccess(" + typeName + ")");
                }
            }
            LoggerLog(" ----fields end-----");
        }
        LoggerLog("---------");
    }

    public static void showObjectFields_noExpand(Object obj, String prefix) {
        if (obj==null){return;}

        LoggerLog(prefix + " fields:");
        for (Field f:obj.getClass().getDeclaredFields()){
            try {
                String typeName = f.getType().getTypeName();
                String typeNameLower = typeName.toLowerCase();
                Object fobj = f.get(obj);
                LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + fobj + "      " + typeName);
                if (!typeNameLower.contains("java")
                        && !typeNameLower.contains("integer")
                        && !typeNameLower.contains("long")
                        && !typeNameLower.contains("byte")
                        && !typeNameLower.contains("boolean")
                        && !typeNameLower.equals("int")
                        && !typeNameLower.startsWith("android")
                ){
                    if (fobj.getClass().isArray()){
                    }
                } if (fobj instanceof Collection){
                    LoggerLog(prefix + "collection size:" + ((Collection)fobj).size());
                }
            }catch (Exception e){
                LoggerLog(prefix + "     cannotAccess(" + f.getType().toString() + ")");
            }
        }
//        LoggerLog(prefix + " ----fields end-----");
    }

    public static void showObjectFields(Object obj, String prefix){
        showObjectFields(obj, prefix,new HashSet<Object>());
    }

    public static void showObjectFields(Object obj, String prefix, HashSet<Object> filter) {
        if (obj==null){return;}

        LoggerLog(prefix + " fields:");
        for (Field f:obj.getClass().getDeclaredFields()){
            try {
                String typeName = f.getType().getTypeName();
                String typeNameLower = typeName.toLowerCase();
                Object fobj = f.get(obj);
                if (!typeNameLower.contains("java")
                        && !typeNameLower.contains("integer")
                        && !typeNameLower.contains("long")
                        && !typeNameLower.contains("byte")
                        && !typeNameLower.contains("boolean")
                        && !typeNameLower.equals("int")
                        && !typeNameLower.startsWith("android")
                ){
                    LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + fobj + "      " + typeName);
                    if (filter.contains(fobj)){
//                        LoggerLog("--contained:"+fobj);
                        continue;
                    }
                    filter.add(fobj);
                    showObjectFields(fobj,prefix + "     ",filter);
                    if (fobj.getClass().isArray()){
                        int len = Array.getLength(fobj);
                        LoggerLog(prefix + "array length:" + len);
                        for (int i=0;i<len;i++){
                            Object o = Array.get(fobj,i);
                            if (o==null){continue;}
                            if (filter.contains(o)){continue;}
                            filter.add(o);
                            showObjectFields(o,prefix + "     ",filter);
                        }
                    }
                }
                else {
                    if (f.getType().equals(long.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getLong(obj) + "      " + typeName);
                    }else if (f.getType().equals(int.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getInt(obj) + "      " + typeName);
                    }else if (f.getType().equals(byte.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getByte(obj) + "      " + typeName);
                    }else if (f.getType().equals(boolean.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getBoolean(obj) + "      " + typeName);
                    }else if (f.getType().equals(short.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getShort(obj) + "      " + typeName);
                    }else if (f.getType().equals(double.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getDouble(obj) + "      " + typeName);
                    }else if (f.getType().equals(float.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getFloat(obj) + "      " + typeName);
                    }else if (f.getType().equals(char.class)){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getChar(obj) + "      " + typeName);
                    }
                }
                if (fobj instanceof Collection){
                    LoggerLog(prefix + "collection size:" + ((Collection)fobj).size());
                    for (Object o:(Collection)fobj){
                        if (o==null){continue;}
                        if (filter.contains(o)){continue;}
                        filter.add(o);
                        showObjectFields(o,prefix + "     ",filter);
                    }
                }
            }catch (Exception e){
                LoggerLog(prefix + "     cannotAccess(" + f.getType().toString() + ")");
            }
        }
        Class superClass = obj.getClass().getSuperclass();
        while (!Objects.equals(superClass, Object.class) && superClass != null){
            LoggerLog("superClass:" + superClass.getTypeName());
            for (Field f:superClass.getDeclaredFields()){
                try {
                    String typeName = f.getType().getTypeName();
                    String typeNameLower = typeName.toLowerCase();
                    Object fobj = f.get(obj);
                    if (!typeNameLower.contains("java")
                            && !typeNameLower.contains("integer")
                            && !typeNameLower.contains("long")
                            && !typeNameLower.contains("byte")
                            && !typeNameLower.contains("boolean")
                            && !typeNameLower.equals("int")
                            && !typeNameLower.startsWith("android")
                    ){
                        LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + fobj + "      " + typeName);
                        if (filter.contains(fobj)){
//                        LoggerLog("--contained:"+fobj);
                            continue;
                        }
                        filter.add(fobj);
                        showObjectFields(fobj,prefix + "     ",filter);
                        if (fobj.getClass().isArray()){
                            int len = Array.getLength(fobj);
                            LoggerLog(prefix + "array length:" + len);
                            for (int i=0;i<len;i++){
                                Object o = Array.get(fobj,i);
                                if (o==null){continue;}
                                if (filter.contains(o)){continue;}
                                filter.add(o);
                                showObjectFields(o,prefix + "     ",filter);
                            }
                        }
                    }
                    else {
                        if (f.getType().equals(long.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getLong(obj) + "      " + typeName);
                        }else if (f.getType().equals(int.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getInt(obj) + "      " + typeName);
                        }else if (f.getType().equals(byte.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getByte(obj) + "      " + typeName);
                        }else if (f.getType().equals(boolean.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getBoolean(obj) + "      " + typeName);
                        }else if (f.getType().equals(short.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getShort(obj) + "      " + typeName);
                        }else if (f.getType().equals(double.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getDouble(obj) + "      " + typeName);
                        }else if (f.getType().equals(float.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getFloat(obj) + "      " + typeName);
                        }else if (f.getType().equals(char.class)){
                            LoggerLog(prefix + "     fieldName:" + f.getName() + "     " + f.getChar(obj) + "      " + typeName);
                        }
                    }
                    if (fobj instanceof Collection){
                        LoggerLog(prefix + "collection size:" + ((Collection)fobj).size());
                        for (Object o:(Collection)fobj){
                            if (o==null){continue;}
                            if (filter.contains(o)){continue;}
                            filter.add(o);
                            showObjectFields(o,prefix + "     ",filter);
                        }
                    }
                }catch (Exception e){
                    LoggerLog(prefix + "     cannotAccess(" + f.getType().toString() + ")");
                }
            }
            superClass = superClass.getSuperclass();
        }
//        LoggerLog(prefix + " ----fields end-----");
    }
}