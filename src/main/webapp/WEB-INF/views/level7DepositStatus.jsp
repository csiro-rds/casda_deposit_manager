<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%--
 level7DepositStatus.jsp: Displays the list of active, failed and recently completed deposit jobs

  Copyright 2015, CSIRO Australia
  All rights reserved.

 --%>
 <html>
<head>
<title>CSIRO Data Deposit Manager - CASDA Level 7 Collection Deposit Status</title>
<link rel="stylesheet" type="text/css" href="/casda_deposit_manager/css/deposit.css" />
</head>
<body>
    <div class="meta" style="float: left;">
        <a href="/casda_deposit_manager">Home</a>&nbsp;&gt;&nbsp;
        Level 7 Collection Deposit Status
    </div>
	<div class="meta" style="float: right;">
		<a  href="logout">logout</a>
	</div>
	<jsp:include page="adminHeader.jsp" />
	
	<fieldset>
		<legend>CASDA Level 7 Collection Deposit Status</legend>		
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
					<td>Collection Id</td>
					<td>Project Code</td>
					<td>Deposit Started Date</td>
					<td>Num Catalogues</td>
					<td>State</td>
					<td>Last Updated</td>
					<td>Failures</td>
				</tr>

				<c:forEach var="lsev" items="${activeDeposits}"
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
						<c:when test="${depositsWithFailures.containsKey(lsev.dapCollectionId)}">
							<c:set var="failures" scope="page" value="${depositsWithFailures.get(lsev.dapCollectionId)}" />
						</c:when>
						<c:otherwise>
							<c:set var="failures" scope="page" value="" />
						</c:otherwise>
					</c:choose>

					<tr class="${rowStyle}" style="valign='top'">
                        <td><a href="level_7_deposits/${lsev.dapCollectionId}">${lsev.dapCollectionId}</a></td>
						<td>${lsev.project.opalCode}</td>
						<td>${lsev.depositStarted}</td>
						<td align="right">${lsev.catalogues.size()}</td>
						<td>${lsev.depositStateDescription}</td>
						<td>${lsev.depositStateChanged}</td>
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

			<h2>Recently Deposited (last ${depositsMaximumAge})</h2>
			<table class="obsTable">

				<colgroup>
					<col />
					<col />
					<col />
					<col />
				</colgroup>

				<tr class="tableHeader">
					<td>Collection Id</td>
					<td>Project code</td>
					<td>Deposit Started Date</td>
					<td>Deposit Completed Date</td>
				</tr>

				<c:forEach var="lsev" items="${completedDeposits}"
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
						<td><a href="level_7_deposits/${lsev.dapCollectionId}">${lsev.dapCollectionId}</a></td>
						<td>${lsev.project.opalCode}</td>
						<td>${lsev.depositStarted}</td>
						<td>${lsev.depositStateChanged}</td>
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
                    <td>Collection Id</td>
                    <td>Project Code</td>
                    <td>Deposit Started Date</td>
                    <td>Num Catalogues</td>
                    <td>Deposit Failed Date</td>
                    <td>Failures</td>
                </tr>

                <c:forEach var="lsev" items="${failedDeposits}"
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
                        <c:when test="${depositsWithFailures.containsKey(lsev.dapCollectionId)}">
                            <c:set var="failures" scope="page" value="${depositsWithFailures.get(lsev.dapCollectionId)}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="failures" scope="page" value="" />
                        </c:otherwise>
                    </c:choose>

                    <tr class="${rowStyle}" style="valign='top'">
                        <td><a href="level_7_deposits/${lsev.dapCollectionId}">${lsev.dapCollectionId}</a></td>
                        <td>${lsev.project.opalCode}</td>
                        <td>${lsev.depositStarted}</td>
                        <td align="right">${lsev.catalogues.size()}</td>
                        <td>${lsev.depositStateChanged}</td>
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
