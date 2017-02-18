/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.throttler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The {@link AbstractChannelThrottler} is abstract class implementing a
 * throttler with one global execution rate, or rate limiter
 * 
 * @author Karel Goderis - Initial contribution
 */
abstract class AbstractChannelThrottler implements ChannelThrottler {

	protected final Rate totalRate;
	protected final TimeProvider timeProvider;
	protected final ScheduledExecutorService scheduler;
	protected final Map<Object, Rate> channels = new HashMap<Object, Rate>();

	protected AbstractChannelThrottler(Rate totalRate,
			ScheduledExecutorService scheduler, Map<Object, Rate> channels,
			TimeProvider timeProvider) {
		this.totalRate = totalRate;
		this.scheduler = scheduler;
		this.channels.putAll(channels);
		this.timeProvider = timeProvider;
	}

	protected synchronized long callTime(Rate channel) {
		long now = timeProvider.getCurrentTimeInMillis();
		long callTime = totalRate.callTime(now);
		if (channel != null) {
			callTime = Math.max(callTime, channel.callTime(now));
			channel.addCall(callTime);
		}
		totalRate.addCall(callTime);
		return callTime;
	}

	protected long getThrottleDelay(Object channelKey) {
		long delay = callTime(channels.get(channelKey))
				- timeProvider.getCurrentTimeInMillis();
		return delay < 0 ? 0 : delay;
	}
}