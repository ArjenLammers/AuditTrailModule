package audittrail.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import audittrail.proxies.Log;
import audittrail.proxies.LogLine;
import audittrail.proxies.ReferenceLog;
import audittrail.proxies.ReferenceLogLine;
import audittrail.proxies.constants.Constants;

public class LogrecordWriter {
	private static IContext context; 
	private static BlockingQueue<Log> buffer = new LinkedBlockingQueue<>(Constants.getCommitBufferSize().intValue()); 
	private static ILogNode LOGGER = Core.getLogger("AuditTrail");
	
	private static LogrecordWriter instance = new LogrecordWriter();
	private LogrecordWriterThread thread;
	
	LogrecordWriter() {
		thread = new LogrecordWriterThread();
		thread.start();
	}
	
	public static IContext getContext() {
		if (context == null) {
			context = Core.createSystemContext();
		}
		return context;
	}
	
	public static void addLog(Log log) {
		if (buffer.remainingCapacity() == 0) {
			LOGGER.warn("Buffer is full, expect delays.");
		}
		while (true) {
			try {
				if (!buffer.offer(log, 1, TimeUnit.MINUTES)) {
					LOGGER.error("Error while offering log to buffer. Retrying...");
				} else {
					break;
				}
			} catch (InterruptedException e) {}
		}
	}
	
	private class LogrecordWriterThread extends Thread {
		public void run() {
			LOGGER.info("Started background commiter thread.");
			while (true) {
				try {
					List<Log> logBuffer = new LinkedList<>();
					List<IMendixObject> commitBuffer = new LinkedList<>();
					for (int i = 0; i < 1000; i++) {
						Log l = buffer.poll();
						if (l == null) break;
						logBuffer.add(l);
					}
					
					if (logBuffer.size() > 0) {
						LOGGER.debug("Flushing " + logBuffer.size() + " log objects.");
						for (Log l : logBuffer) {
							commitBuffer.add(l.getMendixObject());
							List<IMendixObject> logLines = Core.retrieveByPath(context, l.getMendixObject(), LogLine.MemberNames.LogLine_Log.toString());
							commitBuffer.addAll(logLines);
							for (IMendixObject ll : logLines) {
								List<IMendixObject> referenceLogs = Core.retrieveByPath(context, ll, ReferenceLog.MemberNames.ReferenceLog_LogLine.toString());
								for (IMendixObject r : referenceLogs) {
									List<IMendixObject> referenceLogLines = Core.retrieveByPath(context, r, ReferenceLogLine.MemberNames.ReferenceLogLine_ReferenceLog.toString());
									commitBuffer.addAll(referenceLogLines);
								}
								commitBuffer.addAll(referenceLogs);
							}
						}
						
						context.startTransaction();
						Core.commit(context, commitBuffer);
						try { while (context.isInTransaction()) context.endTransaction(); } catch (Exception e) {} 
						LOGGER.trace("Flushed " + commitBuffer.size() + " objects.");
					} 
					
					
					Thread.yield();
					try { sleep(100);} catch (Exception ex2) {}
				} catch (Exception e) {
					LOGGER.error("Error occurred while committing Log records in background: " + e.toString(), e);
					try { sleep(5000);} catch (Exception ex2) {} 
				}
			}
			
			
			
		}
	}
	
}
