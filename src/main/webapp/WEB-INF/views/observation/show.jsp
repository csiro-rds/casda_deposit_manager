<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%--
 show.jsp: Displays details about an observation.

  Copyright 2015, CSIRO Australia.
  All rights reserved.
 --%>
<html>
<head>
<title>CSIRO Data Access Portal - Observation ${observation.getSbid()}</title>
<link rel="stylesheet" type="text/css" href="/casda_deposit_manager/css/deposit.css" />
</head>
<body>
    <div class="meta" style="float: left;">
        <a href="/casda_deposit_manager">Home</a>&nbsp;&gt;&nbsp;
        <a href="/casda_deposit_manager/level_5_deposits">Observation Deposit Status</a>&nbsp;&gt;&nbsp;
        Observation ${observation.getSbid()}
    </div>
	<div class="meta" style="float: right;">
		<a  href="../logout">logout</a>
	</div>
	<jsp:include page="../adminHeader.jsp" />
    <fieldset>
        <legend>Observation ${observation.getSbid()}</legend>

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
            <legend>Observation Details</legend>

            <table class="obsTable">
    
                <tr>
                    <td><strong>Telescope</strong></td>
                    <td>${observation.telescope}</td>
                </tr>
                <tr>
                    <td><strong>Observing Program</strong></td>
                    <td>${observation.obsProgram}</td>
                </tr>
                <tr>
                    <td><strong>Observation Start Time</strong></td>
                    <td>${observation.obsStart}</td>
                </tr>
                <tr>
                    <td><strong>Observation End Time</strong></td>
                    <td>${observation.obsEnd}</td>
                </tr>
            </table>
        </fieldset>

        <br />

        <fieldset>
            <legend>Deposit Status</legend>

            <table class="obsTable">
                <tr>
                    <td><strong>Deposit State</strong></td>
                    <td>${observation.depositStateDescription}</td>
                    <td>
                        <c:if test="${observation.failedDeposit}">
                            <form method="post" action="${observation.sbid}/recover">
                                <input type="submit" value="Recover Deposit"></input>
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            </form>
                        </c:if>
                        <c:if test="${observation.deposited}">
                            <form method="post" action="${observation.sbid}/redeposit">
                                <input type="submit" value="Redeposit"></input>
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            </form>
                        </c:if>
                    </td>
                    <td>
                        <c:if test="${observation.deposited}">
                            <form method="post" action="${observation.sbid}/refresh">
                                <input type="submit" value="Refresh Metadata"></input>
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            </form>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <td><strong>Deposit Started Date</strong></td>
                    <td>${observation.depositStarted}</td>
                    <td colspan=2></td>
                </tr>
                <tr>
                    <td><strong>Deposit Completed Date</strong></td>
                    <td>${observation.depositStateChanged}</td>
                    <td colspan=2></td>
                </tr>
            </table>
    
    <table>
    	<tr>
    	<td>
    		<h3>Observation Artefacts</h3>
    	</td>
    	<td>
          <c:if test="${artefactFailed and not observation.failedDeposit}">
             <form method="post" action="${observation.sbid}/recover/all">
                 <input type="submit" value="Recover All"></input>
                 <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
             </form>
           </c:if>
    	</td>
    	</tr>
    </table>
            
            <table class="obsTable">
    
                <tr class="tableHeader">
                    <td>Artefact Type</td>
                    <td>Filename</td>
                    <td>Deposit State</td>
                    <td>Deposit State Changed</td>
                    <td>Checkpoint State</td>
                    <td>Released date</td>
                    <td>Project</td>
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
                        <td>${depositable.depositStateDescription} 
                        <c:if test="${depositable.failedDeposit && !observation.failedDeposit }">
                            <form method="post" action="${observation.sbid}/artefacts/${depositable.fileId}/recover">
                                <input type="submit" value="Recover Deposit"></input>
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            </form>
                        </c:if>
                        </td>
                        <td>${depositable.depositStateChanged}</td>
                        <td>${depositable.checkpointStateType}</td>
                        <!-- Only display the released date if it is valid for that artefact type -->
                        <td>
                            <c:catch var="exception">${depositable.releasedDate }
                            </c:catch><c:if test="${not empty exception}">N/A</c:if>
                        </td>
                        <!-- Only display the project code if it is valid for that artefact type -->
                        <td>
                            <c:catch var="exception">${depositable.project.opalCode }
                            </c:catch><c:if test="${not empty exception}">N/A</c:if>
                        </td>
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
                        <!-- Only display the released date if it is valid for that artefact type -->
                        <td>${summary.releasedDateString}</td>
                        <!-- Only display the project code if it is valid for that artefact type -->
                        <td>${summary.projectCode }</td>
                    </tr>
                </c:forEach>
            </table>
        </fieldset>
    </fieldset>
<jsp:include page="../adminFooter.jsp" />
</body>
</html>
