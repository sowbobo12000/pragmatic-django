package com.fresconews.fresco.v2.utils;

import java.util.List;

/**
 * Created by wumau on 9/29/2016.
 */

public class ListUtils {
    public static int getSize(List list) {
        if (list == null) {
            return 0;
        }
        return list.size();
    }
}
