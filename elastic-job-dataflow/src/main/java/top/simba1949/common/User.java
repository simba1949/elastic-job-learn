package top.simba1949.common;

import lombok.Data;

import java.util.Date;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:35
 */
@Data
public class User {
    private Long id;
    private String username;
    private Date birthday;
    private Integer status;
}
