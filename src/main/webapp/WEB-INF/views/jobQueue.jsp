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
<title>CSIRO Data Deposit Manager - CASDA Deposit Job Queue</title>
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
		<legend>CASDA Deposit Job Queue</legend>		
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

			<!--  <p>
			<b>Running ${numJobs} out of ${maxJobs}.</b>
			 -->
			<canvas id="gauge"></canvas><br/>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${numJobs} jobs running
            <c:if test="${paused}">
            	&nbsp;&nbsp;<b>Warning: Queues are paused</b>
            </c:if>
			<form method="post">
				<c:choose>
					<c:when test="${paused}">
						<input type="submit" name="unpause" value="Unpause Queues"></input>
					</c:when>
					<c:otherwise>
						<input type="submit" name="pause" value="Pause Queues"></input>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			</form>
			
			<c:forEach var="jobType" items="${jobTypeList}">
				<c:set var="summary" scope="page" value="${queuedJobMap.get(jobType)}" />
				<c:set var="jobs" scope="page" value="${summary.displayJobs}" />
				<c:set var="allowedJobs" scope="page" value="${summary.maxAllowedJobs}" />
			
				<h2>${jobType} [Limit: ${allowedJobs>=0 ? allowedJobs : "None"}]</h2>

				<table class="obsTable">
	
					<colgroup>
						<col />
						<col />
						<col />
						<col />
					</colgroup>
	
					<tr class="tableHeader">
						<td>Job Id</td>
						<td>State</td>
						<td>Last Updated</td>
						<td>Duration</td>
					</tr>
				
					<c:forEach var="job" items="${jobs}"
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
							<td>${job.jobId}</td>
							<c:choose>
								<c:when test="${job.status.isFailed()}">
			                        <td><a href="${url}/${jobType}/joboutput?jobId=${job.jobId}">${job.status}</a></td>
		                        </c:when>
		                        <c:otherwise>
							<td>${job.status}</td>
		                        </c:otherwise>
	                        </c:choose>
							<td>${job.lastUpdated}</td>
							<td>${job.duration}</td>
						</tr>
					</c:forEach>
				</table>
				<UL>
					<li> Other queued jobs: ${summary.numQueuedJobs } 
					<li> Completed jobs: ${summary.numCompletedJobs } 
				</UL>
			
			</c:forEach>
		</div>


	</fieldset>

<script src="gauge.min.js"></script>
<script>
	var opts = {
	  angle: 0.15, 
	  lineWidth: 0.44, 
	  radiusScale: 1, 
	  pointer: {
	    length: 0.6, 
	    strokeWidth: 0.035, 
	    color: '#000000' 
	  },
	  limitMax: true,     // If false, the max value of the gauge will be updated if value surpass max
	  limitMin: true,     // If true, the min value of the gauge will be fixed unless you set it manually
	  colorStart: '#6FADCF',  
	  colorStop: '#8FC0DA',   
	  strokeColor: '#E0E0E0', 
	  generateGradient: true,
	  highDpiSupport: true,   
	  staticLabels: {
	    font: "12px sans-serif", 
	    labels: [0, 2, 4, 6, 8, 10, 12, 14, 18],  // Print labels at these values
	  fractionDigits: 0  
	},
	  staticZones: [
	              {strokeStyle: "#30B32D", min: 0, max: ${maxJobs}}, // Green
	              {strokeStyle: "#FFDD00", min: ${maxJobs}, max: ${maxJobs}+2} // Yellow
	 ],
	};
	var target = document.getElementById('gauge'); 
	var gauge = new Gauge(target).setOptions(opts); 
	gauge.maxValue = ${maxJobs}+2; // Allow for some jobs in non-limited queues
	gauge.setMinValue(0);  
	gauge.animationSpeed = 8; // set animation speed (32 is default value)
	gauge.set(${numJobs}); // set actual value
</script>

<jsp:include page="adminFooter.jsp" />
</body>

</html>
