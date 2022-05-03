package com.example.pc.lbs.utils;


import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

/**
 * Created by pc on 4/19/2019.
 */

public class FileUtils {

    private static final String TAG = "FileUtility";

    public static final String baseDirPath = getSDCardPath() + "/bletest/"; //app的基础输出目录

    /**
     * 将字符串写入到文本文件中
     */
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

    // 生成文件
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
        File file;
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
        StringBuilder content = new StringBuilder();
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    InputStreamReader inputreader
                            = new InputStreamReader(instream, StandardCharsets.UTF_8);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    instream.close();//关闭输入流
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content.toString();
    }


    public static String getSDCardPath() {
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        assert sdcardDir != null;
        return sdcardDir.toString();
    }

    /**
     * 使用 FileWriter 方法向 filename 追加 content 内容的函数。
     * 与本类中 writeTextToFile 的区别在于，本函数是以追加的形式在文件后写入内容。
     */
    public static void appendStringToFile(String fileName, String content) {
        try {
            if (!checkIfFileExist(fileName)) return;

            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkIfFileExist(String fileName) {
        // 检查是否存在这样的文件，不存在则报错
        File f = new File(fileName);
        if (!f.exists()) {
            Log.e(TAG, "File does not exist");
            return false;
        }
        return true;
    }


    /**
     * 得到某一目录下所有文件名，并返回包含这些文件名的 Vector。
     */
    public static Vector<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        Vector<String> s = new Vector<>();
        for (File value : files) {
            s.add(value.getAbsolutePath());
        }
        return s;
    }


    /**
     * 向 csv 文件写入的函数。
     */
    public static void addLineToCsvFile(String path, List<String> inputs) {
        appendStringToFile(path, constructCsvString(inputs));
    }

    /*
     * 将数据附加到csv文件指定行的后面,返回值表示是否添加成功
     * @author Hadeslock
     * @time 2022/4/14 16:15
     */
    public static boolean appendDataToSpecifiedLineOfCsv(String filePath, List<String> data,
                                                         int targetLineIndex) {
        //构造字符串
        String csvString = constructCsvString(data);
        if (csvString == null) {
            return false;
        }

        // ---------- 添加到文件 --------------
        //确认文件名是否存在
        if (!checkIfFileExist(filePath)) return false;
        String curLineString; //读入的一行数据
        int curLineIndex = 0; //当前读到的行
        File srcFile = new File(filePath); //源文件
        //生成临时的输出文件名
        int split = filePath.lastIndexOf('.');
        String outputPath = filePath.substring(0, split) + "(1)" + filePath.substring(split);
        // 源文件输入输出流
        BufferedReader in = null; // 源文件输入流
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        try {
            //生成输入输出流
            fileReader = new FileReader(filePath);
            in = new BufferedReader(fileReader);
            fileWriter = new FileWriter(outputPath);
            while ((curLineString = in.readLine()) != null) {
                if (++curLineIndex == targetLineIndex) {
                    //读到指定的行数,拼接字符串
                    curLineString = curLineString + "," + csvString.substring(0, csvString.length() - 1);
                }
                fileWriter.write(curLineString + '\n');
            }
            srcFile.delete(); //删除源文件
            new File(outputPath).renameTo(srcFile); //新文件重命名为原文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关流
                if (in != null) {
                    in.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /*
     * 根据String集合构建单行csv文件字符串
     * @author Hadeslock
     * @time 2022/4/14 16:29
     */
    private static String constructCsvString(List<String> data) {
        String res;
        int n;
        if ((n = data.size()) == 0) {
            //无数据
            return null;
        }
        //构建添加的字符串
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n - 1; i++) {
            builder.append(data.get(i)).append(',');
        }
        builder.append(data.get(n - 1)).append('\n');
        return builder.toString();
    }


}

