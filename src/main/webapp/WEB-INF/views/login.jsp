<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>Login Page</title>
<link rel="stylesheet" type="text/css" href="/casda_deposit_manager/css/deposit.css" />
</head>
<body onload='document.f.username.focus();'>


<h1>
    CASDA Deposit Manager
</h1>
	<h2>Login with Username and Password</h2>
	
<%if (request.getParameter("error") != null) {%>
		<div> 
			<font color="red">
				Your login attempt was not successful, try again.
				<br>
				Invalid username and password.
			</font>
		</div>
		<br>
<%}%>
	
<%if (request.getParameter("logout") != null) {%>
		<div><font color="blue">You have been logged out.</font></div>
		<br>
<%}%>
	
	<form name='f' action='login' method='POST'>
		<table>
			<tr>
				<td>User:</td>
				<td><input type='text' name='username' value=''></td>
			</tr>
			<tr>
				<td>Password:</td>
				<td><input type='password' name='password' /></td>
			</tr>
			<tr>
				<td colspan='2'><input name="submit" type="submit"
					value="Login" /></td>
			</tr>
		</table>
	</form>
</body>
</html>