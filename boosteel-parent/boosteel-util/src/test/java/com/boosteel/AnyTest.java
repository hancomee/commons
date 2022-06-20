package com.boosteel;


import com.boosteel.util.support.ReflectMap;
import org.apache.commons.net.ntp.TimeStamp;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AnyTest {


    @Test
    public void run() throws Exception {
        Object i = Integer.valueOf("1");
        out(i instanceof Number);
    }


    public void tester(TTT[] testers, int count) throws Exception {
        int i = 0;
        for (TTT tt : testers) {

            int c = count;
            long t = System.currentTimeMillis();
            while (c-- > 0) {
                tt.run();
            }
            out(i++ + ") " + (System.currentTimeMillis() - t));
        }
    }

    interface TTT {
        void run() throws Exception;
    }


    public void refrection() throws Exception {

        String s = "1";
        out(s.matches("\\d+"));
    }

    public long test(Runnable run, int loop, String name) {
        long s = System.currentTimeMillis();
        while (loop-- > 0) run.run();
        s = System.currentTimeMillis() - s;
        if (name != null)
            out(name + " : " + s);
        return s;
    }


    public static class A {
        String name;
        C c;

        public String getName() {
            System.out.println("뚜룹");
            return name;
        }
    }

    public static class B extends A {
        C c = new C();
        String address;

        public String getAddress() {
            return address;
        }
    }

    public static class C {

        public String music = "asdf";
    }


    public List<Map<String, Object>> magic() {
        return null;
    }

    public void out(Object obj) {
        System.out.println(obj);
    }
}
