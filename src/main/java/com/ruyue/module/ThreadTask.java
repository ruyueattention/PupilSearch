package com.ruyue.module;


import com.ruyue.domain.InitialArgs;
import com.ruyue.domain.TreeNodeData;
import com.ruyue.domain.TrackURL;
import com.ruyue.util.OkHttp;
import com.ruyue.util.TreeNode;

import java.net.MalformedURLException;
import java.util.concurrent.*;

public class ThreadTask {

    private ThreadPoolExecutor pool;
    public OkHttp okHttp;

    public ThreadTask(ThreadPoolExecutor pool, OkHttp okHttp) {
        this.pool = pool;
        this.okHttp = okHttp;
    }

    public void run(TreeNode<TreeNodeData> rootNode, InitialArgs initialArgs) throws InterruptedException,
            MalformedURLException,
            ExecutionException {
        //存放要追溯的URL队列
        LinkedBlockingQueue trackUrlLinkedBlockingQueue = new LinkedBlockingQueue<TrackURL>();
        trackUrlLinkedBlockingQueue.put(rootNode.nodeData.trackURL);
        while(true){
            //当没有活动线程并且完成的任务是大于1
            if(this.pool.getActiveCount()==0 && this.pool.getCompletedTaskCount()!=0){
                break;
            }
            //只要trackUrlLinkedBlockingQueue队列不为空就一直执行
            if(!trackUrlLinkedBlockingQueue.isEmpty()){
                //遍历树,如果access标志为false，就传入线程，
                //同时判断树深，如果到了指定深度，则停止，此树不再生长。

                for (TreeNode element : rootNode.elementsIndex){
                    //如果该节点未被访问,传入线程
                    if(!element.isAccess){
                        //到了指定深度，则停止，此树不再生长
                        //并且因为是广度遍历，也代表着前面的已经有遍历过了。
                        if(element.getLevel()>initialArgs.deep){
                            element.exit = true;
                            break;
                        }
                        element.isAccess = true;
                        this.pool.submit(new TrackTask(trackUrlLinkedBlockingQueue,okHttp,element,initialArgs));
                    }
                }

            }
        }
        //线程池拒接收新提交的任务，同时等待线程池里的任务执行完毕后关闭线程池。
        this.pool.shutdown();
        new ReportTask().writeExcel(rootNode,initialArgs.fileRoad);
    }
}





