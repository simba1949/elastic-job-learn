package top.simba1949.config;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 19:56
 */
@Component
@ImportResource({"classpath:application-elastic-job.xml"})
public class JobConfig {
}
