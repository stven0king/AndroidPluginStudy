package com.tzx.ams.plugin;

import com.android.utils.Pair;

import java.util.List;

/**
 * Created by Tanzhenxing
 * Date: 2020-03-02 22:14
 * Description:
 */
public class AmsConfig {
    //日志开关
    public boolean isDebug;
    //class包含str则不处理
    public String[] filterContainsClassStr;
    //class以str开头则不处理
    public String[] filterstartsWithClassStr;
    //拦截在这个文件中声明的class
    public String filterClassFile;
    public List<String> filterClassNameList;
    //需要进行注入的method
    public String amsMethodFile;
    //需要进行注入的method对应的tag
    public String amsMethodTag;
    public List<Pair<String, String>> amsMethodFileList;
}
