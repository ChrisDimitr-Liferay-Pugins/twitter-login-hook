<%@ page import="com.liferay.portal.util.PortalUtil" %>

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

<%@ page import="org.scribe.builder.ServiceBuilder" %>
<%@ page import="org.scribe.builder.api.TwitterApi" %>
<%@ page import="org.scribe.model.Token" %>
<%@ page import="org.scribe.oauth.OAuthService" %>

<%@ include file="/html/portlet/login/init.jsp" %>

<%
String twitterApiKey = PropsUtil.get("twitter.api.key");
String twitterApiSecret = PropsUtil.get("twitter.api.secret");
String twitterAuthURL = PropsUtil.get("twitter.api.auth.url");
String twitterCallbackURL = PrefsPropsUtil.getString(company.getCompanyId(), "twitter.api.callback.url");
boolean twitterAuthEnabled = PrefsPropsUtil.getBoolean(company.getCompanyId(), "twitter.auth.enabled", true);

OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
										   .apiKey(twitterApiKey)
							               .apiSecret(twitterApiSecret)
							               .callback(twitterCallbackURL)
							               .build();

Token requestToken = service.getRequestToken();

String authURL = service.getAuthorizationUrl(requestToken);

String taglibOpenTwitterLoginWindow = "javascript:var twitterLoginWindow = window.open('" + authURL.toString() + "', 'facebook', 'align=center,directories=no,height=560,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no,width=1000'); void(''); twitterLoginWindow.focus();";
%>

<c:if test="<%= twitterAuthEnabled %>">
	<liferay-ui:icon
		message="twitter"
		src="/html/portlet/login/navigation/twitter.png"
		url="<%= taglibOpenTwitterLoginWindow %>"
	/>
</c:if>