package niohttp;

import java.util.ArrayList;
import java.util.List;
@MyRestController
public class UserController {
    private static List<User> userList = new ArrayList<>();

    static {
        userList.add(new User(1, "Jim"));
        userList.add(new User(2, "Lily"));
    }

    @MyRequestMapping("/get")
    public String get(int id) {
        for (User user:userList) {
            if (user.getId()==id){
                return user.toString();
            }
        }
        return "无该用户";
    }

    @MyRequestMapping("/getAll")
    public String getAll() {
        StringBuilder str = new StringBuilder();
        for (User user:userList) {
             str.append(user.toString());
        }
        return str.toString();
    }

    @MyRequestMapping("/getOne")
    public String getOne(int id , String name){
        return new User(id,name).toString();
    }
}
