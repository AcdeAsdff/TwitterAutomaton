package com.linearity.twitterautomaton;

import static com.linearity.twitterautomaton.utils.LoggerLog;
import static com.linearity.twitterautomaton.utils.showArgs;
import static com.linearity.twitterautomaton.utils.showObjectFields;

import java.util.ArrayList;
import java.util.Collection;

public class ObserverArrayList<T> extends ArrayList<T> {
    public ObserverArrayList(){
        super();
    }
    public ObserverArrayList(Collection<? extends T> c){
        super(c);
        showArgs(c.toArray());
    }
    public ObserverArrayList(int i){
        super(i);
    }

    @Override
    public boolean add(T t) {
//        LoggerLog(new Exception("not an exception"));
        showObjectFields(t,"additem:\t");
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
//        LoggerLog(new Exception("not an exception"));
        showObjectFields(element,"additem:\t");
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        showArgs(c.toArray());
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        showArgs(c.toArray());
        return super.addAll(index, c);
    }
}
