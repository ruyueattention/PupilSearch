package com.ruyue;
import com.beust.jcommander.JCommander;
import com.ruyue.domain.InitialArgs;
import com.ruyue.domain.TreeNodeData;
import com.ruyue.domain.TrackURL;
import com.ruyue.module.ThreadTask;
import com.ruyue.util.OkHttp;
import com.ruyue.util.TreeNode;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.*;

public class Application {



    public static void main(String[] args) throws InterruptedException, MalformedURLException, ExecutionException {

        InitialArgs initialArgs = new InitialArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(initialArgs)
                .build();
        commander.parse(args);
        if (initialArgs.help) {
            commander.usage();
        }



        OkHttp okHttp = new OkHttp(initialArgs);



        if(initialArgs.targetFile!=null){

            File file = new File(initialArgs.targetFile);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                int line = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                    initialArgs.url = tempString;
                    // 显示行号
                    try {
                        initialArgs.init();
                    }catch(Exception e){
                        System.out.println("URL:"+tempString+"初始化错误,已跳过");
                        continue;
                    }
                    ThreadPoolExecutor pool = new ThreadPoolExecutor(initialArgs.threadNumber, initialArgs.threadNumber, 1000, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

                    System.out.println(initialArgs.url);
                    TrackURL targetUrl = new TrackURL(initialArgs.url);
                    //根节点
                    TreeNode<TreeNodeData> rootNode = new TreeNode<TreeNodeData>(new TreeNodeData(targetUrl));
                    //okHttp和getThreadPook及树根传进去
                    new ThreadTask(pool,okHttp).run(rootNode,initialArgs);
                    line++;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }

        }else {
            try {
                initialArgs.init();
            }catch(Exception e){
                System.out.println("输入参数错误,输入-h查看帮助");
                System.exit(0);
            }


            ThreadPoolExecutor pool = new ThreadPoolExecutor(initialArgs.threadNumber, initialArgs.threadNumber, 1000, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


            TrackURL targetUrl = new TrackURL(initialArgs.url);
            //根节点
            TreeNode<TreeNodeData> rootNode = new TreeNode<TreeNodeData>(new TreeNodeData(targetUrl));
            //okHttp和getThreadPook及树根传进去
            new ThreadTask(pool,okHttp).run(rootNode,initialArgs);

        }
        System.out.println("任务结束：到达设定深度或该URL已无法得到更多信息");
        System.exit(0);



    }
}
