package cn.ayl.common.job;

import cn.ayl.common.job.execute.PrintJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rock-Ayl 2019-4-30
 * 这个是注册并实现quartz框架的定时管理任务的class，有两种方式设置定时任务
 * 1:-简单的定时器实现:使用简单的时间表生成器.设定任务.每多少小时执行一次(1).次数不限;
 * 2:corn表达式实现
 */
public class Scheduler {

    protected static Logger logger = LoggerFactory.getLogger(Scheduler.class);

    //静态调度程序,由FileServer默认启动
    public static org.quartz.Scheduler instance;
    //存储 工作任务&任务条件 对象的list
    private static List<ScheduleTask> tasks = new ArrayList();

    //普通定时实现:每一小时执行一次
    private static final SimpleScheduleBuilder HourScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(1).repeatForever();
    //普通定时实现:每一分钟执行一次
    private static final SimpleScheduleBuilder MinutesScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever();
    //cron定时实现:每天0点执行一次
    private static final CronScheduleBuilder CronZeroScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 0 * * ?");
    //cron定时实现:每天9点执行一次
    private static final CronScheduleBuilder CronNineScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 9 * * ?");

    /**
     * Task类,存放job和trigger
     */
    private static class ScheduleTask {

        public JobDetail job;
        public Trigger trigger;

        public ScheduleTask(JobDetail job, Trigger trigger) {
            this.job = job;
            this.trigger = trigger;
        }

    }

    /**
     * 通用注册方法
     *
     * @param jobName  工作名称
     * @param jobGroup 工作组
     * @param object   实现Job接口的实现类
     * @param corn     定是实现方式
     */
    private static void register(String jobName, String jobGroup, Class object, ScheduleBuilder corn) {
        JobDetail job = JobBuilder.newJob(object).withIdentity(jobName, jobGroup).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).startNow().withSchedule(corn).build();
        tasks.add(new ScheduleTask(job, trigger));
    }

    //启动Scheduler
    public static void startup() {
        try {
            //创建定时器
            instance = StdSchedulerFactory.getDefaultScheduler();
            //打印任务的demo
            register("PrintJob", "file", PrintJob.class, CronZeroScheduleBuilder);
            //启动
            for (ScheduleTask task : tasks) {
                instance.scheduleJob(task.job, task.trigger);
            }
            instance.start();
            logger.info(">>>file scheduler start.");
        } catch (SchedulerException se) {
            logger.error("工作调度程序启动错误:" + se.getMessage(), se);
        }
    }

    /**
     * 关闭Scheduler
     */
    public static void stop() {
        try {
            instance.shutdown();
        } catch (Exception e) {
            logger.error("工作调度程序关闭错误[{}]", e);
        }
    }

}