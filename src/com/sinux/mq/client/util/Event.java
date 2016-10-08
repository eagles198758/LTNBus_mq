/**
 * 
 */
package com.sinux.mq.client.util;

/**
 * @author jingwen.tong 2006-12-14 
 *  Copyright IBM 2005
 *  事件对象地实现
 */
public class Event {
   // private boolean state = false;
    private int EVNET_NOTIFY_FLAG = 0;
    private int count_ = 0;
    
    public void SetEvent()throws InterruptedException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
        synchronized (this) {
            count_++;
            notify();
        }
    }
    
    public int WaitForSingleObject(long msecs) throws InterruptedException {
        
        if (Thread.interrupted())
            throw new InterruptedException();
        synchronized (this) {
            if(count_>EVNET_NOTIFY_FLAG)
            {
                count_--;
                return 0;
            }
            else
            {
                if (msecs <= 0)
                {
                    wait();
                    if(count_>EVNET_NOTIFY_FLAG)
                    {
                        count_--;
                        return 0;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    long waitTime = msecs;
                    long start = System.currentTimeMillis();
                    try 
                    {
                        while(true) 
                        {
                            wait(waitTime);
                            if(count_>EVNET_NOTIFY_FLAG)
                            {
                                count_--;
                                return 0;
                            }
                            else 
                            {
                                waitTime = msecs
                                        - (System.currentTimeMillis() - start);
                                if (waitTime <= 0)
                                    return -1;
                            }
                        }
                    }
                    catch (InterruptedException ex) {
                        notify();
                        throw ex;
                    }
                }
            }
        }
       
    }
}
