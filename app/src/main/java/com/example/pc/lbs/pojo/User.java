package com.example.pc.lbs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Hadeslock
 * Created on 2022/4/12 1:38
 * Email: hadeslock@126.com
 * Desc:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private String email;
    private String phone;
}
