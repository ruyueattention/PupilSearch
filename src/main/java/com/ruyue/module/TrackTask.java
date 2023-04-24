package com.ruyue.module;

import com.ruyue.domain.InitialArgs;
import com.ruyue.domain.TrackURL;
import com.ruyue.domain.TreeNodeData;
import com.ruyue.util.OkHttp;
import com.ruyue.util.Rendering;
import com.ruyue.util.TreeNode;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackTask implements Callable {
    public TrackURL trackURL;


    public LinkedBlockingQueue trackUrlLinkedBlockingQueue;

    public OkHttp okHttp;
    public TreeNode<TreeNodeData> parentNode;
    public InitialArgs initialArgs;
    public TrackTask(LinkedBlockingQueue trackUrlLinkedBlockingQueue, OkHttp okHttp,
                     TreeNode<TreeNodeData> parentNode, InitialArgs initialArgs) {
        this.trackUrlLinkedBlockingQueue = trackUrlLinkedBlockingQueue;
        this.okHttp = okHttp;
        this.parentNode = parentNode;
        this.initialArgs = initialArgs;
    }




    @Override
    public TreeNode<TreeNodeData> call() throws IOException, InterruptedException {
        if(!parentNode.exit){
            //出队和入队
            trackURL = (TrackURL) trackUrlLinkedBlockingQueue.take();
            String htmlPageResponse = null;
            //第一次用渲染去做,deep =0 and size=1
            if(trackUrlLinkedBlockingQueue.size()==0 && parentNode.isRoot()){
                try {
                     htmlPageResponse = new Rendering().getHtmlPageResponse(trackURL.getUrl().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Response response = okHttp.doGet(trackURL.getUrl().toString());
                //api类型的需要check post
                if(response.code()!=200 && trackURL.postMethod){
                    response = okHttp.doPost(trackURL.getUrl().toString());
                }
                htmlPageResponse = response.body().string();


            }
            new SearchTask(trackURL,htmlPageResponse,trackUrlLinkedBlockingQueue,parentNode,initialArgs).runStart();

        }

        return parentNode;
    }
}
