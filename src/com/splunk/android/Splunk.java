/*
 * Copyright 2012 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk.android;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.splunk.sdk.ResultsReaderJson;
import com.splunk.sdk.Job;
import com.splunk.sdk.Args;
import com.splunk.sdk.Receiver;
import com.splunk.sdk.ResultsReader;
import com.splunk.sdk.Service;

public class Splunk {

	//only json output is supported for Android
	private static final String JSON_OUTPUT_MODE = "json";
	
	//throttle this value down for Android
	private static final int MAXEVENTS = 10000;
	
	private static String USERNAME = "admin";
	private static String PASSWORD = "foo";
	private static String HOST = "myhost.com";
	private static int PORT = 8089;
	
	public static void setUsername(String username) {
		USERNAME = username;
	}

	public static void setPassword(String password) {
		PASSWORD = password;
	}

	public static void setHost(String host) {
		HOST = host;
	}

	public static void setPort(int port) {
		PORT = port;
	}

	private static Splunk instance = null;

	private Service service;

	private Splunk() {
		service = connect();

	}

	public synchronized static Splunk getInstance() {

		if (instance == null)
			instance = new Splunk();

		return instance;
	}

	/**
	 * These connection parameters should ideally come from a config file
	 * @return
	 */
	private Service connect() {

		Map<String, Object> connectionArgs = new HashMap<String, Object>();
		connectionArgs.put("host", HOST);
		connectionArgs.put("username", USERNAME);
		connectionArgs.put("password", PASSWORD);
		connectionArgs.put("port", PORT);
		
		// will login and save the session key which gets put in the HTTP
		// Authorization header
		return Service.connect(connectionArgs);

	}

	/**
	 * Log an event to Splunk
	 * 
	 * @param event
	 * @param sourcetype
	 * @param source
	 * @param index
	 * @param host
	 */
	public void logEvent(SplunkLogEvent event, String sourcetype,
			String source, String index) {

		Receiver receiver = service.getReceiver();

		// Set the metadata
		Args logArgs = new Args();
		logArgs.put("sourcetype", sourcetype);
		logArgs.put("source", source);
		logArgs.put("index", index);

		// Log an event
		receiver.log(logArgs, event.toString());
	}

	/**
	 * Default 5 minute window
	 * 
	 * @param search
	 * @param listener
	 */
	public void realtimeSearch(String search, SplunkResultsListener listener) {
		realtimeSearch(search, listener, "-5m");
	}

	/**
	 * Specify your window start
	 * 
	 * @param search
	 * @param listener
	 * @param earliest
	 */
	public void realtimeSearch(String search, SplunkResultsListener listener,
			String earliest) {
		// search params such as realtime search time modifiers
		Args queryArgs = new Args();
		queryArgs.put("auto_cancel", 3600);
		queryArgs.put("earliest_time", "rt" + earliest);
		queryArgs.put("latest_time", "rt");

		// submit the job
		Job job = service.getJobs().create("search " + search, queryArgs);

		// result params
		Args outputArgs = new Args();
		outputArgs.put("output_mode", JSON_OUTPUT_MODE);

		// variables used in the realtime polling logic
		int resultsOffset = 0;
		InputStream stream = null;

		boolean waitForResults = true;

		try {
			while (waitForResults) {

				job.refresh();
				int resultsCount = job.getResultPreviewCount();
				// wait for something to show up
				if (resultsCount <= resultsOffset) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					continue;
				}
				// ok, we have some realtime results ready, request "count"
				// number of results from the "offset" mark
				outputArgs.put("count", resultsCount);
				outputArgs.put("offset", resultsOffset);
				stream = job.getResultsPreview(outputArgs);

				
				try {

					ResultsReader resultsReader = new ResultsReaderJson(stream);
					listener.processResults(resultsReader);

				} catch (Exception e) {
				}

				// increment our results cursor
				resultsOffset = resultsCount;

			}
		} catch (Exception e) {
		}

	}

	public void search(String search, String earliest, String latest,
			SplunkResultsListener listener) {

		try {

			// submit the job
			Job job = initSearchJob(search, earliest, latest);

			while (!job.isDone()) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}

			processJobResults(job, listener);

		} catch (Exception e) {
		}
	}

	public void processJobResults(Job job, SplunkResultsListener listener) {

		try {
			Args outputArgs = new Args();
			outputArgs.put("output_mode", JSON_OUTPUT_MODE);

			// After the search job is complete, get the number of events
			int eventCount = job.getEventCount();

			// Page through results with a simple loop
			int getOffset = 0;
			while (getOffset < eventCount) {

				outputArgs.put("count", MAXEVENTS);
				outputArgs.put("offset", getOffset);

				InputStream stream = job.getResults(outputArgs);

				ResultsReader resultsReader = new ResultsReaderJson(stream);
				listener.processResults(resultsReader);

				// Increase the offset to get the next set of events
				getOffset = getOffset + MAXEVENTS;
			}
		} catch (Exception e) {
		}

	}

	public Job initSearchJob(String search, String earliest, String latest) {

		try {

			Args queryArgs = new Args();
			queryArgs.put("earliest_time", earliest);
			queryArgs.put("latest_time", latest);

			// submit the job
			Job job = service.getJobs().create("search " + search, queryArgs);

			return job;

		} catch (Exception e) {
		}
		return null;
	}

}
