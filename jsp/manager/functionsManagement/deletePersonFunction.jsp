<%@ taglib uri="/WEB-INF/jsf_core.tld" prefix="f"%>
<%@ taglib uri="/WEB-INF/jsf_fenix_components.tld" prefix="fc"%>
<%@ taglib uri="/WEB-INF/jsf_tiles.tld" prefix="ft"%>
<%@ taglib uri="/WEB-INF/html_basic.tld" prefix="h"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>

<ft:tilesView definition="df.page.functionsManagement" attributeName="body-inline">
	<f:loadBundle basename="resources/HtmlAltResources" var="htmlAltBundle"/>

	<f:loadBundle basename="resources/DepartmentAdmOfficeResources" var="bundle"/>
	
	<h:form>

		<h:inputHidden binding="#{managerFunctionsManagementBackingBean.personIDHidden}"/>
	   	<h:inputHidden binding="#{managerFunctionsManagementBackingBean.personFunctionIDHidden}"/>
	
		<h:outputText value="<h2>#{bundle['label.confirmation']}</h2>" escape="false"/>	
		<h:outputText value="<br/>" escape="false" />
		
		<h:outputText styleClass="error" rendered="#{!empty managerFunctionsManagementBackingBean.errorMessage}"
					value="#{bundle[managerFunctionsManagementBackingBean.errorMessage]}"/>
		
		<h:panelGrid columns="2">
			<h:outputText value="<b>#{bundle['label.name']}</b>: " escape="false"/>		
			<h:outputText value="#{managerFunctionsManagementBackingBean.person.name}"/>		
		</h:panelGrid>	
		
		<h:outputText value="<br/>" escape="false" />
		
		<h:panelGrid columns="2" columnClasses="valigntop">		
			<h:outputText value="<b>#{bundle['label.function']}</b>" escape="false"/>	
			<h:outputText value="#{managerFunctionsManagementBackingBean.personFunction.function.name}"/>
					
			<h:outputText value="<b>#{bundle['label.search.unit']}:</b>" escape="false"/>								
			<h:outputText value="#{managerFunctionsManagementBackingBean.personFunction.unit.presentationNameWithParentsAndBreakLine}" escape="false"/>	
		
			<h:outputText value="<b>#{bundle['label.credits']}</b>" escape="false"/>	
			<h:outputText value="#{managerFunctionsManagementBackingBean.personFunction.credits}"/>
		
			<h:outputText value="<b>#{bundle['label.valid']}</b>" escape="false"/>	
			<h:panelGroup>
				<h:outputText value="#{managerFunctionsManagementBackingBean.personFunction.beginDate}" escape="false"/>	
				<h:outputText value="<b>&nbsp;#{bundle['label.to']}&nbsp;</b>" escape="false"/>	
				<h:outputText value="#{managerFunctionsManagementBackingBean.personFunction.endDate}" escape="false"/>		
			</h:panelGroup>
		</h:panelGrid>
			
		<h:outputText value="<br/>" escape="false" />
		<h:panelGrid columns="2">
			<h:commandButton alt="#{htmlAltBundle['button.delete']}" action="#{managerFunctionsManagementBackingBean.deletePersonFunction}" value="#{bundle['button.delete']}" styleClass="inputbutton"/>							
			<h:commandButton alt="#{htmlAltBundle['commandButton.cancel']}" action="success" immediate="true" value="#{bundle['button.cancel']}" styleClass="inputbutton"/>								
		</h:panelGrid>
	
	</h:form>

</ft:tilesView>