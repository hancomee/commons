package com.boosteel;
import com.boosteel.util.support.Range;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RangeTest {


    @Test
    public void run() throws Exception {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        out(format.parse("2019-3-1"));


    }



    private void out(Object obj) {
        System.out.println(obj);
    }
}
