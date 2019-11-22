package cn.ayl.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rock-Ayl 2019-5-10
 * 打印任务的demo
 */
@DisallowConcurrentExecution
public class PrintJob implements Job {

    protected static Logger logger = LoggerFactory.getLogger(PrintJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //todo 业务
        logger.info("执行定时器任务ing...");
    }

}
