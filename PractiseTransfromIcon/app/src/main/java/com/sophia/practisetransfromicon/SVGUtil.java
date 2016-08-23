package com.sophia.practisetransfromicon;

import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user90 on 2016/8/23.
 */
public class SVGUtil {

    private static volatile SVGUtil svgUtil;
    private Set<String> svgCommandSet;
    private String[] command={"M","L","H","V","C","S","Q","T","A","Z"};

    private SVGUtil(){
        svgCommandSet=new HashSet<String>();
        for (String cmd:command){
            svgCommandSet.add(cmd);
        }
    }

    public static SVGUtil getInstance(){
        if (svgUtil==null){
            synchronized (SVGUtil.class){
                if (svgUtil==null){
                    svgUtil=new SVGUtil();
                }
            }
        }
        return svgUtil;
    }

    static class FragmentPath{
        //记录当前path片段的命令
        public PathType pathType;
        //数据占用长度，同样是Line ，
        //V/H与L后面携带的数据长度不同，
        //这里需要记录
        public int dataLen;
        public Point p1;
        public Point p2;
        public Point p3;

    }

    static enum PathType{
        MOVE,LINE_TO,CURVE_TO,QUAD_TO,CLOSE
    }

    public ArrayList<String> extractSvgData(String svgData){
        //以下为了将命令字母两边添加空格
        //保存已经替换过的字母
        Set<String> hasReplaceSet=new HashSet<String>();
        //正则表达式，用于匹配path里面的字母
        Pattern pattern=Pattern.compile("[a-zA-Z]");
        Matcher matcher=pattern.matcher(svgData);
        //遍历匹配正则表达式的字符串
        while (matcher.find()){
            //s为匹配的字符串
            String s=matcher.group();
            //如果该字符串没有替换，则在改字符串两边加空格
            if (!hasReplaceSet.contains(s)){
                svgData=svgData.replace(s," "+s+" ");
                hasReplaceSet.add(s);
            }
        }
        //--end--命令字母两边添加字母结束---
        //讲“，”替换为“ ”，并强制转为大写字母
        svgData=svgData.replace(","," ").trim().toUpperCase();
        //以" "为分隔符分隔字符串
        String[] ss=svgData.split(" ");
        //将最终分割成的字符串数组转为List
        ArrayList<String> data=new ArrayList<String>();
        for (String s:ss){
            //只有当前的字符串不是空格，才将该字符串加入到List中
            //相当于实现了自动删除多余的空格
            if (s!=null&&!"".equals(s)){
                data.add(s);
            }
        }
        return data;
    }

    //根据ArrayList保存的数据，讲path数据转为Android中的Path对象
    //widthFactor,宽度放缩倍数
    //heightFactor,高度放缩倍数
    public Path parsePath(ArrayList<String> svgDataList,float widthFactor,float heightFactor){
        //new一个需要返回的path对象
        Path path=new Path();
        //解析字符串偏移位置
        int startIndex=0;
        //上一次绘制的终点，默认为左上角
        Point lastPoint=new Point(0,0);
        //提取下一条FragmentPath对象
        FragmentPath fp=nextFrag(svgDataList,startIndex,lastPoint);
        //如果下一条FragmentPath不为null，则循环
        while (fp!=null){
            //根据命令类型，执行Path的不同方法，主要，所有的坐标需要乘以放缩倍数


        }
    }
}
