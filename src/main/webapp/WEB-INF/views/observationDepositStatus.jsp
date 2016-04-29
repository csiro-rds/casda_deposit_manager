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

				<c:forEach var="obs" items="${activeObservations}"
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
						<c:when test="${obsWithFailures.containsKey(obs.sbid)}">
							<c:set var="failures" scope="page" value="${obsWithFailures.get(obs.sbid)}" />
						</c:when>
						<c:otherwise>
							<c:set var="failures" scope="page" value="" />
						</c:otherwise>
					</c:choose>

					<tr class="${rowStyle}" style="valign='top'">
                        <td><a href="level_5_deposits/${obs.sbid}">${obs.sbid}</a></td>
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

			<h2>Recently Deposited (last ${depositedObservationsMaximumAge})</h2>
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

				<c:forEach var="obs" items="${completedObservations}"
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
						<td><a href="level_5_deposits/${obs.sbid}">${obs.sbid}</a></td>
						<td>${obs.obsStart}</td>
						<td>${obs.depositStarted}</td>
						<td>${obs.depositStateChanged}</td>
					</tr>
				</c:forEach>
			</table>

			<h2>Failed Deposits</h2>
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
                    <c:choose>
                        <c:when test="${obsWithFailures.containsKey(obs.sbid)}">
                            <c:set var="failures" scope="page" value="${obsWithFailures.get(obs.sbid)}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="failures" scope="page" value="" />
                        </c:otherwise>
                    </c:choose>

                    <tr class="${rowStyle}" style="valign='top'">
                        <td><a href="level_5_deposits/${obs.sbid}">${obs.sbid}</a></td>
                        <td>${obs.obsStart}</td>
                        <td>${obs.depositStarted}</td>
                        <td align="right">${obs.imageCubes.size()}</td>
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
		</div>


	</fieldset>
<jsp:include page="adminFooter.jsp" />
</body>
</html>
