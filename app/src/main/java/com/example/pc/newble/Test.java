package com.example.pc.newble;

import android.util.Log;

public class Test {
    private static final String HEX = "0123456789abcdef";
    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt(b & 0x0f));
        }
        return sb.toString();

    }
    public static void main(String[] args){
        String datautf8;
        float  dataview1 = 0;//用于画图
        float  dataview2;//用于画图
        float  dataview3;//用于画图
        datautf8 = "channel1:125.2 mV";
        datautf8 = datautf8.substring(9);

        datautf8 = datautf8.replaceAll("[a-zA-Z]","" );  //^[0-9]+ [+-*\] [0-9]
        String[] iv = datautf8.split(",");
        int l = iv.length;
        if (l == 3){

        dataview1 = Float.parseFloat(iv[0]);
        dataview2 = Float.parseFloat(iv[1]);
        dataview3 = Float.parseFloat(iv[2]);}
        dataview1 = Float.parseFloat(iv[0]);
        System.out.println(dataview1);
        System.out.println(dataview1);

    }
}
