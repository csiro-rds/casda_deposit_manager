<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Deposit Manager Home</title>
	<link rel="stylesheet" type="text/css" href="/casda_deposit_manager/css/deposit.css" />
</head>
<body>
    <div class="meta" style="float: left;">
        Home
    </div>
    <div class="meta" style="float: right;">
        <a  href="logout">logout</a>
    </div>
    <br /><br />

<fieldset>
		<legend>CASDA Deposit Manager</legend>		
		<div style="float: left;">
		  <ul>
			<li><a  href="level_5_deposits">CASDA Observation Deposit status</a></li>
			<li><a  href="level_5_refreshes">CASDA Observation Refresh status</a></li>
			<li><a  href="level_7_deposits">CASDA Level 7 Collection Deposit status</a></li>
			<li><a  href="jobs">CASDA Deposit Jobs</a></li>
			<li><a  href="sdoc.jsp">API Documentation</a></li>
	      </ul>
		</div>
</fieldset>

</body>
</html>
