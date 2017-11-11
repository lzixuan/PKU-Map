package com.example.macpro.pku_map;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {

    /**
     * 是否显示欢迎界面,true表示显示，false表示不显示
     */
    public static final String SHOW_GUIDE = "showguide";
    public static final String[] place = {"第一教学楼", "第二教学楼", "第三教学楼",
            "第四教学楼", "理科教学楼", "光华管理学院", "新闻与传播学院", "化学学院",
            "李兆基人文学苑", "五四体育场", "邱德拔体育馆", "第二体育场", "第一体育场",
            "图书馆", "文史楼", "百周年纪念讲堂", "新太阳学生活动中心", "农园食堂",
            "学一食堂", "学五食堂", "燕南食堂", "最美时光咖啡厅", "松林食堂", "艺园 ",
            "勺园食堂", "国际关系学院", "静园草坪", "湖心岛", "花神庙", "博雅塔"
    };
    public static final double[] coordinateX = {116.316864, 116.319999, 116.320438,
            116.320718, 116.319388, 116.318723, 116.318427, 116.323877,
            116.31821, 116.320106, 116.321724, 116.314788, 116.318085,
            116.316801, 116.318309, 116.318309, 116.317304, 116.318778,
            116.314268, 116.319512, 116.316657, 116.314627, 116.314474, 116.313581,
            116.311913, 116.313136, 116.314698, 116.316023, 116.316607, 116.318328
    };
    public static final double[] coordinateY = {39.998833, 39.995662, 39.994983,
            39.995265, 39.997368, 39.99665, 39.992994, 39.997252,
            40.002826, 39.993838, 39.99456, 39.996808, 40.001085,
            39.997899, 39.998174, 39.998174, 39.994928, 39.994481,
            39.993495, 39.992007, 39.996587, 39.994981, 39.993818, 39.994152,
            39.997601, 39.998038, 39.997768, 40.00074, 40.000053, 39.999917
    };

    /**
     * 保存到Preference
     */
    public static void setBoolean(Context context, String key, boolean value) {
        // 得到SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences(
                "preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 从Preference取出数据
     */
    public static boolean getBoolean(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(
                "preference", Context.MODE_PRIVATE);
        // 返回key值，key值默认值是false
        return preferences.getBoolean(key, false);

    }
}