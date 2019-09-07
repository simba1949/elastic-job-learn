package top.simba1949.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import org.springframework.beans.factory.annotation.Autowired;
import top.simba1949.common.User;
import top.simba1949.service.UserService;

import java.util.List;

/**
 * Dataflow类型用于处理数据流，需实现DataflowJob接口。
 * @Author SIMBA1949
 * @Date 2019/9/7 20:35
 */
public class MyDataflowJob implements DataflowJob<User> {

    @Autowired
    private UserService userService;
    /**
     *
     * @param shardingContext
     * @return
     */
    @Override
    public List<User> fetchData(ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        switch (shardingItem){
            case 0:
                // get data from database by sharding item 0
                List<User> data0 = userService.oneList();
                return data0;
            case 1:
                // get data from database by sharding item 0
                List<User> data1 = userService.twoList();
                return data1;
            case 2:
                // get data from database by sharding item 0
                List<User> data2 = userService.threeList();
                return data2;
            case 3:
                // get data from database by sharding item 0
                List<User> data3 = userService.fourList();
                return data3;
            default:
                ;
        }
        return null;
    }

    /**
     *
     * @param shardingContext
     * @param list
     */
    @Override
    public void processData(ShardingContext shardingContext, List<User> list) {
        // list 是每个分片对应获取的数据
        System.err.println("getShardingItem" + shardingContext.getShardingItem());
        list.forEach(item -> {
            System.out.print(item);
        });
        System.out.println("");
    }
}
