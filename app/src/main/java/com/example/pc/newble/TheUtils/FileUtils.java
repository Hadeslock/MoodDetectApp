package com.example.pc.newble.TheUtils;


import android.os.Environment;
import android.util.Log;

import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import java.io.FileWriter;

/**
 * Created by pc on 4/19/2019.
 */

public class FileUtils {

    private static final String TAG = "FileUtility";

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    //生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    //读取指定目录下的所有TXT文件的文件内容
    public static String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content;
    }


    public static String getSDCardPath(){
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdcardExist){
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }


    /** 此函数仅给 RetrieveData.java 下的读取人体电压用
     * 对于其他函数请勿使用，因为有可能造成数据丢失
     */
    public static Vector<Double> readVoltageFromFile(String file) {

        Vector<Double> savedDoubles = new Vector<Double>();
        double temp = 0;

        try {
            Log.e(TAG, "readVoltageFromFile: 进入try");
            BufferedReader in = new BufferedReader(new FileReader(file));
            Log.e(TAG, "readVoltageFromFile: 读取成功");
            String str = in.readLine();
            Log.e(TAG, "readVoltageFromFile: 读取行内内容成功：(第一行)" + str);
            str = in.readLine();
            Log.e(TAG, "readVoltageFromFile: 读取行内内容成功：(第二行)" + str);

            // TODO 可能会flush掉第一行的内容，根据后续设定再改

            while ((str = in.readLine()) != null) {
                Log.e("Retrieve.this QQQQQ", "readVoltageFromFile: "+temp + "   " + savedDoubles.size());
                if (str.isEmpty() == true){
                    // 本行是空行，直接跳过
                    continue;
                } else {
                    Log.e(TAG, "readVoltageFromFile: 哈哈哈哈哈哈哈哈 呜呜呜呜呜呜 哈哈哈哈哈哈" + str );
                    Log.e(TAG, "readVoltageFromFile: 哈哈哈哈哈哈哈哈哈" );
                    Log.e(TAG, "readVoltageFromFile: 6666666" );
                    try{
                        temp = Double.parseDouble(str);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    savedDoubles.add(temp);
                }
            }
            //最后一行的处理：似乎永远是空格？
            //System.out.println(str);
        } catch (java.io.IOException e) {
            Log.e(TAG, "readVoltageFromFile: 读取出现了错误！！！" );
        }
        Log.e(TAG, "readVoltageFromFile: 读取函数的读取结束" );
        return savedDoubles;
    }


    /**
     * 将一个txt文件内的内容按行读取，并将结果存到一个Vector中返回
     * */
    public static Vector<String> readTextFromFile(String file) {
        Vector<String> savedStrings = new Vector<String>();
        String str = "";

        try {

            BufferedReader in = new BufferedReader(new FileReader(file));

            while ((str = in.readLine()) != null) {

                if (str.isEmpty() == true){
                    // 本行是空行，直接跳过
                    continue;
                } else {
                    savedStrings.add(str);
                }
            }
            //最后一行的处理：似乎永远是空格？
            //System.out.println(str);
        } catch (java.io.IOException e) {

        }
        Log.e(TAG, "readVoltageFromFile: 读取函数的读取结束" );
        return savedStrings;
    }

    /**
     * 使用 FileWriter 方法向 filename 追加 content 内容的函数。
     * 与本类中 writeTextToFile 的区别在于，本函数是以追加的形式在文件后写入内容。
     */
    public static void addStringToFile(String fileName, String content) {
        try {
            // TODO 如果不存在的话，新建一个


            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 得到某一目录下所有文件名，并返回包含这些文件名的 Vector。
     * */
    public static Vector<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null){
            Log.e("error","空目录");
            return null;
        }
        Vector<String> s = new Vector<>();
        for(int i =0; i<files.length; i++){
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }


    /**
     * 根据 /bletest/ 目录下的内容，自动生成更新后的 DataList.txt 的函数。
     * 注：本函数的使用位置有些奇怪，因为 MainActivity 里的函数有些叠床架屋，故放到了调用 ChooseHistActivity 时
     *    触发。同时，这样做也带来了一个吊诡的好处 —— 不害怕用户瞎改 DataList.txt 了……
     * WARNING: 本函数高度特化
     * */
    public static void updateDataList(){
        String path = getSDCardPath() + "/bletest/DataList.txt";
        File file = new File(path);
        try {
            if (file.exists() == false) {
                // 如果不存在这个文件，就新建一个
                file.createNewFile();
            } else {
                // 如果存在，就清空
                FileWriter fileWriter =new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }

            // 得到目录下所有以"20"开头的字符，并将它们写入 DataList.txt
            // （显然我们都不指望这个 app 活到二十二世纪）
            Vector<String> allName = getFilesAllName(getSDCardPath() + "/bletest/");
            Log.e(TAG, "updateDataList: " + allName.get(0) );
            Vector<String> histData = new Vector<>();
            for (String item : allName){
          //      Log.e(TAG, "updateDataList: "+item );
            //    Log.e(TAG, "updateDataList: "+item.substring(item.length()-18, item.length()-0) );
              //  Log.e(TAG, "updateDataList: " );
                if (item.length() >= 44){
                    Log.e(TAG, "updateDataList: 已经添加了一项HistData" );
                    histData.add(item.substring(item.length()-18, item.length()-0));
                }

            }
            Log.e(TAG, "updateDataList: HISTDATA"+histData.get(0) );
            for (String string : histData){
                // 向文件写入换行符
                addStringToFile(path, "\n");
                // 向文件写入传入函数的实参
                addStringToFile(path, string);
            }


        } catch (Exception e){
            e.printStackTrace();
        }

    }

}

      /*  作者：Sky*ZST
        来源：CSDN
        原文：https://blog.csdn.net/u012246458/article/details/83063112
        版权声明：本文为博主原创文章，转载请附上博文链接！*/
