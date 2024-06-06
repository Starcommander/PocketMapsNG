package com.starcom.navigation.gps;

import java.util.List;

import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class StaticPollMessage extends PollMessage
{
	List<TPVReport> tpvReportList;

	public StaticPollMessage(TPVReport report)
	{
		tpvReportList = List.of(report);
	}

	@Override public List<TPVReport> getTPVList()
	{
		return tpvReportList;
	}

}
