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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.BaseAutoLogin;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Christopher Dimitriadis
 */
public class TwitterAutoLogin extends BaseAutoLogin {
	
	private static Log _log = LogFactoryUtil.getLog(TwitterAutoLogin.class);
	
	@Override
	protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("Calling the Auto Login Flow");
		_log.info("Calling the Auto Login Flow");
		
		long companyId = PortalUtil.getCompanyId(request);

		boolean twitterAuthEnabled = PrefsPropsUtil.getBoolean(companyId, "twitter.auth.enabled", true);

		if ( !twitterAuthEnabled ) {
			return null;
		}

		User user = getUser(request, companyId);

		if ( user == null ) {
			return null;
		}

		String[] credentials = new String[3];

		credentials[0] = String.valueOf(user.getUserId());
		credentials[1] = user.getPassword();
		credentials[2] = Boolean.TRUE.toString();

		return credentials;
	}
	
	protected User getUser(HttpServletRequest request, long companyId) throws PortalException, SystemException {
			HttpSession session = request.getSession();

			Long userId = (Long) session.getAttribute(TwitterConstants.TWITTER_ID_LOGIN);

			if ( Validator.isNull(userId) ) {
				return null;
			}

			session.removeAttribute(TwitterConstants.TWITTER_ID_LOGIN);

			User user = UserLocalServiceUtil.getUserById(companyId, userId);

			return user;
	}
}