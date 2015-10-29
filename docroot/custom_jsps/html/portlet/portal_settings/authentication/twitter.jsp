<%--
/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portlet/portal_settings/init.jsp" %>

<%
boolean twitterAuthEnabled = PrefsPropsUtil.getBoolean(company.getCompanyId(), "twitter.auth.enabled", true);
String twitterAPIKey = PrefsPropsUtil.getString(company.getCompanyId(), "twitter.api.key");
String twitterAPISecret = PrefsPropsUtil.getString(company.getCompanyId(), "twitter.api.secret");
String twitterCallbackURL = PrefsPropsUtil.getString(company.getCompanyId(), "twitter.api.callback.url");
%>

<aui:fieldset>
	<aui:input label="enabled" name='<%= "settings--twitter.auth.enabled--" %>' type="checkbox" value="<%= twitterAuthEnabled %>" />

	<aui:input cssClass="lfr-input-text-container" label="API Key" name='<%= "settings--twitter.api.key--" %>' type="text" value="<%= twitterAPIKey %>" />

	<aui:input cssClass="lfr-input-text-container" label="API Secret" name='<%= "settings--twitter.api.secret--" %>' type="text" value="<%= twitterAPISecret %>" />
	
	<aui:input cssClass="lfr-input-text-container" label="Redirect URL" name='<%= "settings--twitter.api.callback.url--" %>' type="text" value="<%= twitterCallbackURL %>" />
</aui:fieldset>