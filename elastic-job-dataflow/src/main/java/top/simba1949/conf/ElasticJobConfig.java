package top.simba1949.conf;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:34
 */
@Component
@ImportResource({"classpath:application-elastic-job.xml"})
public class ElasticJobConfig {
}
