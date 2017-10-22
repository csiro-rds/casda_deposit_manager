<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%--
 depositStatus.jsp: Displays the list of active, failed and recently completed deposit jobs

  Copyright 2015, CSIRO Australia
  All rights reserved.

 --%>
 <html>
<head>
<title>CSIRO Data Deposit Manager - CASDA Observation Refresh Status</title>
<link rel="stylesheet" type="text/css" href="/casda_deposit_manager/css/deposit.css" />
</head>
<body>
    <div class="meta" style="float: left;">
        <a href="/casda_deposit_manager">Home</a>&nbsp;&gt;&nbsp;
        Observation Deposit Status
    </div>
	<div class="meta" style="float: right;">
		<a  href="logout">logout</a>
	</div>
	<jsp:include page="adminHeader.jsp" />
	
	<fieldset>
		<legend>CASDA Observation Refresh Status</legend>		
		<div style="float: left;">

			<c:if test="${not empty flash}">
            <c:if test="${not empty flash['error']}">
                <c:set var="flashBackgroundColour" value="#FBC8C8"/>
                <c:set var="flashMessage" value="${flash['error']}"/>
            </c:if>
            <c:if test="${not empty flash['success']}">
                <c:set var="flashBackgroundColour" value="#CBF8CB"/>
                <c:set var="flashMessage" value="${flash['success']}"/>
            </c:if>
            <div style="margin: 50px; padding: 10px; border: 1px solid black; background-color: ${flashBackgroundColour};">
                ${flashMessage}
            </div>
            </c:if>

			<form method="post">
            	<input type="submit" name="refreshAll" value="Refresh Metadata"></input>
            	for all deposited observations
            	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
           </form>



			<h2>Observations Being Refreshed</h2>

			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
				</colgroup>

				<tr class="tableHeader">
					<td>Scheduling Block Id</td>
					<td>State</td>
					<td>Last Updated</td>
				</tr>
			
				<c:forEach var="obs" items="${observations}"
					varStatus="rowCounter">
					<c:choose>
						<c:when test="${rowCounter.count % 2 == 0}">
							<c:set var="rowStyle" scope="page" value="even" />
						</c:when>
						<c:otherwise>
							<c:set var="rowStyle" scope="page" value="odd" />
						</c:otherwise>
					</c:choose>
					
					<tr class="${rowStyle}" style="valign='top'">
						<td>${obs.sbid}</td>
						<td>${obs.refreshState}</td>
						<td>${obs.refreshStateChanged}</td>
					</tr>
				</c:forEach>
			</table>
			<UL>
				<li> Other queued observations: ${numQueuedTasks } 
				<li> Completed observations: ${numCompletedObservations } 
			</UL>


			<h2>Failed Observation Refreshes (last ${failedRefreshesMaximumAge})</h2>

			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
				</colgroup>

				<tr class="tableHeader">
					<td>Scheduling Block Id</td>
					<td>Last Updated</td>
				</tr>
			
				<c:forEach var="obs" items="${failedObservations}"
					varStatus="rowCounter">
					<c:choose>
						<c:when test="${rowCounter.count % 2 == 0}">
							<c:set var="rowStyle" scope="page" value="even" />
						</c:when>
						<c:otherwise>
							<c:set var="rowStyle" scope="page" value="odd" />
						</c:otherwise>
					</c:choose>
					
					<tr class="${rowStyle}" style="valign='top'">
						<td>${obs.sbid}</td>
						<td>${obs.refreshStateChanged}</td>
					</tr>
				</c:forEach>
			</table>


			<h2>Active Refresh Jobs</h2>
			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
                    <col />
                    <col />
                    <col />
				</colgroup>

				<tr class="tableHeader">
					<td>Refresh Started Date</td>
					<td>Num Observations</td>
				</tr>

				<c:forEach var="refreshJob" items="${activeRefreshJobs}"
					varStatus="rowCounter">
					<c:choose>
						<c:when test="${rowCounter.count % 2 == 0}">
							<c:set var="rowStyle" scope="page" value="even" />
						</c:when>
						<c:otherwise>
							<c:set var="rowStyle" scope="page" value="odd" />
						</c:otherwise>
					</c:choose>

					<tr class="${rowStyle}" style="valign='top'">
						<td>${refreshJob.jobStartTime}</td>
						<td align="right">${refreshJob.observationRefreshTasks.size()}</td>
					</tr>
				</c:forEach>
			</table>

			<h2>Recently Refreshed (last ${completedRefreshMaximumAge})</h2>
			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
				</colgroup>

				<tr class="tableHeader">
					<td>Refresh Started Date</td>
					<td>Num Observations</td>
					<td>Refresh Completed Date</td>
				</tr>

				<c:forEach var="refreshJob" items="${completedRefreshJobs}"
					varStatus="rowCounter">
					<c:choose>
						<c:when test="${rowCounter.count % 2 == 0}">
							<c:set var="rowStyle" scope="page" value="even" />
						</c:when>
						<c:otherwise>
							<c:set var="rowStyle" scope="page" value="odd" />
						</c:otherwise>
					</c:choose>

					<tr class="${rowStyle}">
						<td>${refreshJob.jobStartTime}</td>
						<td align="right">${refreshJob.observationRefreshTasks.size()}</td>
						<td>${refreshJob.jobCompleteTime}</td>
					</tr>
				</c:forEach>
			</table>
		</div>


	</fieldset>
<jsp:include page="adminFooter.jsp" />
</body>
</html>
