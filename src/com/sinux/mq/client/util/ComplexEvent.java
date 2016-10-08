package com.sinux.mq.client.util;

/**
 * @author jingwen.tong 2006-12-14 Copyright IBM 2005 带有计数的事件对象
 */
public class ComplexEvent {
	private long count_;
	private long countCopy_;

	public ComplexEvent() {
		count_ = 1;
		countCopy_ = 1;
	}

	public ComplexEvent(long count_) {
		this.count_ = count_;
		this.countCopy_ = count_;
	}

	public void SetEvent() throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		synchronized (this) {
			count_--;
			if (count_ == 0)
				notify();
		}
	}

	public int WaitForSingleObject(long msecs) throws InterruptedException {

		if (Thread.interrupted())
			throw new InterruptedException();
		synchronized (this) {
			if (count_ == 0) {
				count_ = countCopy_;
				return 0;
			} else {
				if (msecs <= 0) {
					wait();
					if (count_ == 0) {
						count_ = countCopy_;
						return 0;
					} else {
						return -1;
					}
				} else {
					long waitTime = msecs;
					long start = System.currentTimeMillis();
					try {
						while (true) {
							wait(waitTime);
							if (count_ == 0) {
								count_ = countCopy_;
								return 0;
							} else {
								waitTime = msecs - (System.currentTimeMillis() - start);
								if (waitTime <= 0)
									return -1;
							}
						}
					} catch (InterruptedException ex) {
						notify();
						throw ex;
					}
				}
			}
		}
	}
}
