package com.example.pc.lbs.utils;


import android.os.Environment;
import android.util.Log;

import java.io.*;
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
    public static void addLineToCsvFile(String path, String filename, List<String> inputs) {
        makeFilePath(path, filename);
        appendStringToFile(path + filename, constructCsvString(inputs));
    }

    /*
     * 将数据添加到csv文件指定行
     * @author hadeslock
     * @date 2022/5/3 14:53
     * @param filePath 文件所在的目录
     * @param fileName 文件名
     * @param data 要添加的数据集
     * @param targetLineIndex 要添加到的行数
     * @param mode 0-插入 1-附加到末尾
     * @return boolean true-成功 false-失败
     */
    public static boolean addDataToSpecifiedLineOfCsv(String filePath, String fileName, List<String> data,
                                                      int targetLineIndex, int mode) {
        //构造字符串
        String dataString = constructCsvString(data);
        if (dataString == null) {
            return false;
        }

        // ---------- 添加到文件 --------------
        //先生成文件
        makeFilePath(filePath, fileName);

        String curLineString; //读入的一行数据
        int curLineIndex = 0; //当前读到的行

        String fullFilePath = filePath + fileName;
        File srcFile = new File(fullFilePath); //源文件
        //生成临时的输出文件名
        int split = fullFilePath.lastIndexOf('.');
        String outputPath = fullFilePath.substring(0, split) + "(1)" + fullFilePath.substring(split);

        // 源文件输入输出流
        BufferedReader in = null; // 源文件输入流
        FileReader fileReader = null;
        FileWriter fileWriter = null;

        try {
            //生成输入输出流
            fileReader = new FileReader(fullFilePath);
            in = new BufferedReader(fileReader);
            fileWriter = new FileWriter(outputPath);
            while ((curLineString = in.readLine()) != null) {
                if (++curLineIndex == targetLineIndex) {
                    //读到指定的行数
                    if (mode == 0) {
                        //模式为插入，直接写入字符串
                        fileWriter.write(dataString);
                    } else if (mode == 1) {
                        //模式为附加，拼接字符串
                        curLineString = curLineString + "," + dataString.substring(0, dataString.length() - 1);
                    }
                }
                fileWriter.write(curLineString + '\n');
            }
            srcFile.delete(); //删除源文件
            new File(outputPath).renameTo(srcFile); //新文件重命名为原文件
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

