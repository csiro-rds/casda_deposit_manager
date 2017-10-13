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
<title>CSIRO Data Deposit Manager - CASDA Observation Deposit Status</title>
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
		<legend>CASDA Observation Deposit Status</legend>		
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
				Find observation: 
            	<input type="text" name="sbid" value=""></input>
            	<input type="submit" name="search" value="Search"></input>
            	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
           </form>

			<form method="post">
            	<input type="submit" name="manualPoll" value="Manual Poll"></input>
            	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
           </form>

			<h2>Currently Depositing</h2>
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
					<td>Scheduling Block Id</td>
					<td>Obs Start Time</td>
					<td>Deposit Started Date</td>
					<td>Num Images</td>
					<td>State</td>
					<td>Last Updated</td>
					<td>Failures</td>
				</tr>

				<c:forEach var="obs" items="${activeParentDepositables}"
					varStatus="rowCounter">
					<c:choose>
						<c:when test="${rowCounter.count % 2 == 0}">
							<c:set var="rowStyle" scope="page" value="even" />
						</c:when>
						<c:otherwise>
							<c:set var="rowStyle" scope="page" value="odd" />
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${parentDepositableWithFailures.containsKey(obs.sbid)}">
							<c:set var="failures" scope="page" value="${parentDepositableWithFailures.get(obs.sbid)}" />
						</c:when>
						<c:otherwise>
							<c:set var="failures" scope="page" value="" />
						</c:otherwise>
					</c:choose>

					<tr class="${rowStyle}" style="valign='top'">
                        <td><a href="${url}/${obs.sbid}">${obs.sbid}</a></td>
						<td>${obs.obsStart}</td>
						<td>${obs.depositStarted}</td>
						<td align="right">${obs.imageCubes.size()}</td>
						<td>${obs.depositStateDescription}</td>
						<td>${obs.depositStateChanged}</td>
                        <td>
                            <table>
                            <c:forEach var="depositable" items="${failures}">
                                <tr>
                                    <td>${depositable.depositableArtefactTypeDescription}</td>
                                    <td>${depositable.filename}</td>
                                    <td>${depositable.checkpointStateType}</td>
                                </tr>
                            </c:forEach>
                            </table>
                        </td>
					</tr>
				</c:forEach>
			</table>

			<h2>Invalid Observations</h2>
			<c:if test="${empty invalidObservations}">
			<p>None</p>
			</c:if>
			<c:if test="${not empty invalidObservations}">
			<p>The observation.xml file of the following observations could not be successfully processed:</p>
			<table class="obsTable">

				<colgroup>
					<col />
				</colgroup>

				<tr class="tableHeader">
					<td>Scheduling Block Id</td>
				</tr>

				<c:forEach var="obs" items="${invalidObservations}"
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
						<td>${obs}</td>
					</tr>
				</c:forEach>
			</table>
			</c:if>

			<h2>Recently Deposited (last ${depositedParentDepositablesMaximumAge})</h2>
			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
				</colgroup>

				<tr class="tableHeader">
					<td>Scheduling Block Id</td>
					<td>Obs Start Time</td>
					<td>Deposit Started Date</td>
					<td>Deposit Completed Date</td>
				</tr>

				<c:forEach var="obs" items="${completedParentDepositables}"
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
						<td><a href="${url}/${obs.sbid}">${obs.sbid}</a></td>
						<td>${obs.obsStart}</td>
						<td>${obs.depositStarted}</td>
						<td>${obs.depositStateChanged}</td>
					</tr>
				</c:forEach>
			</table>

			<h2>Failed Deposits (last ${failedObservationsMaximumAge})</h2>
			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
                    <col />
                    <col />
				</colgroup>

                <tr class="tableHeader">
                    <td>Scheduling Block Id</td>
                    <td>Obs Start Time</td>
                    <td>Deposit Started Date</td>
                    <td>Num Images</td>
                    <td>Deposit Failed Date</td>
                    <td>Failures</td>
                </tr>

                <c:forEach var="obs" items="${failedParentDepositables}"
                    varStatus="rowCounter">
                    <c:choose>
                        <c:when test="${rowCounter.count % 2 == 0}">
                            <c:set var="rowStyle" scope="page" value="even" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="rowStyle" scope="page" value="odd" />
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${parentDepositableWithFailures.containsKey(obs.sbid)}">
                            <c:set var="failures" scope="page" value="${parentDepositableWithFailures.get(obs.sbid)}" />
                            <c:set var="maxErr" value="${failures.size()}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="failures" scope="page" value="" />
                            <c:set var="maxErr" value="1" />
                        </c:otherwise>
                    </c:choose>

                    <tr class="${rowStyle}" style="vertical-align:top">
                        <td><a href="${url}/${obs.sbid}">${obs.sbid}</a></td>
                        <td>${obs.obsStart}</td>
                        <td>${obs.depositStarted}</td>
                        <td align="right">${obs.imageCubes.size()}</td>
                        <td>${obs.depositStateChanged}</td>
                        <td>
                            <table>
                            <c:set var="extraErr" value="0" />
                            <c:if test="${maxErr gt 5}">
                            	<c:set var="extraErr" value="${maxErr-5}" />
                            	<c:set var="maxErr" value="5" />
                            </c:if>
                            <c:forEach var="depositable" items="${failures}" end="${maxErr-1}">
                                <tr>
                                    <td>${depositable.depositableArtefactTypeDescription}</td>
                                    <td>${depositable.filename}</td>
                                    <td>${depositable.checkpointStateType}</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${extraErr gt 0}">
                                <tr><td colspan='3'><b>and ${extraErr} further failure(s).</b></td></tr>
                            </c:if>
                            </table>
                        </td>
                    </tr>
                </c:forEach>
			</table>
		</div>


	</fieldset>
<jsp:include page="adminFooter.jsp" />
</body>
</html>
