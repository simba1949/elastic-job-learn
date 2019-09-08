package top.simba1949.config;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

/**
 * @Author SIMBA1949
 * @Date 2019/9/8 8:15
 */
@Component
@ImportResource({"classpath:application-elastic-job.xml"})
public class ElasticJobConfig {
}
