/*
 * Copyright 2007 ETH Zurich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.llrp.ltk.net;

import java.util.concurrent.BlockingQueue;

import org.apache.mina.common.IoHandlerAdapter;
import org.llrp.ltk.generated.parameters.ConnectionAttemptEvent;
import org.llrp.ltk.types.LLRPMessage;


/**
 * defines methods that need to be any LLRPIoHandlerAdapter implementation in addition to
 * the methods defined by the MINA IoHandler interface. 
 *
 */

public abstract class LLRPIoHandlerAdapter extends IoHandlerAdapter{

	/**
	 * returns queue of all incoming messages where the messages type is equal to the one specified 
	 * in the IoSession parameter LLRPConnection.SYNC_MESSAGE_ANSWER. This method is required by
	 * the transact (synchronous message sending) of the LLRP connections. 
	 **/

	public abstract BlockingQueue<LLRPMessage> getSynMessageQueue();

	/** 
	 * returns queue with all incoming ConnectionAttemptEvent parameters which were embedded in 
	 * READER_NOTIFICATION messages. 
	 * The getConnectionAttemptEventQueue method is used
	 * to fetch any ConnectionAttemptEvent that arrived in READER_NOTIFICATION messages. 
	 * These events are used by LLRPConnection objects
	 * to check whether the connection could be established successfully. 
	 **/ 
	
	public abstract BlockingQueue<ConnectionAttemptEvent> getConnectionAttemptEventQueue();


}
