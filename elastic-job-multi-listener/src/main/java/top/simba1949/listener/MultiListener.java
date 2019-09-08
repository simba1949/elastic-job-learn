package top.simba1949.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

/**
 * 前置后置任务监听实现类，需实现 ElasticJobListener 接口
 *
 * @Author SIMBA1949
 * @Date 2019/9/8 9:41
 */
public class MultiListener implements ElasticJobListener {
    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        System.err.println("君不见黄河之水天上来");
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        String jobParameter = shardingContexts.getJobParameter();
        System.err.println("奔流到海不复回");
    }
}
