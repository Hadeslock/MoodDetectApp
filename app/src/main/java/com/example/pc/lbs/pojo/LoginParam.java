package com.example.pc.lbs.pojo;

/*
 * Author: Hadeslock
 * Created on 2022/4/12 11:21
 * Email: hadeslock@126.com
 * Desc: 登录时的参数类
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginParam {
    private String username;
    private String password;
    private String captcha;
}
