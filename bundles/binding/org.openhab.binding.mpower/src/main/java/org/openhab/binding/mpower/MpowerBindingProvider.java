/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower;

import org.openhab.binding.mpower.internal.MpowerBindingConfig;
import org.openhab.core.binding.BindingProvider;

/**
 * Ubiquiti mPower strip binding
 * 
 * @author magcode
 */

public interface MpowerBindingProvider extends BindingProvider {

	public MpowerBindingConfig getConfigForItemName(String itemName);

	public MpowerBindingConfig getConfigForAddress(String address);

}
