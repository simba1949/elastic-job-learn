package top.simba1949.service;

import org.springframework.stereotype.Service;
import top.simba1949.common.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:50
 */
@Service
public class UserService {

    private List<User> list = new ArrayList<>(10);
    private static final int UN_DELETE = 0;
    private static final int DELETE = 1;

    public List<User> oneList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("00-李白" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }

    public List<User> twoList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("01-杜甫" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }

    public List<User> threeList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("02-白居易" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }

    public List<User> fourList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("03-孟浩然" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }
}
