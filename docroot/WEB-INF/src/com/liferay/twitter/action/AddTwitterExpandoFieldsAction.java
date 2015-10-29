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

package com.liferay.twitter.action;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.model.User;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;

/**
 * @author Christopher Dimitriadis
 */
public class AddTwitterExpandoFieldsAction extends SimpleAction {
	
	private static Log _log = LogFactoryUtil.getLog(AddTwitterExpandoFieldsAction.class);

	@Override
	public void run(String[] ids) throws ActionException {
		System.out.println("Creating Twitter fields for User table");
		_log.info("Creating Twitter fields for User table");
		
		try {
			doRun(GetterUtil.getLong(ids[0]));
		}
		catch (Exception e) {
			throw new ActionException(e);
		}
	}

	protected void doRun(long companyId) throws Exception {
		System.out.println("Create Twitter fields for User table");
		_log.info("Create Twitter fields for User table");
		
		ExpandoTable expandoTable = null;

		try {
			expandoTable = ExpandoTableLocalServiceUtil.addTable(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME);
		}
		catch (Exception e) {
			expandoTable = ExpandoTableLocalServiceUtil.getTable(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME);
		}

		try {
			UnicodeProperties properties = new UnicodeProperties();

			properties.setProperty("hidden", "true");
			properties.setProperty("visible-with-update-permission", "false");
			
			addColumn(expandoTable.getTableId(), "twitterId", properties);

			if ( _log.isInfoEnabled() ) {
				_log.info("Custom field 'twitterId' added to User entity");
			}
		}
		catch ( Exception e ) {
			if ( _log.isDebugEnabled() ) {
				_log.debug("Couldn't add custom field 'twitterId'");
			}
		}
	}
	
	protected void addColumn(long tableId, String name, UnicodeProperties properties) throws PortalException, SystemException {
		ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(tableId, name);

		if ( column != null ) {
			return;
		}

		ExpandoColumn twitterId = ExpandoColumnLocalServiceUtil.addColumn(tableId, name, ExpandoColumnConstants.STRING);

		ExpandoColumnLocalServiceUtil.updateTypeSettings(twitterId.getColumnId(), properties.toString());
	}
}