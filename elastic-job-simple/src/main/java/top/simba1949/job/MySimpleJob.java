package top.simba1949.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

/**
 * 执行分片操作定时任务调度
 *
 * @Author SIMBA1949
 * @Date 2019/9/7 19:39
 */
public class MySimpleJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        //
        String jobName = shardingContext.getJobName();
        //
        String jobParameter = shardingContext.getJobParameter();
        // 获取分片项
        int shardingItem = shardingContext.getShardingItem();
        // 获取分片项参数
        String shardingParameter = shardingContext.getShardingParameter();
        // 获取分片总数
        int shardingTotalCount = shardingContext.getShardingTotalCount();
        //
        String taskId = shardingContext.getTaskId();

        switch (shardingItem){
            case 0:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
            case 1:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
            case 2:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
            case 3:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
        }

    }
}
