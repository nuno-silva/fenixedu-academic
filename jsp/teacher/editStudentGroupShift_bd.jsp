<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<h2><bean:message key="title.editStudentGroupShift"/></h2>

<logic:present name="siteView" property="component">
 <bean:define id="component" name="siteView" property="component" />

	
<br>

	<table width="100%" cellpadding="0" cellspacing="0">
		<tr>
			<td class="infoop">
				<bean:message key="label.teacher.EditStudentGroupShift.description" />
			</td>
		</tr>
	</table>
	<br>
		
<html:form action="/editStudentGroupShift" method="get">
<html:hidden property="page" value="1"/>
<span class="error"><html:errors/></span>

<br>
<br>		 

<table width="50%" cellpadding="0" border="0">
		
		<tr>
			<td><bean:message key="label.editStudentGroupShift.oldShift"/></td>
			 <logic:present name="shift">
			<td><bean:write name="shift" property="nome"/></td>
			</logic:present>
			 <logic:notPresent name="shift">
			<td><bean:message key="message.NoShift"/></td>
			</logic:notPresent>
			<td><span class="error"><html:errors property="shiftType"/></span></td>
		</tr>
		
		<tr>
	 	<td><bean:message key="message.editStudentGroupShift"/></td>
		
		<td>
		<html:select property="shift" size="1">
    	<html:options collection="shiftsList" property="value" labelProperty="label"/>
    	</html:select>
    	</td>
			
			<td><span class="error"><html:errors property="shiftType"/></span></td>
		</tr>	

 
</table>

<table>
<tr>
	<td>
		<html:submit styleClass="inputbutton"><bean:message key="button.change"/>                    		         	
		</html:submit>
	</td>	
	<td>	
		<html:reset styleClass="inputbutton"><bean:message key="label.clear"/>
		</html:reset>
	</td>		
<br>
<br>
<html:hidden property="method" value="editStudentGroupShift"/>
<html:hidden  property="objectCode" value="<%= pageContext.findAttribute("objectCode").toString() %>" />
<logic:present name="shift">
<html:hidden  property="shiftCode" value="<%= request.getParameter("shiftCode") %>" />
</logic:present>
<html:hidden  property="studentGroupCode" value="<%= request.getParameter("studentGroupCode") %>" />
<html:hidden  property="groupPropertiesCode" value="<%= request.getParameter("groupPropertiesCode") %>" />
</html:form>


	<html:form action="/viewStudentGroupInformation" method="get">
	<td>
		<html:cancel styleClass="inputbutton"><bean:message key="button.cancel"/>                    		         	
		</html:cancel>
	</td>
		<html:hidden property="method" value="viewStudentGroupInformation"/>
		<html:hidden  property="objectCode" value="<%= pageContext.findAttribute("objectCode").toString() %>" />
		<html:hidden  property="groupPropertiesCode" value="<%= request.getParameter("groupPropertiesCode") %>" />
		<html:hidden  property="studentGroupCode" value="<%= request.getParameter("studentGroupCode") %>" />
		<logic:present name="shift">
		<html:hidden  property="shiftCode" value="<%= request.getParameter("shiftCode") %>" />
		</logic:present>
	</html:form>

</tr>
</table>
</logic:present>