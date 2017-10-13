<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%--
 show.jsp: Displays details about a level 7 collection.

  Copyright 2015, CSIRO Australia.
  All rights reserved.
 --%>
<html>
<head>
<title>CSIRO Data Access Portal - Level 7 Collection ${level7collection.getDapCollectionId()}</title>
<link rel="stylesheet" type="text/css" href="/casda_deposit_manager/css/deposit.css" />
</head>
<body>
    <div class="meta" style="float: left;">
        <a href="/casda_deposit_manager">Home</a>&nbsp;&gt;&nbsp;
        <a href="/casda_deposit_manager/level_7_deposits">Level 7 Collection Deposit Status</a>&nbsp;&gt;&nbsp;
        Level 7 Collection ${level7Collection.getDapCollectionId()}
    </div>
    
    <div class="meta" style="float: right;">
		<a  href="../logout">logout</a>
	</div>
	<jsp:include page="../adminHeader.jsp" />
    <fieldset>
        <legend>Level 7 Collection ${level7Collection.getDapCollectionId()}</legend>

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

        <fieldset>
            <legend>Level 7 Collection Details</legend>

            <table class="obsTable">
    
                <tr>
                    <td><strong>Project</strong></td>
                    <td>${level7Collection.project.opalCode}</td>
                </tr>

            </table>
        </fieldset>

        <br />

        <fieldset>
            <legend>Deposit Status</legend>

            <table class="obsTable">
                <tr>
                    <td><strong>Deposit State</strong></td>
                    <td>
                        ${level7Collection.depositStateDescription}
	                    <c:if test="${level7Collection.failedDeposit}">
	                        <em>(Deposit can be recovered through DAP System Tasks.)</em>
	                    </c:if>
                    </td>
                </tr>
                <tr>
                    <td><strong>Deposit Started Date</strong></td>
                    <td>${level7Collection.depositStarted}</td>
                </tr>
                <tr>
                    <td><strong>Deposit Completed Date</strong></td>
                    <td>${level7Collection.depositStateChanged}</td>
                </tr>
            </table>
    
            <h3>Collection Artefacts</h3>
    
            <table class="obsTable">
    
                <tr class="tableHeader">
                    <td>Artefact Type</td>
                    <td>Filename</td>
                    <td>Deposit State</td>
                    <td>Deposit State Changed</td>
                    <td>Checkpoint State</td>
                </tr>
    
                <c:forEach var="depositable" items="${depositableArtefacts}" varStatus="rowCounter">
                    <c:choose>
                        <c:when test="${rowCounter.count % 2 == 0}">
                            <c:set var="rowStyle" scope="page" value="even" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="rowStyle" scope="page" value="odd" />
                        </c:otherwise>
                    </c:choose>
    
                    <tr class="${rowStyle}">
                        <td>${depositable.depositableArtefactTypeDescription}</td>
                        <td>${depositable.filename}</td>
                        <td>${depositable.depositStateDescription}</td>
                        <td>${depositable.depositStateChanged}</td>
                        <td>${depositable.checkpointStateType}</td>
                    </tr>
                </c:forEach>
                
                                <c:set var="prevRemainder" value="0" />
                <c:if test="${rowStyle eq 'odd'}">
                	<c:set var="prevRemainder" value="1" />
                </c:if>
                <c:forEach var="summary" items="${artefactSummaries}" varStatus="rowCounter">
                	
                    <c:choose>
                        <c:when test="${(prevRemainder+rowCounter.count) % 2 == 0}">
                            <c:set var="rowStyle" scope="page" value="even" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="rowStyle" scope="page" value="odd" />
                        </c:otherwise>
                    </c:choose>
    
                    <tr class="${rowStyle}">
                        <td>${summary.description}</td>
                        <td>${summary.numArtefacts} file(s)</td>
                        <td>${summary.depositStateDescription}</td>
                        <td>${summary.depositStateChanged}</td>
                        <td>${summary.checkpointStateType}</td>
                    </tr>
                </c:forEach>
                
            </table>
        </fieldset>
    </fieldset>
<jsp:include page="../adminFooter.jsp" />
</body>
</html>
