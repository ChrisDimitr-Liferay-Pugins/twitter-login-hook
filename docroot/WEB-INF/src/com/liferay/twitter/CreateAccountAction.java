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

import java.util.Calendar;
import java.util.Locale;

import com.liferay.portal.AddressCityException;
import com.liferay.portal.AddressStreetException;
import com.liferay.portal.AddressZipException;
import com.liferay.portal.CompanyMaxUsersException;
import com.liferay.portal.ContactFirstNameException;
import com.liferay.portal.ContactFullNameException;
import com.liferay.portal.ContactLastNameException;
import com.liferay.portal.DuplicateUserEmailAddressException;
import com.liferay.portal.DuplicateUserScreenNameException;
import com.liferay.portal.EmailAddressException;
import com.liferay.portal.GroupFriendlyURLException;
import com.liferay.portal.NoSuchCountryException;
import com.liferay.portal.NoSuchListTypeException;
import com.liferay.portal.NoSuchOrganizationException;
import com.liferay.portal.NoSuchRegionException;
import com.liferay.portal.OrganizationParentException;
import com.liferay.portal.PhoneNumberException;
import com.liferay.portal.RequiredFieldException;
import com.liferay.portal.RequiredUserException;
import com.liferay.portal.ReservedUserEmailAddressException;
import com.liferay.portal.ReservedUserScreenNameException;
import com.liferay.portal.TermsOfUseException;
import com.liferay.portal.UserEmailAddressException;
import com.liferay.portal.UserIdException;
import com.liferay.portal.UserPasswordException;
import com.liferay.portal.UserScreenNameException;
import com.liferay.portal.UserSmsException;
import com.liferay.portal.WebsiteURLException;
import com.liferay.portal.kernel.captcha.CaptchaMaxChallengesException;
import com.liferay.portal.kernel.captcha.CaptchaTextException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Christopher Dimitriadis
 */
public class CreateAccountAction extends BaseStrutsPortletAction {
	
	private static Log _log = LogFactoryUtil.getLog(CreateAccountAction.class);

	public void processAction(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig, ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {	
		HttpServletRequest request = PortalUtil.getHttpServletRequest(actionRequest);
		HttpSession session = request.getSession();

		Boolean twitterLoginPending = (Boolean) session.getAttribute(TwitterConstants.TWITTER_LOGIN_PENDING);

		if ( (twitterLoginPending != null) && twitterLoginPending.booleanValue() ) {			
			ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

			Company company = themeDisplay.getCompany();

			if ( !company.isStrangers() ) {
				throw new PrincipalException();
			}

			try {
				addUser(actionRequest, actionResponse);
			}
			catch ( Exception e ) {
				if (e instanceof DuplicateUserEmailAddressException ||
					e instanceof DuplicateUserScreenNameException ||
					e instanceof AddressCityException ||
					e instanceof AddressStreetException ||
					e instanceof AddressZipException ||
					e instanceof CaptchaMaxChallengesException ||
					e instanceof CaptchaTextException ||
					e instanceof CompanyMaxUsersException ||
					e instanceof ContactFirstNameException ||
					e instanceof ContactFullNameException ||
					e instanceof ContactLastNameException ||
					e instanceof EmailAddressException ||
					e instanceof GroupFriendlyURLException ||
					e instanceof NoSuchCountryException ||
					e instanceof NoSuchListTypeException ||
					e instanceof NoSuchOrganizationException ||
					e instanceof NoSuchRegionException ||
					e instanceof OrganizationParentException ||
					e instanceof PhoneNumberException ||
					e instanceof RequiredFieldException ||
					e instanceof RequiredUserException ||
					e instanceof ReservedUserEmailAddressException ||
					e instanceof ReservedUserScreenNameException ||
					e instanceof TermsOfUseException ||
					e instanceof UserEmailAddressException ||
					e instanceof UserIdException ||
					e instanceof UserPasswordException ||
					e instanceof UserScreenNameException ||
					e instanceof UserSmsException ||
					e instanceof WebsiteURLException) {

					SessionErrors.add(actionRequest, e.getClass(), e);
				}
				else {
					throw e;
				}
			}
		}
		else {			
			originalStrutsPortletAction.processAction(originalStrutsPortletAction, portletConfig, actionRequest, actionResponse);
		}
	}

	public String render(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig, RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {
		return originalStrutsPortletAction.render(null, portletConfig, renderRequest, renderResponse);
	}

	public void serveResource(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig, ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
		originalStrutsPortletAction.serveResource(originalStrutsPortletAction, portletConfig, resourceRequest, resourceResponse);
	}

	protected void addUser(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {		
		HttpServletRequest request = PortalUtil.getHttpServletRequest(actionRequest);
		HttpSession session = request.getSession();

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

		Company company = themeDisplay.getCompany();
		
		long creatorUserId = 0;
		long facebookId = ParamUtil.getLong(actionRequest, "facebookId");
		boolean autoPassword = true;
		boolean autoScreenName = false;
		boolean male = ParamUtil.getBoolean(actionRequest, "male", true);
		boolean sendEmail = true;
		String twitterId = ParamUtil.getString(actionRequest, "twitterId");
		String emailAddress = ParamUtil.getString(actionRequest, "emailAddress");
		String screenName = ParamUtil.getString(actionRequest, "screenName");
		String firstName = ParamUtil.getString(actionRequest, "firstName");
		String lastName = ParamUtil.getString(actionRequest, "lastName");
		String middleName = ParamUtil.getString(actionRequest, "middleName");
		String password1 = StringPool.BLANK;
		String password2 = StringPool.BLANK;
		String jobTitle = ParamUtil.getString(actionRequest, "jobTitle");
		String openId = StringPool.BLANK;
		int birthdayMonth = ParamUtil.getInteger(actionRequest, "birthdayMonth");
		int birthdayDay = ParamUtil.getInteger(actionRequest, "birthdayDay");
		int birthdayYear = ParamUtil.getInteger(actionRequest, "birthdayYear");
		int prefixId = ParamUtil.getInteger(actionRequest, "prefixId");
		int suffixId = ParamUtil.getInteger(actionRequest, "suffixId");
		Locale locale = themeDisplay.getLocale();		
		
		long[] groupIds = null;
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;
		
		if ( GetterUtil.getBoolean(PropsUtil.get(PropsKeys.LOGIN_CREATE_ACCOUNT_ALLOW_CUSTOM_PASSWORD)) ) {
			autoPassword = false;

			password1 = ParamUtil.getString(actionRequest, "password1");
			password2 = ParamUtil.getString(actionRequest, "password2");
		}

		ServiceContext serviceContext = new ServiceContext();		

		User user = UserLocalServiceUtil.addUser(
			creatorUserId, company.getCompanyId(), autoPassword, password1, password2,
			autoScreenName, screenName, emailAddress, facebookId, openId, locale,
			firstName, middleName, lastName, prefixId, suffixId, male,
			birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
			organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

		user = UserLocalServiceUtil.updateLastLogin(user.getUserId(), user.getLoginIP());

		user = UserLocalServiceUtil.updatePasswordReset(user.getUserId(), false);

		user = UserLocalServiceUtil.updateEmailAddressVerified(user.getUserId(), false);
		
		ExpandoValueLocalServiceUtil.addValue(company.getCompanyId(), User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, "twitterId", user.getUserId(), twitterId);

		session.setAttribute(TwitterConstants.TWITTER_ID_LOGIN, new Long(user.getUserId()));

		session.removeAttribute(TwitterConstants.TWITTER_LOGIN_PENDING);

		String redirect = PortalUtil.getPortalURL(request);

		actionResponse.sendRedirect(redirect);
	}

}