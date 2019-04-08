import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author sophia
 * @version 1.0
 * @date 2019-04-08
 * 使用zookeeper实现秒杀
 */
public class ZookeeperDemo {
    //定义一个共享资源
    private static int NUMBER = 10;

    //业务方法
    private static void getNumber() {
        System.out.println("*********开始********");
        System.out.println("NUMBER当前值 = " + NUMBER);
        NUMBER--;

        //线程休息2秒钟，该客户依然拿着这把锁
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("*********结束********");
    }


    public static void main(String[] args) {

        /**
         * 定义一个重试策略
         * 1000:每次等待的时间1秒
         * 10：重试的次数
         */
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 10);

        //创建一个ZK客户端,192.168.80.116:2181是zk的地址
        CuratorFramework cf = CuratorFrameworkFactory.builder().connectString("192.168.80.116:2181").retryPolicy(policy).build();

        //启动客户端
        cf.start();

        //在zk中定义一把锁
        final InterProcessMutex lock = new InterProcessMutex(cf, "/sophiaLock");

        //启动10个线程
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        //请求得到锁，如果没有得到锁，使用retryPolicy重试
                        lock.acquire();
                        //访问共享资源
                        getNumber();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //释放锁
                        try {
                            lock.release();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();

        }


    }
}
