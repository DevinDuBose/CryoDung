package com.cryo.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tester {

    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<String>() {{
            add("test");
            add("test2");
            add("tester");
            add("testaloo");
        }};
        update(strings);
        System.out.println(strings);
    }

    public static void update(List<String> strings) {
        for (Iterator<String> it = strings.iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
            return;
        }
    }
}
