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

package com.liferay.twitter;

import com.google.gson.Gson;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.struts.BaseStrutsAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * @author Christopher Dimitriadis
 */
public class TwitterOAuth extends BaseStrutsAction {
	
	private static Log _log = LogFactoryUtil.getLog(TwitterOAuth.class);

	public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("Calling the Twitter OAUTH Flow");
		_log.info("Calling the Twitter OAUTH Flow");
		
		HttpSession session = request.getSession();

		String twitterApiKey = PropsUtil.get("twitter.api.key");
		String twitterApiSecret = PropsUtil.get("twitter.api.secret");
		
		String oauthVerifier = ParamUtil.getString(request, "oauth_verifier");
		String oauthToken = ParamUtil.getString(request, "oauth_token");
		
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

		if ( Validator.isNull(oauthVerifier) || Validator.isNull(oauthToken) ) {
			return null;
		}
		
		OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
												   .apiKey(twitterApiKey)
												   .apiSecret(twitterApiSecret)
												   .build();

		Verifier verifier = new Verifier(oauthVerifier);

		Token requestToken = new Token(oauthToken, twitterApiSecret);

		Token accessToken = service.getAccessToken(requestToken, verifier);
		
		Map<String, Object> twitterData = getTwitterData(service, verifier, accessToken);

		User user = getUser(themeDisplay.getCompanyId(), twitterData);
				
		if ( user != null ) {
			System.out.println("The user with this Twitter Id exists");
			_log.info("The user with this Twitter Id exists");
			
			session.setAttribute(TwitterConstants.TWITTER_ID_LOGIN, user.getUserId());
			
			sendLoginRedirect(request, response);
			
			return null;
		}
		
		System.out.println("The user with this Twitter Id does not exist so redirect to create account flow");
		_log.info("The user with this Twitter Id does not exist so redirect to create account flow");
		
		session.setAttribute(TwitterConstants.TWITTER_LOGIN_PENDING, Boolean.TRUE);
		
		sendCreateAccountRedirect(request, response, twitterData);

		return null;
	}
	
	protected Map<String, Object> getTwitterData(OAuthService service, Verifier verifier, Token accessToken) throws Exception {
		String verifyCredentialsURL = PropsUtil.get("twitter.api.verify.credentials.url");

		OAuthRequest authrequest = new OAuthRequest(Verb.GET, verifyCredentialsURL);

		service.signRequest(accessToken, authrequest);
		
		String responseData = authrequest.send().getBody();
		
		Map<String, Object> jsonResponseMap = new Gson().fromJson(responseData, Map.class);
		
		return jsonResponseMap;
	}
	
	protected User getUser(long companyId, Map<String, Object> jsonResponse) throws Exception {
		ExpandoTable expandoTable = ExpandoTableLocalServiceUtil.getTable(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME);

		ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.getColumn(expandoTable.getTableId(), "twitterId");
		
		String twitterId =  jsonResponse.get("id").toString();

		List<ExpandoValue> expandoValues = ExpandoValueLocalServiceUtil.getColumnValues(expandoColumn.getCompanyId(), User.class.getName(), 
											ExpandoTableConstants.DEFAULT_TABLE_NAME, "twitterId", twitterId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		int usersCount = expandoValues.size();
		
		User user = null;

		if ( usersCount == 1 ) {			
			ExpandoValue expandoValue = expandoValues.get(0);

			long userId = expandoValue.getClassPK();

			user = UserLocalServiceUtil.getUser(userId);			
		}
		else if ( usersCount > 1 ) {			
			throw new Exception("There is more than 1 user with the same Twitter Id");
		}
		
		return user;
	}
	
	protected void sendLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

		PortletURL portletURL = PortletURLFactoryUtil.create(request, PortletKeys.FAST_LOGIN, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);

		portletURL.setWindowState(LiferayWindowState.POP_UP);
		portletURL.setParameter("struts_action", "/login/login_redirect");

		response.sendRedirect(portletURL.toString());
	}
	
	protected void sendCreateAccountRedirect(HttpServletRequest request, HttpServletResponse response, Map<String, Object> data) throws Exception {					
		String[] names = data.get("name").toString().split("\\s+");
		
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		
		PortletURL redirectURL = PortletURLFactoryUtil.create(request, PortletKeys.FAST_LOGIN, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
		redirectURL.setParameter("struts_action", "/login/login_redirect");
		redirectURL.setParameter("anonymousUser", Boolean.FALSE.toString());
		redirectURL.setPortletMode(PortletMode.VIEW);
		redirectURL.setWindowState(LiferayWindowState.POP_UP);
		
		PortletURL portletURL = PortletURLFactoryUtil.create(request, PortletKeys.LOGIN, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
		portletURL.setParameter("saveLastPath", Boolean.FALSE.toString());
		portletURL.setParameter("struts_action", "/login/create_account");
		portletURL.setParameter("redirect", redirectURL.toString());
		portletURL.setParameter("twitterId", data.get("id").toString());
		portletURL.setParameter("screenName", data.get("screen_name").toString());
		portletURL.setParameter("firstName", names[0]);
		portletURL.setParameter("lastName", names[1]);
		portletURL.setPortletMode(PortletMode.VIEW);
		portletURL.setWindowState(LiferayWindowState.POP_UP);

		response.sendRedirect(portletURL.toString());
	}
}